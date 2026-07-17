package com.koog.studio

import com.koog.studio.data.repository.LLMRepositoryImpl
import com.koog.studio.data.repository.SettingsRepositoryImpl
import com.koog.studio.tools.AgentStatusProvider
import kotlinx.coroutines.runBlocking
import kotlin.test.Test

class AgentIntegrationTest {

    private val settingsRepository = SettingsRepositoryImpl()
    private val llmRepository = LLMRepositoryImpl(settingsRepository)

    @Test
    fun testCountImagesOnDesktop() = runBlocking {
        AgentStatusProvider.onLog = { entry ->
            println("[LOG] $entry")
        }
        AgentStatusProvider.onStatusChange = { status ->
            println("[STATUS] $status")
        }

        val provider = "OpenAI"
        val model = llmRepository.getDefaultModel(provider)
        val agent = llmRepository.createAgent(provider, model)
        val result = agent.run("How many image files (png, jpg, gif, etc) are on my Desktop? Give me the exact count.")
        println("=== AGENT RESULT ===")
        println(result)
        println("=== END ===")
    }
}
