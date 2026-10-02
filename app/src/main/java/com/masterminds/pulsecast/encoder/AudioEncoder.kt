package com.masterminds.pulsecast.encoder

import android.annotation.SuppressLint
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioPlaybackCaptureConfiguration
import android.media.AudioRecord
import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaFormat
import android.media.MediaRecorder
import android.media.projection.MediaProjection
import android.os.Build
import android.os.Handler
import android.os.HandlerThread
import android.util.Log
import androidx.annotation.RequiresApi
import com.masterminds.pulsecast.core.AudioCaptureConfig
import com.masterminds.pulsecast.core.AudioMode
import com.masterminds.pulsecast.core.PtsAdjuster
import com.masterminds.pulsecast.core.TrackType
import com.masterminds.pulsecast.streaming.EncodedSample
import com.masterminds.pulsecast.streaming.EncodedSampleSink
import com.masterminds.pulsecast.streaming.AudioFormatAwareSink
import com.masterminds.pulsecast.streaming.FanOutDistributor
import java.util.concurrent.LinkedBlockingQueue

/**
 * Captures mic audio via AudioRecord and encodes it to AAC. Presentation
 * timestamps are derived from the running sample count divided by sample
 * rate, NOT System.nanoTime() at read time — sample-count-based timing is
 * what stays audio/video-sync-accurate over a long recording.
 *
 * Phase 3 change: same fan-out change as VideoEncoder — output goes to a
 * list of sinks (local muxer + any live RTMP destinations), not a single
 * MuxerWrapper directly.
 */
