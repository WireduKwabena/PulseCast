package com.masterminds.pulsecast.streaming

import com.masterminds.pulsecast.core.TrackType

/**
 * One RTMP destination as an EncodedSampleSink — slots into
 * FanOutDistributor's sink list exactly like the local file save
 * (MuxerSink) does. Owns its own DestinationConnection state machine, so a
 * failure/reconnect here has zero effect on any other sink in the same
 * fan-out list.
 *
 * Config (SPS/PPS for video, AAC config for audio) is sent exactly once —
 * extracted from the first keyframe seen after connecting for video, and
 * from an explicitly-provided config for audio (AAC's config isn't
 * embedded in the bitstream itself the way H.264's SPS/PPS are, so it has
 * to come from MediaFormat's csd-0 buffer instead — see setAudioConfig).
 */
class RtmpDestinationSink(
    val connection: DestinationConnection,
    private val publisher: RawRtmpPublisher,
) : EncodedSampleSink {

    private var videoConfigSent = false
    private var audioConfigSent = false
    private var pendingAudioConfig: ByteArray? = null

    fun start() {
        connection.connect()
        try {
            publisher.connect(
                connection.destination.rtmpUrl,
                onConnected = { connection.onConnected() },
                onDisconnected = { reason -> connection.onDisconnected(reason) }
            )
        } catch (t: Throwable) {
            // Catching Throwable, not just Exception, deliberately — this
            // is a boundary to an external/unverified library implementation
            // (see RootEncoderRtmpPublisher's TODO()s, which throw
            // NotImplementedError, an Error subtype that a plain
            // `catch (e: Exception)` would NOT catch). One destination's
            // publisher misbehaving in any way must never crash the whole
            // recording pipeline that other destinations and local saving
            // both depend on.
            connection.onDisconnected(t.message ?: "Failed to start connection")
        }
    }

    fun stop() {
        publisher.disconnect()
        connection.stop()
        videoConfigSent = false
        audioConfigSent = false
    }

    /** Call once, before any audio samples arrive, with the AAC config bytes (MediaFormat's csd-0). */
    fun setAudioConfig(config: ByteArray) {
        pendingAudioConfig = config
    }

    override fun onSample(sample: EncodedSample) {
        // Don't waste frames on a dead connection — they'd just be dropped
        // by the publisher anyway, and skipping here avoids doing the
        // SPS/PPS scan on every keyframe of a connection that isn't live.
        if (!connection.isLive()) return

        when (sample.track) {
            TrackType.VIDEO -> handleVideoSample(sample)
            TrackType.AUDIO -> handleAudioSample(sample)
        }
    }

    private fun handleVideoSample(sample: EncodedSample) {
        if (!videoConfigSent && sample.isKeyFrame) {
            val bytes = ByteArray(sample.data.remaining())
            sample.data.duplicate().get(bytes)
            val (sps, pps) = NalUnitParser.findParameterSets(bytes)
            if (sps != null && pps != null) {
                publisher.sendVideoConfig(sps, pps)
                videoConfigSent = true
            }
            // If sps/pps aren't found on this particular keyframe — shouldn't
            // normally happen, MediaCodec emits them with every keyframe —
            // fall through and still send the frame; retry extraction on
            // the next keyframe instead of dropping video entirely.
        }
        publisher.sendVideoFrame(sample.data, sample.presentationTimeUs, sample.isKeyFrame)
    }

    private fun handleAudioSample(sample: EncodedSample) {
        if (!audioConfigSent) {
            pendingAudioConfig?.let {
                publisher.sendAudioConfig(it)
                audioConfigSent = true
            }
        }
        publisher.sendAudioFrame(sample.data, sample.presentationTimeUs)
    }
}
