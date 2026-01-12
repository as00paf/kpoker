package com.pafoid.kpoker.network

import com.russhwolf.settings.Settings
import com.russhwolf.settings.set

import com.pafoid.kpoker.domain.model.Language
import com.pafoid.kpoker.domain.model.Settings as AppSettings

class SettingsManager {
    private val settings: Settings = Settings()

    companion object {
        private const val KEY_REMEMBER_ME = "remember_me"
        private const val KEY_USERNAME = "saved_username"
        private const val KEY_PASSWORD = "saved_password"
        private const val KEY_FULLSCREEN = "app_fullscreen"
        private const val KEY_MUSIC_VOL = "app_music_vol"
        private const val KEY_SFX_VOL = "app_sfx_vol"
        private const val KEY_LANGUAGE = "app_language"
    }

    fun loadAppSettings(): AppSettings {
        return AppSettings(
            isFullscreen = settings.getBoolean(KEY_FULLSCREEN, false),
            musicVolume = settings.getFloat(KEY_MUSIC_VOL, 0.5f),
            sfxVolume = settings.getFloat(KEY_SFX_VOL, 0.7f),
            language = Language.valueOf(settings.getString(KEY_LANGUAGE, Language.ENGLISH.name))
        )
    }

    fun saveAppSettings(appSettings: AppSettings) {
        settings.putBoolean(KEY_FULLSCREEN, appSettings.isFullscreen)
        settings.putFloat(KEY_MUSIC_VOL, appSettings.musicVolume)
        settings.putFloat(KEY_SFX_VOL, appSettings.sfxVolume)
        settings.putString(KEY_LANGUAGE, appSettings.language.name)
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
