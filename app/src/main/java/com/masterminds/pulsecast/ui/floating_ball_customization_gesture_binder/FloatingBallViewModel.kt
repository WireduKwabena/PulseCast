package com.masterminds.pulsecast.ui.floating_ball_customization_gesture_binder

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.masterminds.pulsecast.core.FloatingBallPrefsRepository
import com.masterminds.pulsecast.core.OrbConfiguration
import com.masterminds.pulsecast.core.OrbConfigurationStore
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class GestureBinding(val gesture: String, val action: String)

class FloatingBallViewModel(application: Application) : AndroidViewModel(application) {
    private val repo = FloatingBallPrefsRepository(application)

    private val _gestureBindings = MutableStateFlow(listOf(
        GestureBinding("SINGLE_TAP", "RADIAL_MENU"),
        GestureBinding("DOUBLE_TAP", "AI_CLIP"),
        GestureBinding("LONG_PRESS", "MIC_MUTE"),
        GestureBinding("SWIPE_IN", "OPEN_CHAT")
    ))
    val gestureBindings: StateFlow<List<GestureBinding>> = _gestureBindings.asStateFlow()

    private val _idleOpacity = MutableStateFlow(0.35f)
    val idleOpacity: StateFlow<Float> = _idleOpacity.asStateFlow()

    private val _activeOpacity = MutableStateFlow(1.0f)
    val activeOpacity: StateFlow<Float> = _activeOpacity.asStateFlow()

    private val _dockingMode = MutableStateFlow("MAGNETIC")
    val dockingMode: StateFlow<String> = _dockingMode.asStateFlow()

    private val _selectedSkin = MutableStateFlow("CYBER_RED")
    val selectedSkin: StateFlow<String> = _selectedSkin.asStateFlow()

    private val _configSaved = MutableSharedFlow<Unit>()
    val configSaved: SharedFlow<Unit> = _configSaved.asSharedFlow()

    init {
        viewModelScope.launch {
            repo.orbConfigurationFlow.collect { config ->
                _idleOpacity.value = config.idleOpacity
                _activeOpacity.value = config.activeOpacity
                _dockingMode.value = config.dockingMode
                _selectedSkin.value = config.skin
                _gestureBindings.value = listOf(
                    GestureBinding("SINGLE_TAP", config.gestureActions["SINGLE_TAP"] ?: "RADIAL_MENU"),
                    GestureBinding("DOUBLE_TAP", config.gestureActions["DOUBLE_TAP"] ?: "AI_CLIP"),
                    GestureBinding("LONG_PRESS", config.gestureActions["LONG_PRESS"] ?: "MIC_MUTE"),
                    GestureBinding("SWIPE_IN", config.gestureActions["SWIPE_IN"] ?: "OPEN_CHAT")
                )
                OrbConfigurationStore.update(config)
            }
        }
    }

    fun setGestureBinding(gesture: String, action: String) {
        _gestureBindings.update { list -> list.map { if (it.gesture == gesture) it.copy(action = action) else it } }
    }
    fun setIdleOpacity(v: Float) { _idleOpacity.value = v }
    fun setActiveOpacity(v: Float) { _activeOpacity.value = v }
    fun setDockingMode(m: String) { _dockingMode.value = m }
    fun setSkin(s: String) { _selectedSkin.value = s }

    fun applyConfiguration() {
        val config = OrbConfiguration(
            idleOpacity = _idleOpacity.value,
            activeOpacity = _activeOpacity.value,
            dockingMode = _dockingMode.value,
            skin = _selectedSkin.value,
            gestureActions = _gestureBindings.value.associate { it.gesture to it.action }
        )
        OrbConfigurationStore.update(config)
        viewModelScope.launch {
            repo.saveOrbConfiguration(config)
            _configSaved.emit(Unit)
        }
    }
}
