package com.masterminds.pulsecast.streaming

/**
 * Per-destination connection state machine. Each StreamDestination gets
 * its own independent instance — a failure or reconnect cycle on one
 * destination has zero effect on any other's state, which is the entire
 * point of streaming to multiple platforms at once rather than treating
 * "am I live" as one shared boolean.
 */
class DestinationConnection(val destination: StreamDestination) {

    var state: ConnectionState = ConnectionState.Idle
        private set

    private var attempt = 0

    fun connect() {
        check(state is ConnectionState.Idle || state is ConnectionState.Failed) {
            "Cannot connect() from state $state"
        }
        attempt = 0
        state = ConnectionState.Connecting
    }

    fun onConnected() {
        check(state is ConnectionState.Connecting) {
            "onConnected() called from unexpected state $state"
        }
        attempt = 0
        state = ConnectionState.Live
    }

    /** Called when the RTMP connection drops, whether during initial connect or after going live. */
    fun onDisconnected(reason: String) {
        check(state is ConnectionState.Connecting || state is ConnectionState.Live) {
            "onDisconnected() called from unexpected state $state"
        }
        attempt++
        state = if (ReconnectBackoff.shouldGiveUp(attempt)) {
            ConnectionState.Failed(reason)
        } else {
            ConnectionState.Reconnecting(attempt, ReconnectBackoff.delayForAttempt(attempt))
        }
    }

    /** Called by the scheduler once a Reconnecting state's delay has elapsed. */
    fun retryNow() {
        check(state is ConnectionState.Reconnecting) {
            "retryNow() called from unexpected state $state"
        }
        state = ConnectionState.Connecting
    }

    fun stop() {
        attempt = 0
        state = ConnectionState.Idle
    }

    fun isLive(): Boolean = state is ConnectionState.Live
}
