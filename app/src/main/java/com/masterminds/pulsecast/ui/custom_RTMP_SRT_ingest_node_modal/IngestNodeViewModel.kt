package com.masterminds.pulsecast.ui.custom_RTMP_SRT_ingest_node_modal

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.masterminds.pulsecast.streaming.BroadcastDestinationStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.Socket

data class IngestNode(val id: String, val label: String, val url: String, val protocol: String, val bitrate: Int, val isArmed: Boolean, val lastPingMs: Int?)

class IngestNodeViewModel : ViewModel() {
    private val _nodes = MutableStateFlow<List<IngestNode>>(emptyList())
    val nodes: StateFlow<List<IngestNode>> = _nodes.asStateFlow()

    private val _pingResult = MutableStateFlow("")
    val pingResult: StateFlow<String> = _pingResult.asStateFlow()

    private val _validationMessage = MutableStateFlow("")
    val validationMessage: StateFlow<String> = _validationMessage.asStateFlow()

    init {
        viewModelScope.launch {
            BroadcastDestinationStore.destinations.collect { destinations ->
                _nodes.value = destinations.map { destination ->
                    val endpoint = runCatching { java.net.URI(destination.rtmpUrl) }.getOrNull()
                    val safeUrl = if (endpoint?.host != null) {
                        "${endpoint.scheme}://${endpoint.host}${endpoint.port.takeIf { it > 0 }?.let { ":$it" }.orEmpty()}"
                    } else "Saved secure destination"
                    IngestNode(
                        id = destination.id,
                        label = destination.label,
                        url = safeUrl,
                        protocol = "RTMP(S)",
                        bitrate = destination.bitrateKbps ?: 0,
                        isArmed = true,
                        lastPingMs = null
                    )
                }
            }
        }
    }

    suspend fun addNode(url: String, streamKey: String, protocol: String, bitrate: Int, label: String): Boolean {
        if (protocol != "RTMP(S)") {
            _validationMessage.value = "SRT and RTSP publishing are not implemented yet. Choose RTMP(S)."
            return false
        }
        val endpoint = runCatching { java.net.URI(url.trim()) }.getOrNull()
        if (endpoint == null || endpoint.scheme?.lowercase() !in setOf("rtmp", "rtmps") || endpoint.host.isNullOrBlank()) {
            _validationMessage.value = "Enter a valid RTMP or RTMPS server URL."
            return false
        }
        val endpointPathSegments = endpoint.rawPath.orEmpty().split('/').filter(String::isNotBlank)
        if (streamKey.isBlank() && endpointPathSegments.size < 2) {
            _validationMessage.value = "Enter a stream key or include it in the server URL."
            return false
        }
        val fullUrl = if (streamKey.isBlank()) url.trim() else "${url.trim().trimEnd('/')}/${streamKey.trim().trimStart('/')}"
        val destination = try {
            BroadcastDestinationStore.add(label, fullUrl, bitrate)
        } catch (error: Exception) {
            _validationMessage.value = error.message ?: "This destination could not be armed."
            return false
        }
        val newNode = IngestNode(destination.id, label, url.trim(), protocol, bitrate, true, null)
        _nodes.value = _nodes.value + newNode
        _validationMessage.value = ""
        return true
    }

    fun removeNode(id: String) {
        viewModelScope.launch {
            try {
                BroadcastDestinationStore.remove(id)
                _nodes.value = _nodes.value.filter { it.id != id }
                _validationMessage.value = ""
            } catch (error: Exception) {
                _validationMessage.value = error.message ?: "Destination could not be removed from secure storage."
            }
        }
    }

    fun testHandshake(url: String, port: Int) {
        if (url.isBlank() || port !in 1..65535) {
            _pingResult.value = "Enter a valid server URL. TCP reachability does not verify stream credentials."
            return
        }
        viewModelScope.launch {
            _pingResult.value = "Testing TCP reachability..."
            val start = System.currentTimeMillis()
            try {
                withContext(Dispatchers.IO) {
                    val socket = Socket()
                    socket.connect(java.net.InetSocketAddress(url, port), 5000)
                    socket.close()
                }
                val rtt = System.currentTimeMillis() - start
                _pingResult.value = "TCP reachable in ${rtt}ms • stream credentials not verified"
            } catch (e: Exception) {
                _pingResult.value = "Failed: ${e.message}"
            }
        }
    }
}
