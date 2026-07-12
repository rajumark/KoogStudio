package com.koog.studio

import ai.koog.agents.core.agent.AIAgent
import ai.koog.prompt.llm.LLMCapability
import ai.koog.prompt.llm.LLMProvider
import ai.koog.prompt.llm.LLModel
import ai.koog.prompt.executor.llms.MultiLLMPromptExecutor
import ai.koog.prompt.executor.ollama.client.OllamaClient
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

val Gemma3_4B = LLModel(
    provider = LLMProvider.Ollama,
    id = "gemma3:4b",
    capabilities = listOf(
        LLMCapability.Temperature,
        LLMCapability.Schema.JSON.Basic,
    ),
    contextLength = 131_072,
)

class ChatViewModel : ViewModel() {

    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _inputText = MutableStateFlow("")
    val inputText: StateFlow<String> = _inputText.asStateFlow()

    private val ollamaClient = OllamaClient()

    private val agent = AIAgent(
        promptExecutor = MultiLLMPromptExecutor(ollamaClient),
        systemPrompt = "You are a helpful AI assistant. Answer concisely and clearly.",
        llmModel = Gemma3_4B,
    )

    fun onInputChange(text: String) {
        _inputText.value = text
    }

    fun sendMessage() {
        val text = _inputText.value.trim()
        if (text.isEmpty() || _isLoading.value) return

        val userMessage = ChatMessage(content = text, isUser = true)
        _messages.value = _messages.value + userMessage
        _inputText.value = ""
        _isLoading.value = true

        viewModelScope.launch {
            try {
                val result = agent.run(text)
                val aiMessage = ChatMessage(content = result, isUser = false)
                _messages.value = _messages.value + aiMessage
            } catch (e: Exception) {
                val errorMessage = ChatMessage(
                    content = "Error: ${e.message ?: "Unknown error"}",
                    isUser = false,
                )
                _messages.value = _messages.value + errorMessage
            } finally {
                _isLoading.value = false
            }
        }
    }
}
