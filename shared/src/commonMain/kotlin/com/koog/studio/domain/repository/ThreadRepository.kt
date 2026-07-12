package com.koog.studio.domain.repository

import com.koog.studio.Thread

interface ThreadRepository {
    fun loadThreads(): List<Thread>
    fun saveThreads(threads: List<Thread>)
}
