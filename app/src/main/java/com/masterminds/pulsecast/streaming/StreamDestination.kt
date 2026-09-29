package com.masterminds.pulsecast.streaming

/**
 * One target to stream to — e.g. YouTube, Twitch, Facebook, each with its
 * own RTMP ingest URL (stream key is typically embedded in the URL path,
 * matching how these platforms issue RTMP endpoints).
 */
data class StreamDestination(
    val id: String,
    val label: String,
    val rtmpUrl: String,
    val bitrateKbps: Int? = null,
)

/**
 * Connection lifecycle for a single destination. Deliberately per-
 * destination, not a single aggregate "is streaming" boolean — the whole
 * point of multi-destination streaming is that Twitch dropping its
 * connection shouldn't affect YouTube's, and the state needs to reflect
 * that independently for each one.
 */
sealed class ConnectionState {
    object Idle : ConnectionState()
    object Connecting : ConnectionState()
    object Live : ConnectionState()
    data class Reconnecting(val attempt: Int, val delayMs: Long) : ConnectionState()
    data class Failed(val reason: String) : ConnectionState()
}
