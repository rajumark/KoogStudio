package com.koog.studio

import java.util.prefs.Preferences

object UserPreferences {
    private val prefs: Preferences = Preferences.userNodeForPackage(UserPreferences::class.java)

    private const val KEY_SELECTED_MODEL = "selected_model"
    private const val KEY_SELECTED_MODE = "selected_mode"

    var selectedModel: String
        get() = prefs.get(KEY_SELECTED_MODEL, "")
        set(value) = prefs.put(KEY_SELECTED_MODEL, value)

    var selectedMode: String
        get() = prefs.get(KEY_SELECTED_MODE, "Chat Mode")
        set(value) = prefs.put(KEY_SELECTED_MODE, value)
}
