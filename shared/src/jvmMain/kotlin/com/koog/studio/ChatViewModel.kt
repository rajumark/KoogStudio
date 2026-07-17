package com.koog.studio

import ai.koog.agents.core.agent.AIAgent
import ai.koog.prompt.executor.llms.MultiLLMPromptExecutor
import ai.koog.prompt.executor.ollama.client.OllamaClient
import ai.koog.prompt.llm.LLMProvider
import ai.koog.prompt.llm.LLModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

@Serializable
data class ChatMessage(
    val content: String,
    val isUser: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)

@Serializable
data class ChatThread(
    val id: String,
    val title: String,
    val messages: List<ChatMessage>,
    val createdAt: Long,
    val updatedAt: Long
)

class ChatViewModel {
    private val scope = CoroutineScope(Dispatchers.Default)
    private val json = Json { prettyPrint = true; ignoreUnknownKeys = true }
    
    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()

    private val _availableModels = MutableStateFlow<List<String>>(emptyList())
    val availableModels: StateFlow<List<String>> = _availableModels.asStateFlow()

    private val _selectedModel = MutableStateFlow("gemma3:1b")
    val selectedModel: StateFlow<String> = _selectedModel.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _isOllamaConnected = MutableStateFlow(false)
    val isOllamaConnected: StateFlow<Boolean> = _isOllamaConnected.asStateFlow()

    private val _currentThreadId = MutableStateFlow(generateThreadId())
    val currentThreadId: StateFlow<String> = _currentThreadId.asStateFlow()

    private val _threads = MutableStateFlow<List<ChatThread>>(emptyList())
    val threads: StateFlow<List<ChatThread>> = _threads.asStateFlow()

    private var agent: AIAgent<String, String>? = null
    
    private val threadsDir: File by lazy {
        val appSupport = File(System.getProperty("user.home"), "Library/Application Support/KoogStudio/threads")
        appSupport.mkdirs()
        appSupport
    }

    init {
        connectToOllama()
        loadThreads()
    }

    private fun generateThreadId(): String {
        return "thread_${System.currentTimeMillis()}"
    }

    private fun connectToOllama() {
        try {
            _isOllamaConnected.value = true
            _availableModels.value = listOf(
                "gemma3:1b", "gemma3:4b", "gemma4:12b", 
                "qwen3.5:4b", "qwen3.5:latest",
                "deepseek-coder:6.7b"
            )
            createAgent()
        } catch (e: Exception) {
            _isOllamaConnected.value = false
        }
    }

    private fun createAgent() {
        try {
            val client = OllamaClient()
            val model = LLModel(LLMProvider.Ollama, _selectedModel.value)
            
            agent = AIAgent.builder()
                .promptExecutor(MultiLLMPromptExecutor(client))
                .llmModel(model)
                .systemPrompt("You are a helpful AI assistant called Koog. Answer questions concisely and helpfully.")
                .build()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun selectModel(modelName: String) {
        _selectedModel.value = modelName
        createAgent()
    }

    fun sendMessage(userMessage: String) {
        if (userMessage.isBlank() || _isLoading.value) return

        val userMsg = ChatMessage(content = userMessage, isUser = true)
        _messages.value = _messages.value + userMsg

        _isLoading.value = true

        scope.launch {
            try {
                val currentAgent = agent
                if (currentAgent != null) {
                    val response = currentAgent.run(userMessage)
                    val responseMsg = ChatMessage(content = response, isUser = false)
                    _messages.value = _messages.value + responseMsg
                } else {
                    val errorMsg = ChatMessage(
                        content = "Error: Agent not initialized. Please check if Ollama is running.",
                        isUser = false
                    )
                    _messages.value = _messages.value + errorMsg
                }
            } catch (e: Exception) {
                val errorMsg = ChatMessage(
                    content = "Error: ${e.message ?: "Unknown error occurred"}",
                    isUser = false
                )
                _messages.value = _messages.value + errorMsg
            } finally {
                _isLoading.value = false
                saveCurrentThread()
            }
        }
    }

    fun clearChat() {
        _messages.value = emptyList()
        _currentThreadId.value = generateThreadId()
    }

    private fun saveCurrentThread() {
        if (_messages.value.isEmpty()) return
        
        val thread = ChatThread(
            id = _currentThreadId.value,
            title = _messages.value.firstOrNull { it.isUser }?.content?.take(50) ?: "New Chat",
            messages = _messages.value,
            createdAt = _messages.value.firstOrNull()?.timestamp ?: System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )

        val file = File(threadsDir, "${thread.id}.json")
        file.writeText(json.encodeToString(thread))
        
        loadThreads()
    }

    private fun loadThreads() {
        val threadFiles = threadsDir.listFiles { file -> file.extension == "json" } ?: emptyArray()
        val loadedThreads = threadFiles.mapNotNull { file ->
            try {
                json.decodeFromString<ChatThread>(file.readText())
            } catch (e: Exception) {
                null
            }
        }.sortedByDescending { it.updatedAt }
        
        _threads.value = loadedThreads
    }

    fun loadThread(threadId: String) {
        val thread = _threads.value.find { it.id == threadId }
        if (thread != null) {
            _messages.value = thread.messages
            _currentThreadId.value = thread.id
        }
    }

    fun deleteThread(threadId: String) {
        val file = File(threadsDir, "$threadId.json")
        if (file.exists()) {
            file.delete()
        }
        _threads.value = _threads.value.filter { it.id != threadId }
        
        if (_currentThreadId.value == threadId) {
            clearChat()
        }
    }
}