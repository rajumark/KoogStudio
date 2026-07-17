package com.koog.studio.domain.repository

interface UserPreferencesRepository {
    var selectedModel: String
    var selectedMode: String
    var isDarkMode: Boolean
    var themeSeedName: String
}
