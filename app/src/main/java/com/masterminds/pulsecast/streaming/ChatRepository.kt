package com.masterminds.pulsecast.streaming

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import java.util.UUID

class ChatRepository(
    private val twitchIrcClient: TwitchIrcClient = TwitchIrcClient()
) {
    private val repositoryScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    
    private val _allMessages = MutableSharedFlow<ChatMessage>(replay = 50, extraBufferCapacity = 200)
    val allMessages: SharedFlow<ChatMessage> = _allMessages.asSharedFlow()

    private var youtubeJob: kotlinx.coroutines.Job? = null
    private var kickJob: kotlinx.coroutines.Job? = null

    init {
        repositoryScope.launch {
            twitchIrcClient.messages.collect { msg ->
                _allMessages.emit(msg)
            }
        }
    }

    fun connectTwitch(channel: String, token: String? = null) {
        twitchIrcClient.connect(channel, token)
    }

    // Stub
    fun connectYoutube(videoId: String) {
        youtubeJob?.cancel()
        youtubeJob = repositoryScope.launch {
            flow {
                while (true) {
                    emit(makeSampleMsg(PlatformType.YOUTUBE))
                    delay(5000)
                }
            }.collect { msg ->
                _allMessages.emit(msg)
            }
        }
    }

    // Stub
    fun connectKick(channelSlug: String) {
        kickJob?.cancel()
        kickJob = repositoryScope.launch {
            flow {
                while (true) {
                    emit(makeSampleMsg(PlatformType.KICK))
                    delay(5000)
                }
            }.collect { msg ->
                _allMessages.emit(msg)
            }
        }
    }

    fun disconnectAll() {
        twitchIrcClient.disconnect()
        youtubeJob?.cancel()
        kickJob?.cancel()
    }

    private fun makeSampleMsg(platform: PlatformType): ChatMessage {
        return ChatMessage(
            id = UUID.randomUUID().toString(),
            platform = platform,
            sender = "User_${(100..999).random()}",
            content = "This is a sample message from $platform!",
            timestamp = System.currentTimeMillis()
        )
    }
}
