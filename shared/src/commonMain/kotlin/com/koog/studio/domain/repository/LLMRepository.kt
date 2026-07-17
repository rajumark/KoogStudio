package com.koog.studio.domain.repository

import ai.koog.agents.core.agent.AIAgent

interface LLMRepository {
    fun createAgent(provider: String, modelId: String): AIAgent<String, String>
    fun getModelsForProvider(provider: String): List<String>
    fun getDefaultModel(provider: String): String
}
