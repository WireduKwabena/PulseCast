package com.masterminds.pulsecast.ui.instant_clip_highlight_export_flow

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.masterminds.pulsecast.core.ExportOptions
import com.masterminds.pulsecast.core.ExportProgress
import com.masterminds.pulsecast.core.Media3VideoExporter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File

class ClipExportViewModel(application: Application) : AndroidViewModel(application) {
    private val exporter = Media3VideoExporter(application)

    val exportProgress: StateFlow<ExportProgress> = exporter.progressState

    private val _selectedRatio = MutableStateFlow("16:9")
    val selectedRatio: StateFlow<String> = _selectedRatio.asStateFlow()

    private val _trimRangeMs = MutableStateFlow(0L to 30000L)
    val trimRangeMs: StateFlow<Pair<Long, Long>> = _trimRangeMs.asStateFlow()

    private val _speedMultiplier = MutableStateFlow(1.0f)
    val speedMultiplier: StateFlow<Float> = _speedMultiplier.asStateFlow()

    fun setSelectedRatio(ratio: String) {
        _selectedRatio.value = ratio
    }

    fun setTrimRange(startMs: Long, endMs: Long) {
        _trimRangeMs.value = startMs to endMs
    }

    fun setSpeedMultiplier(speed: Float) {
        _speedMultiplier.value = speed
    }

    fun startExport(inputUri: Uri) {
        viewModelScope.launch {
            val options = ExportOptions(
                inputUri = inputUri,
                startMs = _trimRangeMs.value.first,
                endMs = _trimRangeMs.value.second,
                targetAspectRatio = _selectedRatio.value,
                speedMultiplier = _speedMultiplier.value
            )
            exporter.exportClip(options)
        }
    }

    fun cancelExport() {
        exporter.cancelExport()
    }
}
