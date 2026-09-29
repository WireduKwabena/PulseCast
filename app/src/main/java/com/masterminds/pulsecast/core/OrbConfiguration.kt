package com.masterminds.pulsecast.core

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class OrbConfiguration(
    val idleOpacity: Float = 0.35f,
    val activeOpacity: Float = 1f,
    val dockingMode: String = "MAGNETIC",
    val skin: String = "CYBER_RED",
    val gestureActions: Map<String, String> = mapOf(
        "SINGLE_TAP" to "RADIAL_MENU",
        "DOUBLE_TAP" to "AI_CLIP",
        "LONG_PRESS" to "MIC_MUTE",
        "SWIPE_IN" to "OPEN_CHAT"
    )
)

/** Process-wide state bridge between the settings UI and the active overlay service. */
object OrbConfigurationStore {
    private val mutableConfiguration = MutableStateFlow(OrbConfiguration())
    val configuration: StateFlow<OrbConfiguration> = mutableConfiguration.asStateFlow()

    fun update(configuration: OrbConfiguration) {
        mutableConfiguration.value = configuration.copy(
            idleOpacity = configuration.idleOpacity.coerceIn(0f, 1f),
            activeOpacity = configuration.activeOpacity.coerceIn(0f, 1f)
        )
    }
}
