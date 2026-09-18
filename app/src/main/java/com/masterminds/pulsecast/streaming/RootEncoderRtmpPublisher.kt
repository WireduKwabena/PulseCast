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
 * Method signatures below are a mix of two confidence levels — noted
 * per-method, since this was the one file in the project I genuinely
 * couldn't fully verify by compiling against the real dependency in this
 * environment:
 *
 *  - VERIFIED against a real, recent (2.6.6) migration code sample from
 *    the library's own issue tracker: setVideoInfo(sps, pps, vps) and
 *    sendVideo(buffer, info) — see
 *    https://github.com/pedroSG94/RootEncoder/issues/1991
 *  - VERIFIED that RtmpClient.connect(...) exists and ConnectChecker is
 *    the current unified callback interface, via a real stack trace
 *    (com.pedro.rtmp.rtmp.RtmpClient.connect$lambda-0) and a real usage
 *    site (`class ShareScreenService : Service(), ConnectChecker`) — see
 *    https://github.com/pedroSG94/RootEncoder/issues/971 and
 *    https://github.com/pedroSG94/RootEncoder/issues/2014
 *  - INFERRED by direct symmetry with the verified video methods, not
 *    independently confirmed: setAudioInfo(sampleRate, isStereo) and
 *    sendAudio(buffer, info). GenericStream's prepareAudio(sampleRate,
 *    isStereo, bitrate) at the higher level strongly suggests this shape,
 *    but double-check these two specifically first if something doesn't
 *    compile.
 */
class RootEncoderRtmpPublisher : RawRtmpPublisher {

    companion object {
        private const val TAG = "RootEncoderRtmpPublisher"
        // AAC-LC mono at 44.1kHz to match AudioEncoder's actual output —
        // update both together if AudioEncoder's config ever changes.
        private const val AUDIO_SAMPLE_RATE = 44_100
        private const val AUDIO_IS_STEREO = false
    }

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

    override fun sendAudioConfig(config: ByteArray) {
        // INFERRED signature — see class doc comment. config (MediaFormat's
        // csd-0 for AAC) isn't actually used in this guessed call shape;
        // if the real API wants the raw AAC config bytes instead of
        // sampleRate/isStereo, this is the line to fix.
        client?.setAudioInfo(AUDIO_SAMPLE_RATE, AUDIO_IS_STEREO)
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
