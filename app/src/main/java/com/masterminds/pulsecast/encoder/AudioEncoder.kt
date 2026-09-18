package com.masterminds.pulsecast.encoder

import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaFormat
import android.media.MediaRecorder
import android.os.Handler
import android.os.HandlerThread
import com.masterminds.pulsecast.core.PtsAdjuster
import com.masterminds.pulsecast.core.TrackType
import com.masterminds.pulsecast.streaming.EncodedSample
import com.masterminds.pulsecast.streaming.EncodedSampleSink
import com.masterminds.pulsecast.streaming.FanOutDistributor

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
    private val sampleRate: Int = 44_100,
) {
    private val codec = MediaCodec.createEncoderByType(MediaFormat.MIMETYPE_AUDIO_AAC)
    private val handlerThread = HandlerThread("AudioEncoderCallback").apply { start() }
    private val handler = Handler(handlerThread.looper)
    private val distributor = FanOutDistributor(sinks)

    private val minBufferSize = AudioRecord.getMinBufferSize(
        sampleRate,
        AudioFormat.CHANNEL_IN_MONO,
        AudioFormat.ENCODING_PCM_16BIT
    )

    private val audioRecord = AudioRecord(
        MediaRecorder.AudioSource.MIC,
        sampleRate,
        AudioFormat.CHANNEL_IN_MONO,
        AudioFormat.ENCODING_PCM_16BIT,
        minBufferSize * 2
    )

    @Volatile private var recording = false
    private var samplesWritten: Long = 0

    init {
        val format = MediaFormat.createAudioFormat(MediaFormat.MIMETYPE_AUDIO_AAC, sampleRate, 1).apply {
            setInteger(MediaFormat.KEY_AAC_PROFILE, MediaCodecInfo.CodecProfileLevel.AACObjectLC)
            setInteger(MediaFormat.KEY_BIT_RATE, 128_000)
        }
        codec.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)

        codec.setCallback(object : MediaCodec.Callback() {
            override fun onInputBufferAvailable(codec: MediaCodec, index: Int) {
                if (!recording) return
                val buffer = codec.getInputBuffer(index) ?: return
                val bytesRead = audioRecord.read(buffer, buffer.capacity())
                if (bytesRead > 0) {
                    val presentationTimeUs = (samplesWritten * 1_000_000L) / sampleRate
                    samplesWritten += bytesRead / 2
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
                            presentationTimeUs = ptsAdjuster.adjust(info.presentationTimeUs),
                            isKeyFrame = true
                        )
                        distributor.distribute(sample)
                    }
                }
                codec.releaseOutputBuffer(index, false)
            }

            override fun onOutputFormatChanged(codec: MediaCodec, format: MediaFormat) {
                muxer.addTrack(TrackType.AUDIO, format)
            }

            override fun onError(codec: MediaCodec, e: MediaCodec.CodecException) {
                throw e
            }
        }, handler)
    }

    fun start() {
        recording = true
        audioRecord.startRecording()
        codec.start()
    }

    fun stop() {
        recording = false
        audioRecord.stop()
        audioRecord.release()
        codec.stop()
        codec.release()
        handlerThread.quitSafely()
    }
}
