package com.masterminds.pulsecast.streaming

import com.masterminds.pulsecast.core.TrackType
import com.masterminds.pulsecast.core.CaptureSessionStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

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
) : EncodedSampleSink, AudioFormatAwareSink {

    private var videoConfigSent = false
    private var audioConfigSent = false
    private var pendingAudioConfig: ByteArray? = null
    private var pendingAudioSampleRate: Int = 44_100
    private var pendingAudioChannelCount: Int = 1
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var retryJob: Job? = null
    @Volatile private var stopped = false

    fun start() {
        stopped = false
        connection.connect()
        publishState()
        connectPublisher()
    }

    private fun connectPublisher() {
        try {
            publisher.connect(
                connection.destination.rtmpUrl,
                onConnected = {
                    if (!stopped) {
                        runCatching { connection.onConnected() }
                        publishState()
                    }
                },
                onDisconnected = ::handleDisconnected
            )
        } catch (error: Exception) {
            // Isolate expected library/transport failures from local recording
            // without swallowing fatal VM errors such as OutOfMemoryError.
            handleDisconnected(error.message ?: "Failed to start connection")
        }
    }

    private fun handleDisconnected(reason: String) {
        if (stopped) return
        val previous = connection.state
        if (previous !is ConnectionState.Connecting && previous !is ConnectionState.Live) return
        runCatching { connection.onDisconnected(reason) }
        publishState()
        val reconnecting = connection.state as? ConnectionState.Reconnecting ?: return
        retryJob?.cancel()
        retryJob = scope.launch {
            delay(reconnecting.delayMs)
            if (stopped) return@launch
            runCatching { publisher.disconnect() }
            if (stopped || connection.state !is ConnectionState.Reconnecting) return@launch
            videoConfigSent = false
            audioConfigSent = false
            runCatching {
                connection.retryNow()
                publishState()
                connectPublisher()
            }.onFailure { handleDisconnected(it.message ?: "Reconnect failed") }
        }
    }

    private fun publishState() {
        BroadcastDestinationStore.setConnectionState(connection.destination.id, connection.state)
        CaptureSessionStore.setBroadcasting(
            BroadcastDestinationStore.connectionStates.value.values.any { it is ConnectionState.Live }
        )
    }

    fun stop() {
        stopped = true
        retryJob?.cancel()
        retryJob = null
        publisher.disconnect()
        connection.stop()
        publishState()
        scope.cancel()
        videoConfigSent = false
        audioConfigSent = false
    }

    /** Call once, before any audio samples arrive, with the AAC config bytes (MediaFormat's csd-0). */
    override fun setAudioConfig(config: ByteArray, sampleRate: Int, channelCount: Int) {
        pendingAudioConfig = config
        pendingAudioSampleRate = sampleRate
        pendingAudioChannelCount = channelCount
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
                publisher.sendAudioConfig(it, pendingAudioSampleRate, pendingAudioChannelCount)
                audioConfigSent = true
            }
        }
        publisher.sendAudioFrame(sample.data, sample.presentationTimeUs)
    }
}
