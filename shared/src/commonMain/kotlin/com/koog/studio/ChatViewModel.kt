package com.koog.studio

import ai.koog.agents.core.agent.AIAgent
import com.koog.studio.domain.repository.LLMRepository
import com.koog.studio.domain.repository.SettingsRepository
import com.koog.studio.domain.repository.ThreadRepository
import com.koog.studio.tools.AgentStatusProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ChatViewModel(
    private val threadRepository: ThreadRepository,
    private val llmRepository: LLMRepository,
    private val settingsRepository: SettingsRepository,
) : ViewModel() {

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

    private val _agentStatus = MutableStateFlow<String?>(null)
    val agentStatus: StateFlow<String?> = _agentStatus.asStateFlow()

    private val _agentLog = MutableStateFlow<List<String>>(emptyList())
    val agentLog: StateFlow<List<String>> = _agentLog.asStateFlow()

    private val _inputText = MutableStateFlow("")
    val inputText: StateFlow<String> = _inputText.asStateFlow()

    private val _selectedProvider = MutableStateFlow<String?>(null)
    val selectedProvider: StateFlow<String?> = _selectedProvider.asStateFlow()

    private val _selectedModel = MutableStateFlow("")
    val selectedModel: StateFlow<String> = _selectedModel.asStateFlow()

    private val _availableModels = MutableStateFlow<List<String>>(emptyList())
    val availableModels: StateFlow<List<String>> = _availableModels.asStateFlow()

    private val _configuredProviders = MutableStateFlow<List<String>>(emptyList())
    val configuredProviders: StateFlow<List<String>> = _configuredProviders.asStateFlow()

    private val _showSettings = MutableStateFlow(false)
    val showSettings: StateFlow<Boolean> = _showSettings.asStateFlow()

    private val _projectDir = MutableStateFlow<String?>(null)
    val projectDir: StateFlow<String?> = _projectDir.asStateFlow()

    private var agent: AIAgent<String, String>? = null
    private var agentJob: Job? = null

    init {
        AgentStatusProvider.onStatusChange = { status ->
            _agentStatus.value = status
        }
        AgentStatusProvider.onLog = { entry ->
            _agentLog.value = _agentLog.value + entry
        }

        loadProviders()

        val saved = threadRepository.loadThreads()
        if (saved.isNotEmpty()) {
            _threads.value = saved
            _activeThreadId.value = saved.last().id
            _projectDir.value = saved.last().projectDir
        } else {
            createNewThread()
        }
    }

    private fun loadProviders() {
        val configured = settingsRepository.getConfiguredProviders()
        _configuredProviders.value = configured

        if (configured.isNotEmpty()) {
            val currentProvider = _selectedProvider.value
            if (currentProvider == null || currentProvider !in configured) {
                selectProvider(configured.first())
            }
        }
    }

    fun selectProvider(provider: String) {
        _selectedProvider.value = provider
        val models = llmRepository.getModelsForProvider(provider)
        _availableModels.value = models

        val defaultModel = llmRepository.getDefaultModel(provider)
        _selectedModel.value = defaultModel
        agent = llmRepository.createAgent(provider, defaultModel)
    }

    fun selectModel(model: String) {
        val provider = _selectedProvider.value ?: return
        _selectedModel.value = model
        agent = llmRepository.createAgent(provider, model)
    }

    fun openSettings() {
        _showSettings.value = true
    }

    fun closeSettings() {
        _showSettings.value = false
        loadProviders()
    }

    fun saveApiKey(provider: String, key: String) {
        if (key.isBlank()) {
            settingsRepository.removeApiKey(provider)
        } else {
            settingsRepository.setApiKey(provider, key)
        }
        loadProviders()
    }

    fun getApiKey(provider: String): String {
        return settingsRepository.getApiKey(provider) ?: ""
    }

    private fun persistThread(thread: Thread) {
        threadRepository.saveThread(thread)
    }

    fun setProjectDir(dir: String?) {
        _projectDir.value = dir
        val threadId = _activeThreadId.value ?: return
        threadRepository.updateProjectDir(threadId, dir)
    }

    fun createNewThread() {
        val thread = Thread()
        _threads.value = _threads.value + thread
        _activeThreadId.value = thread.id
        _inputText.value = ""
        persistThread(thread)
    }

    fun selectThread(id: String) {
        _activeThreadId.value = id
        _inputText.value = ""
        val thread = _threads.value.find { it.id == id }
        _projectDir.value = thread?.projectDir
    }

    fun deleteThread(id: String) {
        val updated = _threads.value.filter { it.id != id }
        _threads.value = updated
        threadRepository.deleteThread(id)
        if (_activeThreadId.value == id) {
            if (updated.isNotEmpty()) {
                _activeThreadId.value = updated.last().id
                _projectDir.value = updated.last().projectDir
            } else {
                createNewThread()
            }
        }
    }

    fun onInputChange(text: String) {
        _inputText.value = text
    }

    fun stopAgent() {
        agentJob?.cancel()
        agentJob = null
        _agentStatus.value = null
        _isLoading.value = false
    }

    fun sendMessage() {
        val text = _inputText.value.trim()
        val threadId = _activeThreadId.value ?: return
        if (text.isEmpty() || _isLoading.value) return

        val currentAgent = agent
        if (currentAgent == null) {
            val errorMessage = ChatMessage(
                content = "No AI provider configured. Open Settings to add an API key.",
                isUser = false,
            )
            appendMessageToThread(threadId, errorMessage)
            return
        }

        val userMessage = ChatMessage(content = text, isUser = true)
        appendMessageToThread(threadId, userMessage)
        _inputText.value = ""
        _isLoading.value = true
        _agentStatus.value = "Thinking..."
        _agentLog.value = emptyList()

        agentJob = viewModelScope.launch {
            try {
                val result = currentAgent.run(text)
                val aiMessage = ChatMessage(content = result, isUser = false)
                appendMessageToThread(threadId, aiMessage)
            } catch (e: Exception) {
                if (e is kotlinx.coroutines.CancellationException) {
                    val stoppedMessage = ChatMessage(
                        content = "Stopped by user.",
                        isUser = false,
                    )
                    appendMessageToThread(threadId, stoppedMessage)
                } else {
                    val errorMessage = ChatMessage(
                        content = "Error: ${e.message ?: "Unknown error"}",
                        isUser = false,
                    )
                    appendMessageToThread(threadId, errorMessage)
                }
            } finally {
                _agentStatus.value = null
                _isLoading.value = false
                agentJob = null
            }
        }
    }

    private fun appendMessageToThread(threadId: String, message: ChatMessage) {
        val updatedThread = _threads.value.find { it.id == threadId }?.let { thread ->
            val updatedMessages = thread.messages + message
            val title = if (thread.messages.isEmpty() && message.isUser) {
                message.content.take(40).let { if (it.length < message.content.length) "$it..." else it }
            } else {
                thread.title
            }
            thread.copy(messages = updatedMessages, title = title)
        } ?: return

        _threads.value = _threads.value.map {
            if (it.id == threadId) updatedThread else it
        }
        persistThread(updatedThread)
    }
}
