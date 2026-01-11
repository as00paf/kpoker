package com.pafoid.kpoker.audio

import kotlinx.coroutines.CoroutineScope

interface AudioPlayer {
    fun playMusic(resourcePath: String, volume: Float, loop: Boolean = true)
    fun playSound(resourcePath: String, volume: Float)
    fun setMusicVolume(volume: Float)
    fun setSfxVolume(volume: Float)
    fun stopMusic()
}

expect fun createAudioPlayer(scope: CoroutineScope): AudioPlayer
