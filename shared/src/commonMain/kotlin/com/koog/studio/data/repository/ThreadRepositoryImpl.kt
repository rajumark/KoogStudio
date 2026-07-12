package com.koog.studio.data.repository

import com.koog.studio.Thread
import com.koog.studio.domain.repository.ThreadRepository
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

class ThreadRepositoryImpl : ThreadRepository {

    private val json = Json { prettyPrint = true; ignoreUnknownKeys = true }

    private val appDir: File by lazy {
        val os = System.getProperty("os.name").lowercase()
        val base = when {
            os.contains("mac") -> File(System.getProperty("user.home"), "Library/Application Support")
            os.contains("win") -> File(System.getenv("APPDATA") ?: System.getProperty("user.home"))
            else -> File(System.getProperty("user.home"), ".config")
        }
        File(base, "KoogStudio").also { it.mkdirs() }
    }

    private val threadsFile get() = File(appDir, "threads.json")

    override fun loadThreads(): List<Thread> {
        return try {
            if (threadsFile.exists()) {
                json.decodeFromString<List<Thread>>(threadsFile.readText())
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    override fun saveThreads(threads: List<Thread>) {
        try {
            threadsFile.writeText(json.encodeToString(threads))
        } catch (_: Exception) {}
    }
}
