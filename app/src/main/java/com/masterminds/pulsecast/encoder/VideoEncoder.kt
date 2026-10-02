package com.masterminds.pulsecast.encoder

import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaFormat
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.util.Log
import android.view.Surface
import com.masterminds.pulsecast.core.PtsAdjuster
import com.masterminds.pulsecast.core.Resolution
import com.masterminds.pulsecast.core.TrackType
import com.masterminds.pulsecast.streaming.EncodedSample
import com.masterminds.pulsecast.streaming.EncodedSampleSink
import com.masterminds.pulsecast.streaming.FanOutDistributor

/**
 * Encodes video via MediaCodec's Surface-input mode: the VirtualDisplay
 * renders directly into [inputSurface], so there's no manual pixel copying
 * between capture and encode.
 *
 * Runs in async/callback mode on a dedicated HandlerThread rather than a
 * busy-polling dequeue loop.
 *
 * Phase 3 change: output now fans out to a list of EncodedSampleSink
 * (local file muxer AND any live RTMP destinations) via FanOutDistributor,
 * instead of writing to a single MuxerWrapper directly — one encode,
 * multiple simultaneous outputs. addTrack() still goes straight to
 * [muxer] though; MediaFormat deliberately never enters the generic sink
 * interface (see MuxerSink's doc comment for why).
 */
class VideoEncoder(
    resolution: Resolution,
    frameRate: Int,
    bitRate: Int,
    private val muxer: MuxerWrapper,
    sinks: List<EncodedSampleSink>,
    private val ptsAdjuster: PtsAdjuster,
) {
    private val codec: MediaCodec = MediaCodec.createEncoderByType(MediaFormat.MIMETYPE_VIDEO_AVC)
    val inputSurface: Surface
    private val handlerThread = HandlerThread("VideoEncoderCallback").apply { start() }
    private val handler = Handler(handlerThread.looper)
    private val distributor = FanOutDistributor(sinks)

    init {
        val format = MediaFormat.createVideoFormat(
            MediaFormat.MIMETYPE_VIDEO_AVC,
            resolution.width,
            resolution.height
        ).apply {
            setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface)
            setInteger(MediaFormat.KEY_BIT_RATE, bitRate)
            setInteger(MediaFormat.KEY_FRAME_RATE, frameRate)
            setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 2)
        }

        codec.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
        inputSurface = codec.createInputSurface()

        codec.setCallback(object : MediaCodec.Callback() {
            override fun onInputBufferAvailable(codec: MediaCodec, index: Int) {
                // Not used in Surface-input mode.
            }

            override fun onOutputBufferAvailable(
                codec: MediaCodec,
                index: Int,
                info: MediaCodec.BufferInfo
            ) {
                if (info.size > 0 && info.flags and MediaCodec.BUFFER_FLAG_CODEC_CONFIG == 0) {
                    val buffer = codec.getOutputBuffer(index)
                    if (buffer != null) {
                        buffer.position(info.offset)
                        buffer.limit(info.offset + info.size)
                        val sample = EncodedSample(
                            track = TrackType.VIDEO,
                            data = buffer,
                            presentationTimeUs = ptsAdjuster.adjust(info.presentationTimeUs),
                            isKeyFrame = info.flags and MediaCodec.BUFFER_FLAG_KEY_FRAME != 0
                        )
                        distributor.distribute(sample)
                    }
                }
                codec.releaseOutputBuffer(index, false)
            }

            override fun onOutputFormatChanged(codec: MediaCodec, format: MediaFormat) {
                muxer.addTrack(TrackType.VIDEO, format)
            }

            override fun onError(codec: MediaCodec, e: MediaCodec.CodecException) {
                Log.e("VideoEncoder", "Video MediaCodec error: ${e.diagnosticInfo}", e)
            }
        }, handler)
    }

    fun start() = codec.start()

    /**
     * Dynamically updates the encoder bitrate on the fly without stopping the stream.
     * Used by the Adaptive Bitrate (ABR) auto-optimizer during network congestion or thermal throttling.
     */
    fun setBitrate(newBitRate: Int) {
        val params = Bundle().apply {
            putInt(MediaCodec.PARAMETER_KEY_VIDEO_BITRATE, newBitRate)
        }
        codec.setParameters(params)
    }

    fun stop() {
        runCatching { codec.signalEndOfInputStream() }
        runCatching { codec.stop() }
        runCatching { codec.release() }
        runCatching { handlerThread.quitSafely() }
    }
}
