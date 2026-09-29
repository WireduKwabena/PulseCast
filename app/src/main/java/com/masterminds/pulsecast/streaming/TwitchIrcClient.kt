package com.masterminds.pulsecast.streaming

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import okhttp3.*
import java.util.UUID

class TwitchIrcClient(
    private val client: OkHttpClient = OkHttpClient()
) {
    private val _messages = MutableSharedFlow<ChatMessage>(replay = 50, extraBufferCapacity = 200)
    val messages: SharedFlow<ChatMessage> = _messages.asSharedFlow()

    private var webSocket: WebSocket? = null
    private var isConnected = false
    private var currentChannel: String = ""
    private var currentToken: String? = null
    private var retryAttempt = 0

    fun connect(channel: String, oauthToken: String? = null) {
        currentChannel = channel
        currentToken = oauthToken
        retryAttempt = 0
        internalConnect()
    }

    private fun internalConnect() {
        if (isConnected) disconnect()

        val request = Request.Builder()
            .url("wss://irc-ws.chat.twitch.tv:443")
            .build()

        webSocket = client.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                isConnected = true
                retryAttempt = 0
                
                webSocket.send("CAP REQ :twitch.tv/tags twitch.tv/commands")

                val nick = if (currentToken != null) "justinfan12345" else "justinfan12345"
                val token = currentToken ?: "SCHMOOPIIE"
                
                if (currentToken != null) {
                    webSocket.send("PASS oauth:$currentToken")
                    webSocket.send("NICK $nick")
                } else {
                    webSocket.send("PASS SCHMOOPIIE")
                    webSocket.send("NICK justinfan12345")
                }
                webSocket.send("JOIN #$currentChannel")
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                val lines = text.split("\r\n")
                for (line in lines) {
                    if (line.isEmpty()) continue
                    
                    if (line.startsWith("PING")) {
                        webSocket.send(line.replace("PING", "PONG"))
                        continue
                    }
                    
                    if (line.contains(" PRIVMSG ")) {
                        parsePrivMsg(line)
                    }
                }
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                isConnected = false
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                isConnected = false
                retryAttempt++
                if (!ReconnectBackoff.shouldGiveUp(retryAttempt)) {
                    val delayMs = ReconnectBackoff.delayForAttempt(retryAttempt)
                    CoroutineScope(Dispatchers.IO).launch {
                        delay(delayMs)
                        internalConnect()
                    }
                }
            }
        })
    }
    
    private fun parsePrivMsg(line: String) {
        var username = ""
        var content = ""
        val badges = mutableListOf<Badge>()
        
        var remainingLine = line
        if (remainingLine.startsWith("@")) {
            val spaceIndex = remainingLine.indexOf(' ')
            if (spaceIndex != -1) {
                val tagsStr = remainingLine.substring(1, spaceIndex)
                remainingLine = remainingLine.substring(spaceIndex + 1)
                
                val tags = tagsStr.split(";")
                for (tag in tags) {
                    if (tag.startsWith("badges=")) {
                        val badgesStr = tag.substring("badges=".length)
                        if (badgesStr.isNotEmpty()) {
                            val badgePairs = badgesStr.split(",")
                            for (pair in badgePairs) {
                                val badgeName = pair.split("/").firstOrNull()
                                if (badgeName == "subscriber" || badgeName == "moderator" || badgeName == "broadcaster") {
                                    badges.add(Badge(name = badgeName))
                                }
                            }
                        }
                    }
                }
            }
        }
        
        if (remainingLine.startsWith(":")) {
            val bangIndex = remainingLine.indexOf('!')
            if (bangIndex != -1) {
                username = remainingLine.substring(1, bangIndex)
            }
            
            val privMsgIndex = remainingLine.indexOf(" PRIVMSG ")
            if (privMsgIndex != -1) {
                val colonIndex = remainingLine.indexOf(" :", privMsgIndex)
                if (colonIndex != -1) {
                    content = remainingLine.substring(colonIndex + 2)
                }
            }
        }
        
        if (username.isNotEmpty() && content.isNotEmpty()) {
            val message = ChatMessage(
                id = UUID.randomUUID().toString(),
                platform = PlatformType.TWITCH,
                sender = username,
                content = content,
                badges = badges,
                timestamp = System.currentTimeMillis()
            )
            _messages.tryEmit(message)
        }
    }

    fun disconnect() {
        webSocket?.close(1000, "User disconnected")
        webSocket = null
        isConnected = false
    }
}
