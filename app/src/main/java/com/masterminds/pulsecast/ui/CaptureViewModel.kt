package com.masterminds.pulsecast.ui

import android.app.Application
import android.content.Context
import android.os.Environment
import android.os.StatFs
import android.util.DisplayMetrics
import android.view.WindowManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.masterminds.pulsecast.core.AudioMode
import com.masterminds.pulsecast.core.BitrateCalculator
import com.masterminds.pulsecast.core.CaptureSessionStore
import com.masterminds.pulsecast.core.Quality
import com.masterminds.pulsecast.core.RecordingPrefsRepository
import com.masterminds.pulsecast.core.ResolutionScaler
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class CaptureViewModel(application: Application) : AndroidViewModel(application) {
    private val repo = RecordingPrefsRepository(application)

    val isRecording = CaptureSessionStore.isRecording

    private val _storagePercentage = MutableStateFlow(0.74f)
    val storagePercentage = _storagePercentage.asStateFlow()

    private val _freeSpaceText = MutableStateFlow("112 GB Free • 18.4 hrs remaining")
    val freeSpaceText = _freeSpaceText.asStateFlow()

    private val _systemAudioLevel = MutableStateFlow(0.72f)
    val systemAudioLevel = _systemAudioLevel.asStateFlow()

    private val _micAudioLevel = MutableStateFlow(0.60f)
    val micAudioLevel = _micAudioLevel.asStateFlow()

    private val _resolution = MutableStateFlow("1080p")
    val resolution: StateFlow<String> = _resolution.asStateFlow()

    private val _fps = MutableStateFlow(60)
    val fps: StateFlow<Int> = _fps.asStateFlow()

    private val _audioMode = MutableStateFlow(AudioMode.MIC_ONLY)
    val audioMode: StateFlow<AudioMode> = _audioMode.asStateFlow()

    init {
        viewModelScope.launch {
            repo.resolution.collect { _resolution.value = it }
        }
        viewModelScope.launch {
            repo.fps.collect { _fps.value = it }
        }
        viewModelScope.launch {
            repo.audioMode.collect { _audioMode.value = it }
        }
        
        viewModelScope.launch {
            while (true) {
                refreshStorage(getApplication())
                delay(10_000)
            }
        }
    }

    fun toggleRecording() {
        CaptureSessionStore.setRecording(!isRecording.value)
    }
    
    fun setRecording(active: Boolean) {
        CaptureSessionStore.setRecording(active)
    }

    fun setResolution(v: String) {
        viewModelScope.launch { repo.setResolution(v) }
    }

    fun setFps(v: Int) {
        viewModelScope.launch { repo.setFps(v) }
    }

    fun setAudioMode(m: AudioMode) {
        viewModelScope.launch { repo.setAudioMode(m) }
    }

    fun refreshStorage(context: Context) {
        // Recordings are written to the primary shared Movies volume, not the
        // app's private data partition. Use app-specific external storage to
        // inspect that same volume without requesting broad media access.
        val storagePath = context.getExternalFilesDir(Environment.DIRECTORY_MOVIES)?.path
            ?: Environment.getDataDirectory().path
        val stat = StatFs(storagePath)
        val availableBytes = stat.availableBytes
        val totalBytes = stat.totalBytes
        
        val freeGb = availableBytes / (1024.0 * 1024.0 * 1024.0)
        
        val percentage = (1.0 - availableBytes.toDouble() / totalBytes.toDouble()).toFloat().coerceIn(0f, 1f)
        _storagePercentage.value = percentage
        
        // Bitrate calculation based on active capture profile (Mbps)
        val quality = when (_resolution.value) {
            "2K", "1440p" -> Quality.HIGH
            "4K" -> Quality.ORIGINAL
            "720p" -> Quality.LOW
            else -> Quality.MEDIUM
        }
        val fpsValue = _fps.value.coerceIn(15, 120)
        val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val displayMetrics = DisplayMetrics()
        @Suppress("DEPRECATION")
        windowManager.defaultDisplay.getRealMetrics(displayMetrics)
        val captureResolution = ResolutionScaler.scaleToTier(displayMetrics.widthPixels, displayMetrics.heightPixels, quality)
        val videoBitrate = BitrateCalculator.calculate(captureResolution, fpsValue, quality)
        val totalBitrate = videoBitrate + 128_000 // AAC audio estimate
        val remainingHours = (availableBytes * 8.0 / totalBitrate) / 3600.0
        
        _freeSpaceText.value = String.format("%.1f GB Free • %.1f hrs remaining", freeGb, remainingHours)
    }
}
