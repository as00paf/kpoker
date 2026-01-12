package com.pafoid.kpoker.domain.model

import kotlinx.serialization.Serializable

@Serializable
enum class Language {
    ENGLISH, FRENCH
}

@Serializable
data class Settings(
    val isFullscreen: Boolean = false,
    val musicVolume: Float = 0.5f,
    val sfxVolume: Float = 0.7f,
    val language: Language = Language.ENGLISH
)
