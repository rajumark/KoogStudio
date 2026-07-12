package com.koog.studio.data.repository

import com.koog.studio.Thread
import com.koog.studio.ThreadMetadata
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

    private val threadsDir: File by lazy {
        File(appDir, "threads").also { it.mkdirs() }
    }

    private val legacyFile: File by lazy {
        File(appDir, "threads.json")
    }

    override fun loadThreads(): List<Thread> {
        migrateLegacyIfNeeded()

        return try {
            threadsDir.listFiles()
                ?.filter { it.isDirectory }
                ?.mapNotNull { dir -> readThreadFromDir(dir) }
                ?.sortedBy { it.createdAt }
                ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    override fun saveThread(thread: Thread) {
        try {
            val dir = File(threadsDir, thread.id).also { it.mkdirs() }

            val metadata = ThreadMetadata(
                id = thread.id,
                title = thread.title,
                createdAt = thread.createdAt,
            )
            File(dir, "metadata.json").writeText(json.encodeToString(metadata))

            File(dir, "messages.json").writeText(json.encodeToString(thread.messages))
        } catch (_: Exception) {}
    }

    override fun deleteThread(threadId: String) {
        try {
            val dir = File(threadsDir, threadId)
            if (dir.exists()) {
                dir.listFiles()?.forEach { it.delete() }
                dir.delete()
            }
        } catch (_: Exception) {}
    }

    private fun readThreadFromDir(dir: File): Thread? {
        return try {
            val metadataFile = File(dir, "metadata.json")
            val messagesFile = File(dir, "messages.json")

            if (!metadataFile.exists()) return null

            val metadata = json.decodeFromString<ThreadMetadata>(metadataFile.readText())
            val messages = if (messagesFile.exists()) {
                json.decodeFromString<List<com.koog.studio.ChatMessage>>(messagesFile.readText())
            } else {
                emptyList()
            }

            Thread(
                id = metadata.id,
                title = metadata.title,
                messages = messages,
                createdAt = metadata.createdAt,
            )
        } catch (e: Exception) {
            null
        }
    }

    private fun migrateLegacyIfNeeded() {
        if (!legacyFile.exists()) return

        try {
            val legacyThreads = json.decodeFromString<List<Thread>>(legacyFile.readText())
            for (thread in legacyThreads) {
                saveThread(thread)
            }
            legacyFile.delete()
        } catch (_: Exception) {}
    }
}
