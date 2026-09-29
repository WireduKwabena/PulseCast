package com.masterminds.pulsecast.core

import android.content.Context
import android.util.Base64
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.masterminds.pulsecast.streaming.StreamDestination
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

private val Context.destinationsDataStore: DataStore<Preferences> by preferencesDataStore(name = "stream_destinations_prefs")

class StreamDestinationRepository(private val context: Context) {

    companion object {
        private val KEY_DESTINATIONS_JSON = stringPreferencesKey("destinations_serialized")
    }

    val destinationsFlow: Flow<List<StreamDestination>> = context.destinationsDataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { prefs ->
            val rawString = prefs[KEY_DESTINATIONS_JSON] ?: ""
            if (rawString.isEmpty()) {
                emptyList()
            } else {
                parseDestinations(rawString)
            }
        }

    suspend fun saveDestinations(destinations: List<StreamDestination>) {
        val serialized = serializeDestinations(destinations)
        context.destinationsDataStore.edit { prefs ->
            prefs[KEY_DESTINATIONS_JSON] = serialized
        }
    }

    private fun serializeDestinations(destinations: List<StreamDestination>): String {
        return destinations.joinToString(";") { dest ->
            val encryptedUrl = PulseCastKeystoreManager.encrypt(dest.rtmpUrl)
            listOf(dest.id, dest.label, encryptedUrl, (dest.bitrateKbps ?: -1).toString())
                .joinToString("|") { encodeField(it) }
        }
    }

    private fun parseDestinations(rawString: String): List<StreamDestination> {
        return rawString.split(";").filter(String::isNotEmpty).map { entry ->
            val parts = entry.split("|")
            require(parts.size == 4) { "Stored ingest destination has an invalid format" }
            val id = decodeField(parts[0])
            val label = decodeField(parts[1])
            val decryptedUrl = PulseCastKeystoreManager.decrypt(decodeField(parts[2]))
            val bitrate = decodeField(parts[3]).toIntOrNull()
                ?: throw IllegalStateException("Stored ingest bitrate is invalid")
            StreamDestination(id, label, decryptedUrl, bitrate.takeIf { it > 0 })
        }
    }

    private fun encodeField(value: String): String = Base64.encodeToString(
        value.toByteArray(Charsets.UTF_8),
        Base64.URL_SAFE or Base64.NO_WRAP or Base64.NO_PADDING
    )

    private fun decodeField(value: String): String = String(
        Base64.decode(value, Base64.URL_SAFE or Base64.NO_WRAP or Base64.NO_PADDING),
        Charsets.UTF_8
    )
}
