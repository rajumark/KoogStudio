package com.koog.studio

import com.koog.studio.data.repository.OllamaRepositoryImpl
import com.koog.studio.tools.AgentStatusProvider
import kotlinx.coroutines.runBlocking
import kotlin.test.Test

class AgentIntegrationTest {

    private val repository = OllamaRepositoryImpl()

    @Test
    fun testCountImagesOnDesktop() = runBlocking {
        AgentStatusProvider.onLog = { entry ->
            println("[LOG] $entry")
        }
        AgentStatusProvider.onStatusChange = { status ->
            println("[STATUS] $status")
        }

        val agent = repository.createAgent("gemma4:12b")
        val result = agent.run("How many image files (png, jpg, gif, etc) are on my Desktop? Give me the exact count.")
        println("=== AGENT RESULT ===")
        println(result)
        println("=== END ===")
    }
}