class AudioEncoder(
    private val muxer: MuxerWrapper,
    sinks: List<EncodedSampleSink>,
    private val ptsAdjuster: PtsAdjuster,
    private val config: AudioCaptureConfig = AudioCaptureConfig(),
    private val mediaProjection: MediaProjection? = null
) {
    private val codec = MediaCodec.createEncoderByType(MediaFormat.MIMETYPE_AUDIO_AAC)
    private val handlerThread = HandlerThread("AudioEncoderCallback").apply { start() }
    private val handler = Handler(handlerThread.looper)
    private val distributor = FanOutDistributor(sinks)
    private val audioConfigSinks = sinks.filterIsInstance<AudioFormatAwareSink>()

    private val sampleRate = config.sampleRate
    private val minBufferSize = AudioRecord.getMinBufferSize(
        sampleRate,
        AudioFormat.CHANNEL_IN_MONO,
        AudioFormat.ENCODING_PCM_16BIT
    )

    private var micRecord: AudioRecord? = null
    private var internalRecord: AudioRecord? = null

    private var dualBusMicThread: Thread? = null
    private val dualBusMicQueue = LinkedBlockingQueue<ShortArray>()

    @Volatile private var recording = false
    @Volatile private var capturePaused = false
    @Volatile private var microphoneMuted = false
    private var samplesWritten: Long = 0

    init {
        setupAudioRecords()

        val format = MediaFormat.createAudioFormat(MediaFormat.MIMETYPE_AUDIO_AAC, sampleRate, 1).apply {
            setInteger(MediaFormat.KEY_AAC_PROFILE, MediaCodecInfo.CodecProfileLevel.AACObjectLC)
            setInteger(MediaFormat.KEY_BIT_RATE, config.bitrate)
        }
        codec.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)

        codec.setCallback(object : MediaCodec.Callback() {
            override fun onInputBufferAvailable(codec: MediaCodec, index: Int) {
                val buffer = codec.getInputBuffer(index) ?: return
                if (!recording || capturePaused) {
                    codec.queueInputBuffer(index, 0, 0, (samplesWritten * 1_000_000L) / sampleRate, 0)
                    return
                }

                val pcmBuffer = ShortArray(buffer.capacity() / 2)
                var shortsRead = 0

                when {
                    micRecord != null && internalRecord != null -> {
                        val micBuffer = dualBusMicQueue.poll()
                        val sizeToRead = micBuffer?.size ?: 1024
                        shortsRead = internalRecord?.read(pcmBuffer, 0, minOf(sizeToRead, pcmBuffer.size)) ?: 0
                        if (shortsRead > 0 && micBuffer != null) {
                            val len = minOf(shortsRead, micBuffer.size)
                            if (microphoneMuted) micBuffer.fill(0, 0, len)
                            mixPcmBuffers(pcmBuffer, micBuffer, pcmBuffer, len)
                        }
                    }
                    internalRecord != null -> {
                        shortsRead = internalRecord?.read(pcmBuffer, 0, pcmBuffer.size) ?: 0
                    }
                    micRecord != null -> {
                        shortsRead = micRecord?.read(pcmBuffer, 0, pcmBuffer.size) ?: 0
                        if (microphoneMuted && shortsRead > 0) pcmBuffer.fill(0, 0, shortsRead)
                    }
                }

                if (shortsRead > 0) {
                    val bytesRead = shortsRead * 2
                    buffer.clear()
                    buffer.asShortBuffer().put(pcmBuffer, 0, shortsRead)

                    val presentationTimeUs = (samplesWritten * 1_000_000L) / sampleRate
                    samplesWritten += shortsRead
                    codec.queueInputBuffer(index, 0, bytesRead, presentationTimeUs, 0)
                } else {
                    codec.queueInputBuffer(index, 0, 0, 0, 0)
                }
            }

            override fun onOutputBufferAvailable(codec: MediaCodec, index: Int, info: MediaCodec.BufferInfo) {
                if (info.size > 0) {
                    val buffer = codec.getOutputBuffer(index)
                    if (buffer != null) {
                        buffer.position(info.offset)
                        buffer.limit(info.offset + info.size)
                        val sample = EncodedSample(
                            track = TrackType.AUDIO,
                            data = buffer,
                            // Audio timestamps are derived from samples actually captured. Since paused
                            // recorders are stopped, sample time already excludes the pause interval.
                            presentationTimeUs = info.presentationTimeUs,
                            isKeyFrame = true
                        )
                        distributor.distribute(sample)
                    }
                }
                codec.releaseOutputBuffer(index, false)
            }

            override fun onOutputFormatChanged(codec: MediaCodec, format: MediaFormat) {
                muxer.addTrack(TrackType.AUDIO, format)
                val codecSpecificData = format.getByteBuffer("csd-0")?.duplicate()
                if (codecSpecificData != null) {
                    val config = ByteArray(codecSpecificData.remaining()).also(codecSpecificData::get)
                    val actualSampleRate = format.getInteger(MediaFormat.KEY_SAMPLE_RATE)
                    val actualChannelCount = format.getInteger(MediaFormat.KEY_CHANNEL_COUNT)
                    audioConfigSinks.forEach { sink ->
                        runCatching { sink.setAudioConfig(config, actualSampleRate, actualChannelCount) }
                    }
                }
            }

            override fun onError(codec: MediaCodec, e: MediaCodec.CodecException) {
                Log.e("AudioEncoder", "Audio MediaCodec error: ${e.diagnosticInfo}", e)
            }
        }, handler)
    }

    @SuppressLint("MissingPermission")
    private fun setupAudioRecords() {
        val useInternal = (config.mode == AudioMode.INTERNAL_ONLY || config.mode == AudioMode.INTERNAL_AND_MIC) &&
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && mediaProjection != null

        // Respect the selected source. In particular, an unsupported or
        // unavailable playback-capture source must not silently record the
        // microphone when the user explicitly chose INTERNAL_ONLY.
        val useMic = config.mode == AudioMode.MIC_ONLY || config.mode == AudioMode.INTERNAL_AND_MIC

        if (useInternal) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                internalRecord = createInternalAudioRecord()
            }
        }

        if (useMic) {
            micRecord = createMicAudioRecord()
        }
    }

    private fun createMicAudioRecord(): AudioRecord {
        return AudioRecord(
            MediaRecorder.AudioSource.MIC,
            sampleRate,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT,
            minBufferSize * 2
        )
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun createInternalAudioRecord(): AudioRecord? {
        return try {
            val playbackConfig = AudioPlaybackCaptureConfiguration.Builder(mediaProjection!!)
                .addMatchingUsage(AudioAttributes.USAGE_GAME)
                .addMatchingUsage(AudioAttributes.USAGE_MEDIA)
                .addMatchingUsage(AudioAttributes.USAGE_UNKNOWN)
                .build()

            val format = AudioFormat.Builder()
                .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                .setSampleRate(sampleRate)
                .setChannelMask(AudioFormat.CHANNEL_IN_MONO)
                .build()

            AudioRecord.Builder()
                .setAudioFormat(format)
                .setBufferSizeInBytes(minBufferSize * 4)
                .setAudioPlaybackCaptureConfig(playbackConfig)
                .build()
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Dual-channel floating point PCM mixer thread performing soft-clipping summation
     * between internal game audio and mic capture to prevent digital clipping/distortion.
     */
    private fun mixPcmBuffers(internal: ShortArray, mic: ShortArray, out: ShortArray, len: Int) {
        for (i in 0 until len) {
            val iVal = internal[i].toFloat() / 32768f
            val mVal = mic[i].toFloat() / 32768f
            val sum = iVal + mVal
            // Soft clipping curve for smooth analog saturation
            val mixed = if (sum > 1.0f) {
                1.0f - Math.exp(-sum.toDouble() + 1.0).toFloat()
            } else if (sum < -1.0f) {
                -1.0f + Math.exp(sum.toDouble() + 1.0).toFloat()
            } else {
                sum
            }
            out[i] = (mixed * 32767f).coerceIn(-32768f, 32767f).toInt().toShort()
        }
    }

    fun start() {
        recording = true
        micRecord?.startRecording()
        internalRecord?.startRecording()

        if (micRecord != null && internalRecord != null) {
            dualBusMicThread = Thread {
                val pool = Array(4) { ShortArray(2048) }
                var poolIndex = 0
                while (recording) {
                    if (capturePaused) {
                        try { Thread.sleep(20) } catch (_: InterruptedException) { break }
                        continue
                    }
                    val currentBuffer = pool[poolIndex]
                    val read = micRecord?.read(currentBuffer, 0, currentBuffer.size) ?: 0
                    if (read > 0) {
                        val copy = ShortArray(read)
                        System.arraycopy(currentBuffer, 0, copy, 0, read)
                        if (dualBusMicQueue.size > 8) {
                            dualBusMicQueue.poll()
                        }
                        dualBusMicQueue.offer(copy)
                        poolIndex = (poolIndex + 1) % pool.size
                    }
                }
            }.apply { start() }
        }

        codec.start()
    }

    fun stop() {
        recording = false
        runCatching { dualBusMicThread?.join(500) }
        dualBusMicThread = null

        runCatching { micRecord?.stop() }
        runCatching { micRecord?.release() }
        micRecord = null

        runCatching { internalRecord?.stop() }
        runCatching { internalRecord?.release() }
        internalRecord = null

        runCatching { codec.stop() }
        runCatching { codec.release() }
        runCatching { handlerThread.quitSafely() }
    }

    fun pauseCapture() {
        if (capturePaused) return
        capturePaused = true
        dualBusMicQueue.clear()
        runCatching { micRecord?.stop() }
        runCatching { internalRecord?.stop() }
    }

    fun resumeCapture() {
        if (!capturePaused || !recording) return
        runCatching { micRecord?.startRecording() }
        runCatching { internalRecord?.startRecording() }
        dualBusMicQueue.clear()
        capturePaused = false
    }

    fun setMicrophoneMuted(muted: Boolean) {
        microphoneMuted = muted
        if (muted && config.mode == AudioMode.MIC_ONLY) {
            // Inputs are still read and timestamped, but muted samples are encoded as silence.
        }
    }
}
