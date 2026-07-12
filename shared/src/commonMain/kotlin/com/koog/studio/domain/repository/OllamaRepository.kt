package com.koog.studio.domain.repository

import ai.koog.agents.core.agent.AIAgent

interface OllamaRepository {
    fun createAgent(modelId: String): AIAgent<String, String>
    suspend fun fetchAvailableModels(): List<String>
}
