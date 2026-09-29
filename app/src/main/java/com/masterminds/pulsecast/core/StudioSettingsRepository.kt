package com.masterminds.pulsecast.core

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

data class StudioSettings(
    val ultraLowLatencyEnabled: Boolean = true,
    val streamDelaySec: Int = 30,
    val watermarkOpacity: Float = 0.8f,
    val watermarkAnchor: String = "TR",
    val facecamShape: String = "16:9 Wide",
    val pipScalePct: Float = 25f,
    val pipDock: String = "Top-R",
    val chromaEnabled: Boolean = true,
    val chromaHue: String = "Studio Green",
    val similarityPct: Float = 42f,
    val smoothnessPct: Float = 18f,
    val spillSuppressionPct: Float = 65f
)

private val Context.studioDataStore: DataStore<Preferences> by preferencesDataStore(name = "studio_settings_prefs")

class StudioSettingsRepository(private val context: Context) {

    companion object {
        private val KEY_ULTRA_LOW_LATENCY = booleanPreferencesKey("ultra_low_latency")
        private val KEY_STREAM_DELAY = intPreferencesKey("stream_delay")
        private val KEY_WM_OPACITY = floatPreferencesKey("wm_opacity")
        private val KEY_WM_ANCHOR = stringPreferencesKey("wm_anchor")
        private val KEY_FACECAM_SHAPE = stringPreferencesKey("facecam_shape")
        private val KEY_PIP_SCALE = floatPreferencesKey("pip_scale")
        private val KEY_PIP_DOCK = stringPreferencesKey("pip_dock")
        private val KEY_CHROMA_ENABLED = booleanPreferencesKey("chroma_enabled")
        private val KEY_CHROMA_HUE = stringPreferencesKey("chroma_hue")
        private val KEY_SIMILARITY = floatPreferencesKey("similarity_pct")
        private val KEY_SMOOTHNESS = floatPreferencesKey("smoothness_pct")
        private val KEY_SPILL_SUPPRESSION = floatPreferencesKey("spill_suppression_pct")
    }

    val studioSettingsFlow: Flow<StudioSettings> = context.studioDataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { prefs ->
            StudioSettings(
                ultraLowLatencyEnabled = prefs[KEY_ULTRA_LOW_LATENCY] ?: true,
                streamDelaySec = prefs[KEY_STREAM_DELAY] ?: 30,
                watermarkOpacity = prefs[KEY_WM_OPACITY] ?: 0.8f,
                watermarkAnchor = prefs[KEY_WM_ANCHOR] ?: "TR",
                facecamShape = prefs[KEY_FACECAM_SHAPE] ?: "16:9 Wide",
                pipScalePct = prefs[KEY_PIP_SCALE] ?: 25f,
                pipDock = prefs[KEY_PIP_DOCK] ?: "Top-R",
                chromaEnabled = prefs[KEY_CHROMA_ENABLED] ?: true,
                chromaHue = prefs[KEY_CHROMA_HUE] ?: "Studio Green",
                similarityPct = prefs[KEY_SIMILARITY] ?: 42f,
                smoothnessPct = prefs[KEY_SMOOTHNESS] ?: 18f,
                spillSuppressionPct = prefs[KEY_SPILL_SUPPRESSION] ?: 65f
            )
        }

    suspend fun saveStudioSettings(settings: StudioSettings) {
        context.studioDataStore.edit { prefs ->
            prefs[KEY_ULTRA_LOW_LATENCY] = settings.ultraLowLatencyEnabled
            prefs[KEY_STREAM_DELAY] = settings.streamDelaySec
            prefs[KEY_WM_OPACITY] = settings.watermarkOpacity
            prefs[KEY_WM_ANCHOR] = settings.watermarkAnchor
            prefs[KEY_FACECAM_SHAPE] = settings.facecamShape
            prefs[KEY_PIP_SCALE] = settings.pipScalePct
            prefs[KEY_PIP_DOCK] = settings.pipDock
            prefs[KEY_CHROMA_ENABLED] = settings.chromaEnabled
            prefs[KEY_CHROMA_HUE] = settings.chromaHue
            prefs[KEY_SIMILARITY] = settings.similarityPct
            prefs[KEY_SMOOTHNESS] = settings.smoothnessPct
            prefs[KEY_SPILL_SUPPRESSION] = settings.spillSuppressionPct
        }
    }
}
