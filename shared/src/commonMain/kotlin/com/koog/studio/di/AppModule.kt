package com.koog.studio.di

import com.koog.studio.ChatViewModel
import com.koog.studio.data.repository.OllamaRepositoryImpl
import com.koog.studio.data.repository.ThreadRepositoryImpl
import com.koog.studio.data.repository.UserPreferencesRepositoryImpl
import com.koog.studio.domain.repository.OllamaRepository
import com.koog.studio.domain.repository.ThreadRepository
import com.koog.studio.domain.repository.UserPreferencesRepository
import org.koin.dsl.module

val appModule = module {
    single<ThreadRepository> { ThreadRepositoryImpl() }
    single<UserPreferencesRepository> { UserPreferencesRepositoryImpl() }
    single<OllamaRepository> { OllamaRepositoryImpl() }

    single {
        ChatViewModel(
            threadRepository = get(),
            preferencesRepository = get(),
            ollamaRepository = get(),
        )
    }
}
