package com.koog.studio.domain.repository

import com.koog.studio.Thread

interface ThreadRepository {
    fun loadThreads(): List<Thread>
    fun saveThread(thread: Thread)
    fun deleteThread(threadId: String)
}
