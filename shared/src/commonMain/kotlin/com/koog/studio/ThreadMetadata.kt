package com.koog.studio

import kotlinx.serialization.Serializable

@Serializable
data class ThreadMetadata(
    val id: String,
    val title: String,
    val createdAt: Long,
    val projectDir: String? = null,
)
