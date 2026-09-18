package com.masterminds.pulsecast.encoder

import android.media.MediaCodec
import com.masterminds.pulsecast.streaming.EncodedSample
import com.masterminds.pulsecast.streaming.EncodedSampleSink

/**
 * Adapts the existing (already-tested) MuxerWrapper to the generic
 * EncodedSampleSink interface, so local file saving becomes just another
 * sink in the same fan-out list as any RTMP destination — no special-
 * casing needed in VideoEncoder/AudioEncoder for "the local save" versus
 * "a stream target."
 *
 * MediaFormat/addTrack handling deliberately stays OUTSIDE this adapter
 * and the EncodedSampleSink interface entirely — VideoEncoder/AudioEncoder
 * still call muxer.addTrack() directly when the encoder's format changes.
 * Keeping android.media.MediaFormat out of EncodedSampleSink is what keeps
 * the whole streaming/ fan-out layer free of Android dependencies and
 * unit-testable (see StreamingTests.kt).
 */
class MuxerSink(private val muxer: MuxerWrapper) : EncodedSampleSink {
    override fun onSample(sample: EncodedSample) {
        val info = MediaCodec.BufferInfo().apply {
            set(
                0,
                sample.data.remaining(),
                sample.presentationTimeUs,
                if (sample.isKeyFrame) MediaCodec.BUFFER_FLAG_KEY_FRAME else 0
            )
        }
        muxer.writeSample(sample.track, sample.data, info)
    }
}
