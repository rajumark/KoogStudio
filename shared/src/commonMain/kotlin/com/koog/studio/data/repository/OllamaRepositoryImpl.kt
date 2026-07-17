package com.koog.studio.data.repository

import ai.koog.agents.core.agent.AIAgent
import ai.koog.agents.features.eventHandler.feature.handleEvents
import ai.koog.prompt.llm.LLMCapability
import ai.koog.prompt.llm.LLMProvider
import ai.koog.prompt.llm.LLModel
import ai.koog.prompt.message.MessagePart
import ai.koog.prompt.executor.llms.MultiLLMPromptExecutor
import ai.koog.prompt.executor.ollama.client.OllamaClient
import com.koog.studio.domain.repository.OllamaRepository
import com.koog.studio.tools.AgentStatusProvider
import com.koog.studio.tools.createToolRegistry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class OllamaRepositoryImpl : OllamaRepository {

    private val ollamaClient = OllamaClient()
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
