package com.masterminds.pulsecast.streaming

import com.masterminds.pulsecast.core.StreamDestinationRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.UUID

/**
 * Process-scoped list of RTMP destinations armed for the next broadcast.
 * Encrypted DataStore persistence ensures armed stream destinations and keys
 * are retained securely across app restarts.
 */
object BroadcastDestinationStore {
    private val mutableDestinations = MutableStateFlow<List<StreamDestination>>(emptyList())
    val destinations: StateFlow<List<StreamDestination>> = mutableDestinations.asStateFlow()
    private val mutableConnectionStates = MutableStateFlow<Map<String, ConnectionState>>(emptyMap())
    val connectionStates: StateFlow<Map<String, ConnectionState>> = mutableConnectionStates.asStateFlow()

    private var repository: StreamDestinationRepository? = null
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val mutableInitialized = MutableStateFlow(false)
    val isInitialized: StateFlow<Boolean> = mutableInitialized.asStateFlow()
    private val mutableInitializationError = MutableStateFlow<String?>(null)
    val initializationError: StateFlow<String?> = mutableInitializationError.asStateFlow()
    private val mutationMutex = Mutex()
    @Volatile private var collectionStarted = false

    fun initialize(repo: StreamDestinationRepository) {
        if (collectionStarted) return
        collectionStarted = true
        repository = repo
        scope.launch {
            try {
                repo.destinationsFlow.collect { list ->
                    require(list.size <= 1) { "More than one ingest destination is stored; remove extras before going live." }
                    val retainedStates = mutableConnectionStates.value
                    mutableDestinations.value = list
                    mutableConnectionStates.value = list.associate { it.id to (retainedStates[it.id] ?: ConnectionState.Idle) }
                    mutableInitializationError.value = null
                    mutableInitialized.value = true
                }
            } catch (error: Exception) {
                mutableInitializationError.value = error.message ?: "Saved ingest destination could not be loaded securely."
                mutableInitialized.value = true
            }
        }
    }

    suspend fun add(label: String, rtmpUrl: String, bitrateKbps: Int? = null): StreamDestination = mutationMutex.withLock {
        require(mutableInitialized.value) { "Ingest destinations are still loading. Try again shortly." }
        check(mutableInitializationError.value == null) {
            mutableInitializationError.value ?: "Saved ingest destination could not be loaded securely."
        }
        require(mutableDestinations.value.isEmpty()) { "V1 supports one RTMP destination. Remove the current destination before adding another." }
        require(rtmpUrl.startsWith("rtmp://", ignoreCase = true) || rtmpUrl.startsWith("rtmps://", ignoreCase = true)) {
            "Only RTMP and RTMPS destinations can be armed right now."
        }
        val destination = StreamDestination(UUID.randomUUID().toString(), label, rtmpUrl, bitrateKbps)
        val updated = mutableDestinations.value + destination
        val repo = checkNotNull(repository) { "Ingest destination storage is unavailable." }
        // Do not expose an armed destination until encrypted persistence succeeds.
        repo.saveDestinations(updated)
        mutableDestinations.value = updated
        mutableConnectionStates.value = mutableConnectionStates.value + (destination.id to ConnectionState.Idle)
        destination
    }

    suspend fun remove(id: String) = mutationMutex.withLock {
        val updated = mutableDestinations.value.filterNot { it.id == id }
        val repo = checkNotNull(repository) { "Ingest destination storage is unavailable." }
        repo.saveDestinations(updated)
        mutableDestinations.value = updated
        mutableConnectionStates.value = mutableConnectionStates.value - id
    }

    suspend fun clear() = mutationMutex.withLock {
        val repo = checkNotNull(repository) { "Ingest destination storage is unavailable." }
        repo.saveDestinations(emptyList())
        mutableDestinations.value = emptyList()
        mutableConnectionStates.value = emptyMap()
    }

    @Synchronized
    fun setConnectionState(id: String, state: ConnectionState) {
        if (mutableDestinations.value.any { it.id == id }) {
            mutableConnectionStates.value = mutableConnectionStates.value + (id to state)
        }
    }

}
