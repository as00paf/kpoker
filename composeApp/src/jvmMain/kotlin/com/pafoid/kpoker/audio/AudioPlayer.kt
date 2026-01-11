package com.pafoid.kpoker.audio

import korlibs.audio.sound.*
import korlibs.io.file.std.*
import kotlinx.coroutines.*
import java.io.File

class JvmAudioPlayer(private val scope: CoroutineScope) : AudioPlayer {
    private var musicChannel: SoundChannel? = null
    private var musicVolume = 0.5f
    private var sfxVolume = 0.7f

    override fun playMusic(resourcePath: String, volume: Float, loop: Boolean) {
        musicVolume = volume
        scope.launch {
            try {
                // In a real KMP app, resources are handled differently, but for JVM:
                val sound = resourcesVfs[resourcePath].readSound()
                musicChannel?.stop()
                musicChannel = sound.play(scope, if (loop) InfinitePlaybackTimes else PlaybackTimes(1))
                musicChannel?.volume = volume.toDouble()
            } catch (e: Exception) {
                println("Failed to play music: ${e.message}")
            }
        }
    }

    override fun playSound(resourcePath: String, volume: Float) {
        scope.launch {
            try {
                val sound = resourcesVfs[resourcePath].readSound()
                val channel = sound.play(scope)
                channel.volume = volume.toDouble() * sfxVolume
            } catch (e: Exception) {
                println("Failed to play sound: ${e.message}")
            }
        }
    }

    override fun setMusicVolume(volume: Float) {
        musicVolume = volume
        musicChannel?.volume = volume.toDouble()
    }

    override fun setSfxVolume(volume: Float) {
        sfxVolume = volume
    }

    override fun stopMusic() {
        musicChannel?.stop()
    }
}

actual fun createAudioPlayer(scope: CoroutineScope): AudioPlayer = JvmAudioPlayer(scope)
