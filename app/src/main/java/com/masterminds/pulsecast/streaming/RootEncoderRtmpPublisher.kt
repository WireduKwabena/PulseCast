package com.masterminds.pulsecast.streaming

import android.media.MediaCodec
import android.util.Log
import com.pedro.common.ConnectChecker
import com.pedro.rtmp.rtmp.RtmpClient
import java.nio.ByteBuffer

/**
 * Backs RawRtmpPublisher with RootEncoder's low-level RtmpClient
 * (com.pedro.rtmp.rtmp.RtmpClient) — not GenericStream/RtmpCamera1, which
 * are the higher-level classes most of the library's docs cover for
 * "capture from camera and stream." RtmpClient is the class actually
 * meant for feeding pre-encoded frames from an external source (like our
 * own MediaProjection + MediaCodec pipeline) directly, per the library
 * author's own guidance for exactly this use case:
 * https://github.com/pedroSG94/RootEncoder/discussions/1287
 *
 * The adapter is compiled against the project's resolved RootEncoder
 * 2.8.1 dependency. RTMP receives the app's already-encoded H.264/AAC
 * samples directly, avoiding a second capture/encode pipeline. RootEncoder
 * constructs the AAC sequence header from the actual codec sample rate and
 * channel layout; csd-0 is still required to confirm MediaCodec initialized
 * the AAC format before any audio packets are sent.
 */
class RootEncoderRtmpPublisher : RawRtmpPublisher {

    companion object { private const val TAG = "RootEncoderRtmpPublisher" }

    private var client: RtmpClient? = null

    override fun connect(url: String, onConnected: () -> Unit, onDisconnected: (String) -> Unit) {
        val checker = object : ConnectChecker {
            override fun onConnectionStarted(url: String) {
                // Informational only — no action needed.
            }

            override fun onConnectionSuccess() {
                onConnected()
            }

            override fun onConnectionFailed(reason: String) {
                Log.w(TAG, "RTMP connection failed: $reason")
                onDisconnected(reason)
            }

            override fun onNewBitrate(bitrate: Long) {
                // Not surfaced through RawRtmpPublisher's current interface —
                // would be a natural future addition (adaptive bitrate) but
                // out of scope for this phase.
            }

            override fun onDisconnect() {
                onDisconnected("Disconnected")
            }

            override fun onAuthError() {
                onDisconnected("RTMP authentication failed")
            }

            override fun onAuthSuccess() {
                // No action needed — onConnectionSuccess() still fires separately.
            }
        }

        val rtmpClient = RtmpClient(checker)
        client = rtmpClient
        rtmpClient.connect(url)
    }

    override fun sendVideoConfig(sps: ByteArray, pps: ByteArray) {
        // vps (3rd param) is for H265 only — always null here since
        // VideoEncoder always configures MediaFormat.MIMETYPE_VIDEO_AVC (H264).
        client?.setVideoInfo(ByteBuffer.wrap(sps), ByteBuffer.wrap(pps), null)
    }

    override fun sendVideoFrame(data: ByteBuffer, presentationTimeUs: Long, isKeyFrame: Boolean) {
        val info = MediaCodec.BufferInfo().apply {
            set(
                0,
                data.remaining(),
                presentationTimeUs,
                if (isKeyFrame) MediaCodec.BUFFER_FLAG_KEY_FRAME else 0
            )
        }
        client?.sendVideo(data, info)
    }

    override fun sendAudioConfig(config: ByteArray, sampleRate: Int, channelCount: Int) {
        require(config.isNotEmpty()) { "AAC encoder did not provide codec-specific data" }
        require(sampleRate > 0 && channelCount > 0) { "AAC output format is invalid" }
        // RootEncoder builds the RTMP AAC sequence header from sample rate
        // and channel layout; csd-0 is validated here as proof the codec
        // format event arrived before audio packets are sent.
        client?.setAudioInfo(sampleRate, channelCount > 1)
    }

    override fun sendAudioFrame(data: ByteBuffer, presentationTimeUs: Long) {
        val info = MediaCodec.BufferInfo().apply {
            set(0, data.remaining(), presentationTimeUs, 0)
        }
        client?.sendAudio(data, info)
    }

    override fun disconnect() {
        client?.disconnect()
        client = null
    }
}
