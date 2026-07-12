package com.koog.studio

import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class Thread(
    val id: String = UUID.randomUUID().toString(),
    val title: String = "New Chat",
    val messages: List<ChatMessage> = emptyList(),
    val createdAt: Long = System.currentTimeMillis(),
)
