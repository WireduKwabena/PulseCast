package com.masterminds.pulsecast.ui.live_stream_chat_unified_moderation_drawer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.masterminds.pulsecast.streaming.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay

class ChatViewModel : ViewModel() {
    private val repo = ChatRepository()

    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()

    private val _activePlatformFilter = MutableStateFlow<PlatformType?>(null)
    val activePlatformFilter: StateFlow<PlatformType?> = _activePlatformFilter.asStateFlow()

    val filteredMessages: StateFlow<List<ChatMessage>> = combine(_messages, _activePlatformFilter) { msgs, filter ->
        if (filter == null) {
            msgs.filter { !it.isBanned }
        } else {
            msgs.filter { it.platform == filter && !it.isBanned }
        }
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    private val _chatVelocity = MutableStateFlow(0)
    val chatVelocity: StateFlow<Int> = _chatVelocity.asStateFlow()

    init {
        viewModelScope.launch {
            repo.allMessages.collect { newMsg ->
                _messages.update { current ->
                    val updated = current + newMsg
                    if (updated.size > 200) updated.drop(updated.size - 200) else updated
                }
            }
        }

        viewModelScope.launch {
            while (true) {
                val now = System.currentTimeMillis()
                val sixtySecondsAgo = now - 60_000
                
                val countInLastMinute = _messages.value.count { it.timestamp >= sixtySecondsAgo }
                _chatVelocity.value = countInLastMinute
                
                delay(2000)
            }
        }
    }

    fun setFilter(platform: PlatformType?) {
        _activePlatformFilter.value = platform
    }

    fun banUser(messageId: String) {
        _messages.update { current ->
            current.map { if (it.id == messageId) it.copy(isBanned = true) else it }
        }
    }

    fun approveMessage(messageId: String) {
        _messages.update { current ->
            current.map { if (it.id == messageId) it.copy(isSpamFlagged = false) else it }
        }
    }

    fun connectDefaultChannels() {
        repo.connectTwitch("xqc")
        repo.connectYoutube("dummy_video_id")
        repo.connectKick("dummy_channel")
    }

    fun sendChatMessage(text: String) {
        // Stub: logs the message
        println("Sending chat message: $text")
    }

    override fun onCleared() {
        super.onCleared()
        repo.disconnectAll()
    }
}
