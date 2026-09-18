package com.masterminds.pulsecast.core

/**
 * The recording lifecycle as an explicit state machine, deliberately with
 * zero Android dependencies so it's unit-testable in plain Kotlin.
 *
 * Why this matters: a huge share of screen-recorder crashes come from
 * calling stop()/pause() from a state that doesn't support it (e.g.
 * pausing an already-stopped recorder, or double-starting after a
 * MediaProjection callback fires twice). Routing every transition through
 * one place that rejects invalid moves — instead of scattering ad-hoc
 * "if (isRecording)" checks across Activity/Service code — is what
 * prevents that whole category of bug.
 */
enum class RecordingState {
    IDLE,
    PERMISSION_REQUESTED,
    RECORDING,
    PAUSED,
    STOPPING,
}

sealed class RecordingEvent {
    object PermissionGranted : RecordingEvent()
    object PermissionDenied : RecordingEvent()
    object Start : RecordingEvent()
    object Pause : RecordingEvent()
    object Resume : RecordingEvent()
    object Stop : RecordingEvent()
    object Finished : RecordingEvent()
}

class InvalidTransitionException(from: RecordingState, event: RecordingEvent) :
    IllegalStateException("Cannot handle $event while in state $from")

class RecordingStateMachine(private var state: RecordingState = RecordingState.IDLE) {

    fun current(): RecordingState = state

    /**
     * Returns the new state, or throws [InvalidTransitionException] if the
     * event doesn't make sense from the current state. Throwing (rather
     * than silently ignoring) is deliberate — an invalid transition attempt
     * usually means a real bug upstream (e.g. a stale button click after
     * the service already died), and surfacing it during development is
     * far better than papering over it and corrupting a recording silently.
     */
    fun transition(event: RecordingEvent): RecordingState {
        val next = when (state to event) {
            RecordingState.IDLE to RecordingEvent.Start ->
                RecordingState.PERMISSION_REQUESTED

            RecordingState.PERMISSION_REQUESTED to RecordingEvent.PermissionGranted ->
                RecordingState.RECORDING

            RecordingState.PERMISSION_REQUESTED to RecordingEvent.PermissionDenied ->
                RecordingState.IDLE

            RecordingState.RECORDING to RecordingEvent.Pause ->
                RecordingState.PAUSED

            RecordingState.RECORDING to RecordingEvent.Stop ->
                RecordingState.STOPPING

            RecordingState.PAUSED to RecordingEvent.Resume ->
                RecordingState.RECORDING

            RecordingState.PAUSED to RecordingEvent.Stop ->
                RecordingState.STOPPING

            RecordingState.STOPPING to RecordingEvent.Finished ->
                RecordingState.IDLE

            else -> throw InvalidTransitionException(state, event)
        }
        state = next
        return next
    }

    fun canPause(): Boolean = state == RecordingState.RECORDING
    fun canResume(): Boolean = state == RecordingState.PAUSED
    fun canStop(): Boolean = state == RecordingState.RECORDING || state == RecordingState.PAUSED
    fun isActive(): Boolean = state == RecordingState.RECORDING || state == RecordingState.PAUSED
}
