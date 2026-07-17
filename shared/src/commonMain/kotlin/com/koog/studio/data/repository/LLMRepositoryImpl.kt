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
import ai.koog.prompt.llm.LLMProvider
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

    override fun createAgent(provider: String, modelId: String): AIAgent<String, String> {
        val apiKey = settingsRepository.getApiKey(provider)
            ?: error("No API key configured for $provider")

        val executor = when (provider) {
            "OpenAI" -> MultiLLMPromptExecutor(OpenAILLMClient(apiKey))
            "Anthropic" -> MultiLLMPromptExecutor(AnthropicLLMClient(apiKey))
            "Google" -> MultiLLMPromptExecutor(GoogleLLMClient(apiKey))
            else -> error("Unsupported provider: $provider")
        }

        val llmProvider = when (provider) {
            "OpenAI" -> LLMProvider.OpenAI
            "Anthropic" -> LLMProvider.Anthropic
            "Google" -> LLMProvider.Google
            else -> error("Unsupported provider: $provider")
        }

        val model = ai.koog.prompt.llm.LLModel(
            provider = llmProvider,
            id = modelId,
            capabilities = listOf(
                ai.koog.prompt.llm.LLMCapability.Temperature,
                ai.koog.prompt.llm.LLMCapability.Schema.JSON.Basic,
            ),
            contextLength = 131_072,
        )

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
        return when (provider) {
            "OpenAI" -> listOf(
                OpenAIModels.Chat.GPT4o.id,
                OpenAIModels.Chat.GPT4oMini.id,
                OpenAIModels.Chat.GPT4_1.id,
                OpenAIModels.Chat.GPT4_1Mini.id,
                OpenAIModels.Chat.O3Mini.id,
            )
            "Anthropic" -> listOf(
                AnthropicModels.Opus_4_1.id,
                AnthropicModels.Sonnet_4_5.id,
                AnthropicModels.Sonnet_4.id,
                AnthropicModels.Haiku_4_5.id,
            )
            "Google" -> listOf(
                GoogleModels.Gemini2_5Pro.id,
                GoogleModels.Gemini2_5Flash.id,
                GoogleModels.Gemini2_5FlashLite.id,
            )
            else -> emptyList()
        }
    }

    override fun getDefaultModel(provider: String): String {
        return when (provider) {
            "OpenAI" -> OpenAIModels.Chat.GPT4o.id
            "Anthropic" -> AnthropicModels.Sonnet_4_5.id
            "Google" -> GoogleModels.Gemini2_5Flash.id
            else -> getModelsForProvider(provider).firstOrNull() ?: ""
        }
    }
}
