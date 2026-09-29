package com.masterminds.pulsecast.ui.pre_stream_go_live_safety_checklist

import android.app.Application
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.BatteryManager
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlin.math.log10
import kotlin.math.sqrt

class PreFlightViewModel(application: Application) : AndroidViewModel(application) {

    private var diagnosticsJob: Job? = null
    private var microphoneJob: Job? = null
    private var batteryReceiver: BroadcastReceiver? = null
    @Volatile private var microphoneRecorder: AudioRecord? = null

    private val _batteryLevel = MutableStateFlow(100)
    val batteryLevel: StateFlow<Int> = _batteryLevel.asStateFlow()

    private val _batteryCharging = MutableStateFlow(false)
    val batteryCharging: StateFlow<Boolean> = _batteryCharging.asStateFlow()

    private val _thermalStatus = MutableStateFlow("GOOD")
    val thermalStatus: StateFlow<String> = _thermalStatus.asStateFlow()

    private val _networkQuality = MutableStateFlow("EXCELLENT")
    val networkQuality: StateFlow<String> = _networkQuality.asStateFlow()

    private val _micDbLevel = MutableStateFlow(-60f)
    val micDbLevel: StateFlow<Float> = _micDbLevel.asStateFlow()

    private val _dndGranted = MutableStateFlow(false)
    val dndGranted: StateFlow<Boolean> = _dndGranted.asStateFlow()

    private val _dndArmed = MutableStateFlow(false)
    val dndArmed: StateFlow<Boolean> = _dndArmed.asStateFlow()

    private val _overallReadiness = MutableStateFlow(0)
    val overallReadiness: StateFlow<Int> = _overallReadiness.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                batteryLevel,
                thermalStatus,
                networkQuality,
                micDbLevel,
                dndArmed
            ) { battery, thermal, network, micDb, dnd ->
                var score = 0
                if (battery >= 20) score += 20
                if (battery >= 50) score += 10
                if (thermal == "GOOD") score += 25
                if (network == "EXCELLENT" || network == "GOOD") score += 25
                if (micDb > -50f) score += 10
                if (dnd) score += 10
                score
            }.collect { score ->
                _overallReadiness.value = score
            }
        }
    }

    @Synchronized
    fun startDiagnostics() {
        if (diagnosticsJob?.isActive == true || microphoneJob?.isActive == true) return
        val context = getApplication<Application>().applicationContext

        batteryReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                intent?.let {
                    val level = it.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
                    val scale = it.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
                    if (level != -1 && scale != -1) {
                        _batteryLevel.value = (level * 100 / scale.toFloat()).toInt()
                    }
                    val status = it.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
                    _batteryCharging.value = status == BatteryManager.BATTERY_STATUS_CHARGING
                }
            }
        }
        runCatching { context.registerReceiver(batteryReceiver, IntentFilter(Intent.ACTION_BATTERY_CHANGED)) }

        diagnosticsJob = viewModelScope.launch(Dispatchers.Default) {
            val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
            val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            
            while (isActive) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    _thermalStatus.value = when (powerManager.currentThermalStatus) {
                        PowerManager.THERMAL_STATUS_NONE, PowerManager.THERMAL_STATUS_LIGHT -> "GOOD"
                        PowerManager.THERMAL_STATUS_MODERATE -> "MODERATE"
                        else -> "SEVERE"
                    }
                } else {
                    _thermalStatus.value = "GOOD"
                }

                val network = connectivityManager.activeNetwork
                val capabilities = connectivityManager.getNetworkCapabilities(network)
                if (capabilities != null && capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)) {
                    val bandwidth = capabilities.linkDownstreamBandwidthKbps
                    if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                        _networkQuality.value = if (bandwidth > 10000) "EXCELLENT" else "GOOD"
                    } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
                        _networkQuality.value = if (bandwidth > 5000) "GOOD" else "POOR"
                    } else {
                        _networkQuality.value = "POOR"
                    }
                } else {
                    _networkQuality.value = "POOR"
                }

                _dndGranted.value = notificationManager.isNotificationPolicyAccessGranted
                
                delay(2000)
            }
        }

        microphoneJob = viewModelScope.launch(Dispatchers.IO) {
            var recorder: AudioRecord? = null
            try {
                val minBuffer = AudioRecord.getMinBufferSize(16000, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT)
                if (minBuffer <= 0) return@launch
                val activeRecorder = AudioRecord(MediaRecorder.AudioSource.MIC, 16000, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, minBuffer * 2)
                recorder = activeRecorder
                microphoneRecorder = activeRecorder
                if (activeRecorder.state == AudioRecord.STATE_INITIALIZED) {
                    activeRecorder.startRecording()
                    val buf = ShortArray(minBuffer)
                    while (isActive) {
                        val read = activeRecorder.read(buf, 0, buf.size)
                        if (read > 0) {
                            var sum = 0.0
                            for (s in buf.take(read)) sum += s.toDouble() * s
                            val rms = sqrt(sum / read)
                            val db = if (rms > 0) (20 * log10(rms / 32767.0)).coerceIn(-60.0, 0.0) else -60.0
                            _micDbLevel.value = db.toFloat()
                        }
                    }
                }
            } catch (e: SecurityException) {
            } catch (e: Exception) {
            } finally {
                if (microphoneRecorder === recorder) microphoneRecorder = null
                runCatching { recorder?.stop() }
                runCatching { recorder?.release() }
            }
        }
    }

    @Synchronized
    fun stopDiagnostics() {
        diagnosticsJob?.cancel()
        diagnosticsJob = null
        microphoneJob?.cancel()
        runCatching { microphoneRecorder?.stop() }
        microphoneJob = null
        batteryReceiver?.let { receiver ->
            runCatching { getApplication<Application>().applicationContext.unregisterReceiver(receiver) }
        }
        batteryReceiver = null
    }

    override fun onCleared() {
        stopDiagnostics()
        super.onCleared()
    }

    fun armDndShield(context: Context) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (notificationManager.isNotificationPolicyAccessGranted) {
            notificationManager.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_PRIORITY)
            _dndArmed.value = true
        }
    }

    fun openDndSettings(context: Context) {
        context.startActivity(Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        })
    }
}
