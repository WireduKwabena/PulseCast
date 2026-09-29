package com.masterminds.pulsecast.streaming

enum class PlatformType { TWITCH, YOUTUBE, KICK, CUSTOM }

data class Badge(val name: String, val imageUrl: String? = null)

data class ChatMessage(
    val id: String,
    val platform: PlatformType,
    val sender: String,
    val content: String,
    val badges: List<Badge> = emptyList(),
    val isSuperChat: Boolean = false,
    val tipAmount: Double? = null,
    val timestamp: Long = System.currentTimeMillis(),
    val isSpamFlagged: Boolean = false,
    val isBanned: Boolean = false
)
