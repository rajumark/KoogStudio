package com.koog.studio.data.repository

import com.koog.studio.domain.repository.UserPreferencesRepository
import java.util.prefs.Preferences

class UserPreferencesRepositoryImpl : UserPreferencesRepository {

    private val prefs: Preferences = Preferences.userNodeForPackage(UserPreferencesRepository::class.java)

    companion object {
        private const val KEY_SELECTED_MODEL = "selected_model"
        private const val KEY_SELECTED_MODE = "selected_mode"
    }

    override var selectedModel: String
        get() = prefs.get(KEY_SELECTED_MODEL, "")
        set(value) = prefs.put(KEY_SELECTED_MODEL, value)

    override var selectedMode: String
        get() = prefs.get(KEY_SELECTED_MODE, "Chat Mode")
        set(value) = prefs.put(KEY_SELECTED_MODE, value)
}
