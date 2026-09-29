package com.masterminds.pulsecast.core

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** Shared lifecycle state published by the capture foreground service. */
object CaptureSessionStore {
    private val mutableRecording = MutableStateFlow(false)
    val isRecording: StateFlow<Boolean> = mutableRecording.asStateFlow()
    private val mutableBroadcasting = MutableStateFlow(false)
    val isBroadcasting: StateFlow<Boolean> = mutableBroadcasting.asStateFlow()

    fun setRecording(recording: Boolean) {
        mutableRecording.value = recording
        if (!recording) mutableBroadcasting.value = false
    }

    fun setBroadcasting(broadcasting: Boolean) {
        mutableBroadcasting.value = broadcasting
    }
}
