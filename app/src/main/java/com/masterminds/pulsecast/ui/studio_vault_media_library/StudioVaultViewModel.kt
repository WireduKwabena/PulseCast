package com.masterminds.pulsecast.ui.studio_vault_media_library

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.masterminds.pulsecast.core.MediaStoreMediaRepository
import com.masterminds.pulsecast.core.StorageTelemetry
import com.masterminds.pulsecast.core.VaultMediaItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class StudioVaultViewModel(application: Application) : AndroidViewModel(application) {
    private val repo = MediaStoreMediaRepository(application)

    private val _mediaItems = MutableStateFlow<List<VaultMediaItem>>(emptyList())
    val mediaItems: StateFlow<List<VaultMediaItem>> = _mediaItems.asStateFlow()

    private val _telemetry = MutableStateFlow(StorageTelemetry(68.4, 256.0, 187.6, 0.27f))
    val telemetry: StateFlow<StorageTelemetry> = _telemetry.asStateFlow()

    private val _selectedCategory = MutableStateFlow("All")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedUris = MutableStateFlow<Set<Uri>>(emptySet())
    val selectedUris: StateFlow<Set<Uri>> = _selectedUris.asStateFlow()

    init {
        refreshMedia()
    }

    fun refreshMedia() {
        viewModelScope.launch {
            val queried = repo.queryStudioMedia()
            _mediaItems.value = queried
            _telemetry.value = repo.getStorageTelemetry()
        }
    }

    fun setCategory(category: String) {
        _selectedCategory.value = category
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun toggleSelection(uri: Uri) {
        val current = _selectedUris.value.toMutableSet()
        if (current.contains(uri)) current.remove(uri) else current.add(uri)
        _selectedUris.value = current
    }

    fun clearSelection() {
        _selectedUris.value = emptySet()
    }

    fun getDeleteIntentForSelection() = repo.createBatchDeleteIntent(_selectedUris.value.toList())

    fun deleteSelectedDirect() {
        viewModelScope.launch {
            _selectedUris.value.forEach { uri ->
                repo.deleteDirect(uri)
            }
            clearSelection()
            refreshMedia()
        }
    }
}
