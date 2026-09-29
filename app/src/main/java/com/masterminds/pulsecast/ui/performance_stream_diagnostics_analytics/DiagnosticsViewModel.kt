package com.masterminds.pulsecast.ui.performance_stream_diagnostics_analytics

import android.app.Application
import android.content.Context
import android.os.BatteryManager
import android.os.Build
import android.os.PowerManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

class DiagnosticsViewModel(application: Application) : AndroidViewModel(application) {
    private val _fps = MutableStateFlow(60f)
    val fps: StateFlow<Float> = _fps.asStateFlow()

    private val _bitrateMbps = MutableStateFlow(6.0f)
    val bitrateMbps: StateFlow<Float> = _bitrateMbps.asStateFlow()

    private val _socTempC = MutableStateFlow(38f)
    val socTempC: StateFlow<Float> = _socTempC.asStateFlow()

    private val _batteryPct = MutableStateFlow(100)
    val batteryPct: StateFlow<Int> = _batteryPct.asStateFlow()

    private val _fpsHistory = MutableStateFlow<List<Float>>(emptyList())
    val fpsHistory: StateFlow<List<Float>> = _fpsHistory.asStateFlow()

    private val _bitrateHistory = MutableStateFlow<List<Float>>(emptyList())
    val bitrateHistory: StateFlow<List<Float>> = _bitrateHistory.asStateFlow()

    private val _optimizeEvent = MutableSharedFlow<String>()
    val optimizeEvent: SharedFlow<String> = _optimizeEvent.asSharedFlow()

    private var monitorJob: Job? = null

    fun startMonitoring() {
        if (monitorJob != null) return
        monitorJob = viewModelScope.launch(Dispatchers.Default) {
            while (isActive) {
                _fps.value = (60f + (-2..2).random()).coerceIn(10f, 120f)
                _bitrateMbps.value = (6.0f + (-5..5).random() / 10f).coerceAtLeast(0.5f)
                
                val context = getApplication<Application>()
                
                val batteryManager = context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager
                _batteryPct.value = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)

                if (Build.VERSION.SDK_INT >= 29) {
                    val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
                    _socTempC.value = powerManager.currentThermalStatus.toFloat()
                } else {
                    _socTempC.value = (38..45).random().toFloat()
                }

                _fpsHistory.value = (_fpsHistory.value + _fps.value).takeLast(60)
                _bitrateHistory.value = (_bitrateHistory.value + _bitrateMbps.value).takeLast(60)

                delay(1000)
            }
        }
    }

    fun stopMonitoring() {
        monitorJob?.cancel()
        monitorJob = null
    }

    fun autoOptimize() {
        viewModelScope.launch {
            if (_fps.value < 55 || _socTempC.value >= 1f) {
                val newBitrate = _bitrateMbps.value * 0.6f
                _bitrateMbps.value = newBitrate
                _optimizeEvent.emit("Bitrate reduced to ${String.format("%.1f", newBitrate)} Mbps for thermal headroom")
            }
        }
    }
}
