package com.masterminds.pulsecast.streaming

import java.nio.ByteBuffer

/**
 * What this app needs from an RTMP publisher, and nothing more. Deliberately
 * NOT coupled to any specific library's API (RootEncoder or otherwise) —
 * defining our own interface here means:
 *
 *  1. RtmpDestinationSink, the state-machine wiring, and NAL parsing can
 *     all be written and tested against this interface with a fake
 *     implementation, independent of which concrete library backs it.
 *  2. If the concrete library's actual API doesn't fit well once verified
 *     in Android Studio, or a different library turns out to be a better
 *     fit, only one small adapter class needs to change — nothing else in
 *     the app is coupled to it.
 *
 * See RootEncoderRtmpPublisher for the real Android+library-specific
 * implementation, and its doc comment for what specifically needs
 * verifying against the library's current version.
 */
interface RawRtmpPublisher {
    fun connect(url: String, onConnected: () -> Unit, onDisconnected: (String) -> Unit)
    fun sendVideoConfig(sps: ByteArray, pps: ByteArray)
    fun sendVideoFrame(data: ByteBuffer, presentationTimeUs: Long, isKeyFrame: Boolean)
    fun sendAudioConfig(config: ByteArray, sampleRate: Int, channelCount: Int)
    fun sendAudioFrame(data: ByteBuffer, presentationTimeUs: Long)
    fun disconnect()
}

/** Receives codec metadata needed to configure an encoded-audio RTMP track. */
interface AudioFormatAwareSink {
    fun setAudioConfig(config: ByteArray, sampleRate: Int, channelCount: Int)
}
