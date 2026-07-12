package com.koog.studio.data.repository

import ai.koog.agents.core.agent.AIAgent
import ai.koog.prompt.llm.LLMCapability
import ai.koog.prompt.llm.LLMProvider
import ai.koog.prompt.llm.LLModel
import ai.koog.prompt.executor.llms.MultiLLMPromptExecutor
import ai.koog.prompt.executor.ollama.client.OllamaClient
import com.koog.studio.domain.repository.OllamaRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class OllamaRepositoryImpl : OllamaRepository {

    private val ollamaClient = OllamaClient()

    override fun createAgent(modelId: String): AIAgent<String, String> {
        val model = LLModel(
            provider = LLMProvider.Ollama,
            id = modelId,
            capabilities = listOf(
                LLMCapability.Temperature,
                LLMCapability.Schema.JSON.Basic,
            ),
            contextLength = 131_072,
        )
        return AIAgent(
            promptExecutor = MultiLLMPromptExecutor(ollamaClient),
            systemPrompt = "You are a helpful AI assistant. Answer concisely and clearly.",
            llmModel = model,
        )
    }

    override suspend fun fetchAvailableModels(): List<String> {
        return try {
            val result = withContext(Dispatchers.IO) {
                val process = ProcessBuilder("ollama", "list")
                    .redirectErrorStream(true)
                    .start()
                val output = process.inputStream.bufferedReader().readText()
                process.waitFor()
                output
            }
            result.lines()
                .drop(1)
                .mapNotNull { line ->
                    line.split("\\s+".toRegex()).firstOrNull()?.takeIf { it.isNotEmpty() }
                }
        } catch (e: Exception) {
            emptyList()
        }
    }
}
