package com.masterminds.pulsecast.core

import android.util.Log

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

    @Synchronized
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

            // Safe idempotent fallbacks for duplicate events:
            RecordingState.STOPPING to RecordingEvent.Stop ->
                RecordingState.STOPPING

            RecordingState.IDLE to RecordingEvent.Finished ->
                RecordingState.IDLE

            RecordingState.IDLE to RecordingEvent.Stop ->
                RecordingState.IDLE

            else -> {
                Log.w("RecordingStateMachine", "Ignoring unexpected transition $event from state $state")
                state
            }
        }
        state = next
        return next
    }

    fun canPause(): Boolean = state == RecordingState.RECORDING
    fun canResume(): Boolean = state == RecordingState.PAUSED
    fun canStop(): Boolean = state == RecordingState.RECORDING || state == RecordingState.PAUSED
    fun isActive(): Boolean = state == RecordingState.RECORDING || state == RecordingState.PAUSED
}
