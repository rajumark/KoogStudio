package com.koog.studio

import ai.koog.agents.core.agent.AIAgent
import com.koog.studio.domain.repository.OllamaRepository
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
    private val ollamaRepository: OllamaRepository,
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

    private val _inputText = MutableStateFlow("")
    val inputText: StateFlow<String> = _inputText.asStateFlow()

    private val _selectedModel = MutableStateFlow("qwen3.5:latest")
    val selectedModel: StateFlow<String> = _selectedModel.asStateFlow()

    private var agent: AIAgent<String, String> = ollamaRepository.createAgent("qwen3.5:latest")
    private var agentJob: Job? = null

    init {
        AgentStatusProvider.onStatusChange = { status ->
            _agentStatus.value = status
        }

        val saved = threadRepository.loadThreads()
        if (saved.isNotEmpty()) {
            _threads.value = saved
            _activeThreadId.value = saved.last().id
        } else {
            createNewThread()
        }
    }

    private fun persistThread(thread: Thread) {
        threadRepository.saveThread(thread)
    }

    fun setProjectDir(dir: String?) {
        val threadId = _activeThreadId.value ?: return
        _threads.value = _threads.value.map {
            if (it.id == threadId) it.copy(projectDir = dir) else it
        }
        threadRepository.updateProjectDir(threadId, dir)
    }

    fun getActiveThreadProjectDir(): String? {
        val threadId = _activeThreadId.value ?: return null
        return _threads.value.find { it.id == threadId }?.projectDir
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

        val userMessage = ChatMessage(content = text, isUser = true)
        appendMessageToThread(threadId, userMessage)
        _inputText.value = ""
        _isLoading.value = true
        _agentStatus.value = "Thinking..."

        agentJob = viewModelScope.launch {
            try {
                val result = agent.run(text)
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
