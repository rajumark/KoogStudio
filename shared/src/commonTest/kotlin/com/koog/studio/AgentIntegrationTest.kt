package com.koog.studio

import com.koog.studio.data.repository.OllamaRepositoryImpl
import kotlinx.coroutines.runBlocking
import kotlin.test.Test

class AgentIntegrationTest {

    private val repository = OllamaRepositoryImpl()

    @Test
    fun testCountImagesOnDesktop() = runBlocking {
        val agent = repository.createAgent("gemma4:12b")
        val result = agent.run("How many image files (png, jpg, jpeg, gif, bmp, webp) are in the /Users/raju/Desktop folder? Use tools to find out. Just give me the count number.")
        println("=== AGENT RESULT ===")
        println(result)
        println("=== END ===")
    }
}
