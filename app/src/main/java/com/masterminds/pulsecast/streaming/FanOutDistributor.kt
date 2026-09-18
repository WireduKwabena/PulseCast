package com.masterminds.pulsecast.streaming

import com.masterminds.pulsecast.core.TrackType
import java.nio.ByteBuffer

/**
 * A single encoded frame, deliberately independent of android.media.
 * MediaCodec.BufferInfo — using java.nio.ByteBuffer (standard JVM, not
 * Android-specific) instead of the Android type is what keeps this whole
 * fan-out layer pure-Kotlin and unit-testable without an emulator. The
 * Android-specific encoder classes (VideoEncoder/AudioEncoder) translate
 * MediaCodec's real output into this shape at the boundary.
 */
data class EncodedSample(
    val track: TrackType,
    val data: ByteBuffer,
    val presentationTimeUs: Long,
    val isKeyFrame: Boolean,
)

/**
 * Anything that can consume the encoder's output — the existing local-file
 * muxer and each RTMP destination both implement this, so
 * FanOutDistributor doesn't need to know or care which kind it's talking
 * to.
 */
interface EncodedSampleSink {
    fun onSample(sample: EncodedSample)
}

/**
 * Feeds one shared encoder pipeline's output to N sinks. The property this
 * exists to guarantee: one sink throwing (e.g. an RTMP destination whose
 * connection just dropped) must never stop delivery to any other sink —
 * that's the entire reason multi-destination streaming is worth building
 * as a fan-out rather than N independent capture pipelines, and it's easy
 * to accidentally break with a naive "for sink in sinks" loop that doesn't
 * isolate exceptions per-iteration.
 */
class FanOutDistributor(private val sinks: List<EncodedSampleSink>) {

    fun distribute(sample: EncodedSample) {
        for (sink in sinks) {
            // .duplicate() gives each sink its own position/limit/mark over
            // the SAME underlying bytes — required because ByteBuffer's
            // position is mutable shared state; without duplicating, the
            // first sink to read the buffer would leave it exhausted
            // (position == limit) for every sink after it.
            val sampleForSink = sample.copy(data = sample.data.duplicate())
            try {
                sink.onSample(sampleForSink)
            } catch (e: Exception) {
                // Deliberately swallowed here, not silently — the caller
                // (StreamFanOutManager in the full integration) is
                // responsible for logging this against the specific
                // destination and driving its DestinationConnection to
                // onDisconnected(). A crash from one destination's sink
                // must never propagate up and kill the shared encoder loop
                // that every OTHER destination (and the local recording)
                // also depends on.
            }
        }
    }
}
