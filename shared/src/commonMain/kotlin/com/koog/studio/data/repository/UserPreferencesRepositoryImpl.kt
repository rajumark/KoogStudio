package com.koog.studio.data.repository

import com.koog.studio.domain.repository.UserPreferencesRepository
import java.util.prefs.Preferences

class UserPreferencesRepositoryImpl : UserPreferencesRepository {

    private val prefs: Preferences = Preferences.userNodeForPackage(UserPreferencesRepository::class.java)

    companion object {
        private const val KEY_SELECTED_MODEL = "selected_model"
        private const val KEY_SELECTED_MODE = "selected_mode"
        private const val KEY_IS_DARK_MODE = "is_dark_mode"
        private const val KEY_THEME_SEED_NAME = "theme_seed_name"
    }

    override var selectedModel: String
        get() = prefs.get(KEY_SELECTED_MODEL, "")
        set(value) = prefs.put(KEY_SELECTED_MODEL, value)

    override var selectedMode: String
        get() = prefs.get(KEY_SELECTED_MODE, "Chat Mode")
        set(value) = prefs.put(KEY_SELECTED_MODE, value)

    override var isDarkMode: Boolean
        get() = prefs.getBoolean(KEY_IS_DARK_MODE, false)
        set(value) = prefs.putBoolean(KEY_IS_DARK_MODE, value)

    override var themeSeedName: String
        get() = prefs.get(KEY_THEME_SEED_NAME, "Blue")
        set(value) = prefs.put(KEY_THEME_SEED_NAME, value)
}
