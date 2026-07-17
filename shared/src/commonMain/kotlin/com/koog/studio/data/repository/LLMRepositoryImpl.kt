package com.koog.studio.data.repository

import ai.koog.agents.core.agent.AIAgent
import ai.koog.agents.features.eventHandler.feature.handleEvents
import ai.koog.prompt.executor.clients.openai.OpenAILLMClient
import ai.koog.prompt.executor.clients.openai.OpenAIModels
import ai.koog.prompt.executor.clients.anthropic.AnthropicLLMClient
import ai.koog.prompt.executor.clients.anthropic.AnthropicModels
import ai.koog.prompt.executor.clients.google.GoogleLLMClient
import ai.koog.prompt.executor.clients.google.GoogleModels
import ai.koog.prompt.executor.llms.MultiLLMPromptExecutor
import ai.koog.prompt.llm.LLModel
import ai.koog.prompt.message.MessagePart
import com.koog.studio.domain.repository.LLMRepository
import com.koog.studio.domain.repository.SettingsRepository
import com.koog.studio.tools.AgentStatusProvider
import com.koog.studio.tools.createToolRegistry

class LLMRepositoryImpl(
    private val settingsRepository: SettingsRepository,
) : LLMRepository {

    private val toolRegistry = createToolRegistry()

    private fun getSystemPrompt(): String {
        val os = System.getProperty("os.name")
        val home = System.getProperty("user.home")
        val user = System.getProperty("user.name")
        return """You are a helpful AI assistant with access to tools for file operations, shell commands, and user interaction.

System info:
- OS: $os
- Home directory: $home
- Username: $user

Rules:
- Always use tools to answer questions about files, directories, or system state. Never guess.
- When a tool fails, try a different approach. Do not give up after one failure.
- Use absolute paths (starting with $home) instead of ~ when possible.
- If you don't know something, use tools to find out rather than making assumptions."""
    }

    private val allModels: Map<String, List<LLModel>> = mapOf(
        "OpenAI" to listOf(
            OpenAIModels.Chat.GPT4o,
            OpenAIModels.Chat.GPT4oMini,
            OpenAIModels.Chat.GPT4_1,
            OpenAIModels.Chat.GPT4_1Mini,
            OpenAIModels.Chat.O3Mini,
        ),
        "Anthropic" to listOf(
            AnthropicModels.Opus_4_1,
            AnthropicModels.Sonnet_4_5,
            AnthropicModels.Sonnet_4,
            AnthropicModels.Haiku_4_5,
        ),
        "Google" to listOf(
            GoogleModels.Gemini2_5Pro,
            GoogleModels.Gemini2_5Flash,
            GoogleModels.Gemini2_5FlashLite,
        ),
    )

    private fun findModel(provider: String, modelId: String): LLModel {
        return allModels[provider]?.find { it.id == modelId }
            ?: error("Unknown model: $modelId for provider: $provider")
    }

    override fun createAgent(provider: String, modelId: String): AIAgent<String, String> {
        val apiKey = settingsRepository.getApiKey(provider)
            ?: error("No API key configured for $provider")

        val executor = when (provider) {
            "OpenAI" -> MultiLLMPromptExecutor(OpenAILLMClient(apiKey))
            "Anthropic" -> MultiLLMPromptExecutor(AnthropicLLMClient(apiKey))
            "Google" -> MultiLLMPromptExecutor(GoogleLLMClient(apiKey))
            else -> error("Unsupported provider: $provider")
        }

        val model = findModel(provider, modelId)

        return AIAgent(
            promptExecutor = executor,
            systemPrompt = getSystemPrompt(),
            llmModel = model,
            toolRegistry = toolRegistry,
            installFeatures = {
                handleEvents {
                    onLLMCallStarting { ctx ->
                        AgentStatusProvider.notify("Thinking...")
                        AgentStatusProvider.log(">>> Sending to ${ctx.model.id}...")
                    }
                    onLLMCallCompleted { ctx ->
                        val response = ctx.response ?: return@onLLMCallCompleted
                        val text = response.textContent()
                        val toolCalls = response.parts.filterIsInstance<MessagePart.Tool.Call>()
                        if (text.isNotBlank()) {
                            AgentStatusProvider.log("<<< LLM: ${text.take(300)}")
                        }
                        if (toolCalls.isNotEmpty()) {
                            toolCalls.forEach { tc ->
                                AgentStatusProvider.log(">> Tool: ${tc.tool}(${tc.args.take(200)})")
                            }
                        }
                    }
                    onToolCallStarting { ctx ->
                        AgentStatusProvider.notify("Running: ${ctx.toolName}")
                        AgentStatusProvider.log(">> Executing: ${ctx.toolName}")
                    }
                    onToolCallCompleted { ctx ->
                        AgentStatusProvider.log("<< Result: ${ctx.toolResult.toString().take(200)}")
                    }
                    onToolCallFailed { ctx ->
                        AgentStatusProvider.log("!!! Tool failed [${ctx.toolName}]: ${ctx.message}")
                    }
                    onAgentCompleted { ctx ->
                        AgentStatusProvider.log("--- Done ---")
                        AgentStatusProvider.notify(null)
                    }
                    onAgentExecutionFailed { ctx ->
                        AgentStatusProvider.log("!!! Error: ${ctx.error.message}")
                        AgentStatusProvider.notify(null)
                    }
                }
            },
        )
    }

    override fun getModelsForProvider(provider: String): List<String> {
        return allModels[provider]?.map { it.id } ?: emptyList()
    }

    override fun getDefaultModel(provider: String): String {
        return allModels[provider]?.firstOrNull()?.id ?: ""
    }
}
