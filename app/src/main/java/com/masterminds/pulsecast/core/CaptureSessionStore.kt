package com.masterminds.pulsecast.core

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

/** Shared lifecycle state published by the capture foreground service. */
object CaptureSessionStore {
    private val mutableRecording = MutableStateFlow(false)
    val isRecording: StateFlow<Boolean> = mutableRecording.asStateFlow()

    private val mutableBroadcasting = MutableStateFlow(false)
    val isBroadcasting: StateFlow<Boolean> = mutableBroadcasting.asStateFlow()

    private val mutableDurationSeconds = MutableStateFlow(0L)
    val recordingDurationSeconds: StateFlow<Long> = mutableDurationSeconds.asStateFlow()

    fun setRecording(recording: Boolean) {
        mutableRecording.value = recording
        if (!recording) {
            mutableBroadcasting.value = false
            mutableDurationSeconds.value = 0L
        }
    }

    fun setBroadcasting(broadcasting: Boolean) {
        mutableBroadcasting.value = broadcasting
    }

    fun updateDurationSeconds(seconds: Long) {
        mutableDurationSeconds.value = seconds
    }

    fun getFormattedDuration(seconds: Long = mutableDurationSeconds.value): String {
        val minutes = seconds / 60
        val remSeconds = seconds % 60
        return if (minutes >= 60) {
            val hours = minutes / 60
            val remMinutes = minutes % 60
            String.format(Locale.US, "%02d:%02d:%02d", hours, remMinutes, remSeconds)
        } else {
            String.format(Locale.US, "%02d:%02d", minutes, remSeconds)
        }
    }
}
