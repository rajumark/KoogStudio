package com.koog.studio.di

import com.koog.studio.ChatViewModel
import com.koog.studio.data.repository.LLMRepositoryImpl
import com.koog.studio.data.repository.SettingsRepositoryImpl
import com.koog.studio.data.repository.ThreadRepositoryImpl
import com.koog.studio.domain.repository.LLMRepository
import com.koog.studio.domain.repository.SettingsRepository
import com.koog.studio.domain.repository.ThreadRepository
import org.koin.dsl.module

val appModule = module {
    single<ThreadRepository> { ThreadRepositoryImpl() }
    single<SettingsRepository> { SettingsRepositoryImpl() }
    single<LLMRepository> { LLMRepositoryImpl(settingsRepository = get()) }

    single {
        ChatViewModel(
            threadRepository = get(),
            llmRepository = get(),
            settingsRepository = get(),
        )
    }
}
