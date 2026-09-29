package com.masterminds.pulsecast.core

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

private val Context.floatingBallDataStore: DataStore<Preferences> by preferencesDataStore(name = "floating_ball_prefs")

class FloatingBallPrefsRepository(private val context: Context) {

    companion object {
        private val KEY_IDLE_OPACITY = floatPreferencesKey("idle_opacity")
        private val KEY_ACTIVE_OPACITY = floatPreferencesKey("active_opacity")
        private val KEY_DOCKING_MODE = stringPreferencesKey("docking_mode")
        private val KEY_SELECTED_SKIN = stringPreferencesKey("selected_skin")
        private val KEY_SINGLE_TAP = stringPreferencesKey("gesture_single_tap")
        private val KEY_DOUBLE_TAP = stringPreferencesKey("gesture_double_tap")
        private val KEY_LONG_PRESS = stringPreferencesKey("gesture_long_press")
        private val KEY_SWIPE_IN = stringPreferencesKey("gesture_swipe_in")
    }

    val orbConfigurationFlow: Flow<OrbConfiguration> = context.floatingBallDataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { prefs ->
            val idleOpacity = prefs[KEY_IDLE_OPACITY] ?: 0.35f
            val activeOpacity = prefs[KEY_ACTIVE_OPACITY] ?: 1.0f
            val dockingMode = prefs[KEY_DOCKING_MODE] ?: "MAGNETIC"
            val skin = prefs[KEY_SELECTED_SKIN] ?: "CYBER_RED"
            val singleTap = prefs[KEY_SINGLE_TAP] ?: "RADIAL_MENU"
            val doubleTap = prefs[KEY_DOUBLE_TAP] ?: "AI_CLIP"
            val longPress = prefs[KEY_LONG_PRESS] ?: "MIC_MUTE"
            val swipeIn = prefs[KEY_SWIPE_IN] ?: "OPEN_CHAT"

            OrbConfiguration(
                idleOpacity = idleOpacity,
                activeOpacity = activeOpacity,
                dockingMode = dockingMode,
                skin = skin,
                gestureActions = mapOf(
                    "SINGLE_TAP" to singleTap,
                    "DOUBLE_TAP" to doubleTap,
                    "LONG_PRESS" to longPress,
                    "SWIPE_IN" to swipeIn
                )
            )
        }

    suspend fun saveOrbConfiguration(config: OrbConfiguration) {
        context.floatingBallDataStore.edit { prefs ->
            prefs[KEY_IDLE_OPACITY] = config.idleOpacity
            prefs[KEY_ACTIVE_OPACITY] = config.activeOpacity
            prefs[KEY_DOCKING_MODE] = config.dockingMode
            prefs[KEY_SELECTED_SKIN] = config.skin
            config.gestureActions["SINGLE_TAP"]?.let { prefs[KEY_SINGLE_TAP] = it }
            config.gestureActions["DOUBLE_TAP"]?.let { prefs[KEY_DOUBLE_TAP] = it }
            config.gestureActions["LONG_PRESS"]?.let { prefs[KEY_LONG_PRESS] = it }
            config.gestureActions["SWIPE_IN"]?.let { prefs[KEY_SWIPE_IN] = it }
        }
    }
}
