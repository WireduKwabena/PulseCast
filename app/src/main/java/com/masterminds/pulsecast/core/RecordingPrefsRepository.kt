package com.masterminds.pulsecast.core

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.masterminds.pulsecast.core.AudioMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "recording_prefs")

class RecordingPrefsRepository(private val context: Context) {

    companion object {
        private val KEY_RESOLUTION = stringPreferencesKey("resolution")
        private val KEY_FPS = intPreferencesKey("fps")
        private val KEY_AUDIO_MODE = stringPreferencesKey("audio_mode")
    }

    val resolution: Flow<String> = context.dataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { it[KEY_RESOLUTION] ?: "1080p" }

    val fps: Flow<Int> = context.dataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { it[KEY_FPS] ?: 60 }

    val audioMode: Flow<AudioMode> = context.dataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { AudioMode.valueOf(it[KEY_AUDIO_MODE] ?: AudioMode.MIC_ONLY.name) }

    suspend fun setResolution(value: String) {
        context.dataStore.edit { it[KEY_RESOLUTION] = value }
    }

    suspend fun setFps(value: Int) {
        context.dataStore.edit { it[KEY_FPS] = value }
    }

    suspend fun setAudioMode(mode: AudioMode) {
        context.dataStore.edit { it[KEY_AUDIO_MODE] = mode.name }
    }
}
