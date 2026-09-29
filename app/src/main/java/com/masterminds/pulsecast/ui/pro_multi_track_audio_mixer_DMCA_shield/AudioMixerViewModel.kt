package com.masterminds.pulsecast.ui.pro_multi_track_audio_mixer_DMCA_shield

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ChannelState(val name: String, val gainDb: Float, val pan: Float, val muted: Boolean, val color: Long)

class AudioMixerViewModel : ViewModel() {
    private val _channels = MutableStateFlow(listOf(
        ChannelState("Game", 0f, 0f, false, 0xFF00FFFF), // CyberCyan
        ChannelState("Mic", 0f, 0f, false, 0xFFFF003C),  // ElectricRuby
        ChannelState("Discord", 0f, 0f, false, 0xFF00FF00), // SignalGreen
        ChannelState("Music", 0f, 0f, false, 0xFFFFB300) // NeonAmber
    ))
    val channels: StateFlow<List<ChannelState>> = _channels.asStateFlow()

    private val _masterGainDb = MutableStateFlow(0f)
    val masterGainDb: StateFlow<Float> = _masterGainDb.asStateFlow()

    private val _dmcaShieldEnabled = MutableStateFlow(true)
    val dmcaShieldEnabled: StateFlow<Boolean> = _dmcaShieldEnabled.asStateFlow()

    private val _smartDuckingEnabled = MutableStateFlow(false)
    val smartDuckingEnabled: StateFlow<Boolean> = _smartDuckingEnabled.asStateFlow()

    private val _smartDuckingThreshold = MutableStateFlow(0.4f)
    val smartDuckingThreshold: StateFlow<Float> = _smartDuckingThreshold.asStateFlow()

    private val _profileApplied = MutableSharedFlow<Unit>()
    val profileApplied: SharedFlow<Unit> = _profileApplied.asSharedFlow()

    fun setChannelGain(index: Int, db: Float) {
        _channels.update { list -> list.mapIndexed { i, c -> if (i == index) c.copy(gainDb = db) else c } }
    }
    fun setChannelPan(index: Int, pan: Float) {
        _channels.update { list -> list.mapIndexed { i, c -> if (i == index) c.copy(pan = pan) else c } }
    }
    fun toggleChannelMute(index: Int) {
        _channels.update { list -> list.mapIndexed { i, c -> if (i == index) c.copy(muted = !c.muted) else c } }
    }
    fun setMasterGain(db: Float) { _masterGainDb.value = db }
    fun toggleDmcaShield() { _dmcaShieldEnabled.value = !_dmcaShieldEnabled.value }
    fun toggleSmartDucking() { _smartDuckingEnabled.value = !_smartDuckingEnabled.value }
    fun setDuckingThreshold(v: Float) { _smartDuckingThreshold.value = v }
    
    fun applyProfile() {
        viewModelScope.launch {
            _profileApplied.emit(Unit)
        }
    }
}
