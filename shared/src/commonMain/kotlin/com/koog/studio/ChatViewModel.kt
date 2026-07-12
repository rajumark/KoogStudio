package com.koog.studio

import ai.koog.agents.core.agent.AIAgent
import ai.koog.prompt.llm.LLMCapability
import ai.koog.prompt.llm.LLMProvider
import ai.koog.prompt.llm.LLModel
import ai.koog.prompt.executor.llms.MultiLLMPromptExecutor
import ai.koog.prompt.executor.ollama.client.OllamaClient
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ChatViewModel : ViewModel() {

    private val _threads = MutableStateFlow<List<Thread>>(emptyList())
    val threads: StateFlow<List<Thread>> = _threads.asStateFlow()

    private val _activeThreadId = MutableStateFlow<String?>(null)
    val activeThreadId: StateFlow<String?> = _activeThreadId.asStateFlow()

    val activeMessages: List<ChatMessage>
        get() {
            val id = _activeThreadId.value ?: return emptyList()
            return _threads.value.find { it.id == id }?.messages ?: emptyList()
        }

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _inputText = MutableStateFlow("")
    val inputText: StateFlow<String> = _inputText.asStateFlow()

    private val _models = MutableStateFlow<List<String>>(emptyList())
    val models: StateFlow<List<String>> = _models.asStateFlow()

    private val _selectedModel = MutableStateFlow(UserPreferences.selectedModel)
    val selectedModel: StateFlow<String> = _selectedModel.asStateFlow()

    private val _selectedMode = MutableStateFlow(UserPreferences.selectedMode)
    val selectedMode: StateFlow<String> = _selectedMode.asStateFlow()

    private val ollamaClient = OllamaClient()

    private var agent: AIAgent<String, String> = createAgent(_selectedModel.value.ifEmpty { "gemma3:4b" })

    init {
        val saved = ThreadStore.loadThreads()
        if (saved.isNotEmpty()) {
            _threads.value = saved
            _activeThreadId.value = saved.last().id
        } else {
            createNewThread()
        }
        fetchModels()
    }

    private fun createAgent(modelId: String): AIAgent<String, String> {
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

    private fun persistThreads() {
        ThreadStore.saveThreads(_threads.value)
    }

    fun createNewThread() {
        val thread = Thread()
        _threads.value = _threads.value + thread
        _activeThreadId.value = thread.id
        _inputText.value = ""
        persistThreads()
    }

    fun selectThread(id: String) {
        _activeThreadId.value = id
        _inputText.value = ""
    }

    fun onInputChange(text: String) {
        _inputText.value = text
    }

    fun onModelSelected(model: String) {
        _selectedModel.value = model
        UserPreferences.selectedModel = model
        agent = createAgent(model)
    }

    fun onModeSelected(mode: String) {
        _selectedMode.value = mode
        UserPreferences.selectedMode = mode
    }

    fun fetchModels() {
        viewModelScope.launch {
            try {
                val result = withContext(Dispatchers.IO) {
                    val process = ProcessBuilder("ollama", "list")
                        .redirectErrorStream(true)
                        .start()
                    val output = process.inputStream.bufferedReader().readText()
                    process.waitFor()
                    output
                }
                val modelNames = result.lines()
                    .drop(1)
                    .mapNotNull { line ->
                        line.split("\\s+".toRegex()).firstOrNull()?.takeIf { it.isNotEmpty() }
                    }
                _models.value = modelNames

                val saved = _selectedModel.value
                if (saved.isNotEmpty() && saved in modelNames) {
                    onModelSelected(saved)
                } else if (modelNames.isNotEmpty()) {
                    onModelSelected(modelNames.first())
                }
            } catch (e: Exception) {
                _models.value = emptyList()
            }
        }
    }

    fun sendMessage() {
        val text = _inputText.value.trim()
        val threadId = _activeThreadId.value ?: return
        if (text.isEmpty() || _isLoading.value) return

        val userMessage = ChatMessage(content = text, isUser = true)
        appendMessageToThread(threadId, userMessage)
        _inputText.value = ""
        _isLoading.value = true

        viewModelScope.launch {
            try {
                val result = agent.run(text)
                val aiMessage = ChatMessage(content = result, isUser = false)
                appendMessageToThread(threadId, aiMessage)
            } catch (e: Exception) {
                val errorMessage = ChatMessage(
                    content = "Error: ${e.message ?: "Unknown error"}",
                    isUser = false,
                )
                appendMessageToThread(threadId, errorMessage)
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun appendMessageToThread(threadId: String, message: ChatMessage) {
        _threads.value = _threads.value.map { thread ->
            if (thread.id == threadId) {
                val updatedMessages = thread.messages + message
                val title = if (thread.messages.isEmpty() && message.isUser) {
                    message.content.take(40).let { if (it.length < message.content.length) "$it..." else it }
                } else {
                    thread.title
                }
                thread.copy(messages = updatedMessages, title = title)
            } else {
                thread
            }
        }
        persistThreads()
    }
}
