package com.pafoid.kpoker.network

import com.russhwolf.settings.Settings
import com.russhwolf.settings.set

class SettingsManager {
    private val settings: Settings = Settings()

    companion object {
        private const val KEY_REMEMBER_ME = "remember_me"
        private const val KEY_USERNAME = "saved_username"
        private const val KEY_PASSWORD = "saved_password"
    }

    var rememberMe: Boolean
        get() = settings.getBoolean(KEY_REMEMBER_ME, false)
        set(value) { settings.putBoolean(KEY_REMEMBER_ME, value) }

    var savedUsername: String?
        get() = settings.getStringOrNull(KEY_USERNAME)
        set(value) { if (value != null) settings.putString(KEY_USERNAME, value) else settings.remove(KEY_USERNAME) }

    var savedPassword: String?
        get() = settings.getStringOrNull(KEY_PASSWORD)
        set(value) { if (value != null) settings.putString(KEY_PASSWORD, value) else settings.remove(KEY_PASSWORD) }

    fun clearSavedCredentials() {
        savedUsername = null
        savedPassword = null
        rememberMe = false
    }
}
