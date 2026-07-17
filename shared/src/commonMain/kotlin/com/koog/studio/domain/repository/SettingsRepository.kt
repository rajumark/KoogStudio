package com.koog.studio.domain.repository

interface SettingsRepository {
    fun getApiKey(provider: String): String?
    fun setApiKey(provider: String, key: String)
    fun removeApiKey(provider: String)
    fun isConfigured(provider: String): Boolean
    fun getConfiguredProviders(): List<String>
}
