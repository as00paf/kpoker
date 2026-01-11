package com.pafoid.kpoker.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class Settings(
    val isFullscreen: Boolean = true,
    val musicVolume: Float = 0.5f,
    val sfxVolume: Float = 0.7f
)
