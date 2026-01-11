package com.pafoid.kpoker.audio

import javazoom.jl.player.Player
import kotlinx.coroutines.*
import java.io.InputStream

class JvmAudioPlayer(private val scope: CoroutineScope) : AudioPlayer {
    private var musicJob: Job? = null
    private var musicVolume = 0.5f
    private var sfxVolume = 0.7f

    override fun playMusic(resourcePath: String, volume: Float, loop: Boolean) {
        musicVolume = volume
        musicJob?.cancel()
        musicJob = scope.launch(Dispatchers.IO) {
            while (isActive) {
                try {
                    val inputStream = getResourceStream(resourcePath)
                    if (inputStream != null) {
                        val player = Player(inputStream)
                        player.play()
                    } else {
                        println("Music resource not found: $resourcePath")
                        break
                    }
                } catch (e: Exception) {
                    println("Error playing music: ${e.message}")
                    break
                }
                if (!loop) break
            }
        }
    }

    override fun playSound(resourcePath: String, volume: Float) {
        scope.launch(Dispatchers.IO) {
            try {
                val inputStream = getResourceStream(resourcePath)
                if (inputStream != null) {
                    val player = Player(inputStream)
                    player.play()
                }
            } catch (e: Exception) {
                println("Error playing sound: ${e.message}")
            }
        }
    }

    override fun setMusicVolume(volume: Float) {
        musicVolume = volume
        // JLayer doesn't support easy volume control mid-stream
        // In a real app, we'd use a more advanced library or a gain control wrapper
    }

    override fun setSfxVolume(volume: Float) {
        sfxVolume = volume
    }

    override fun stopMusic() {
        musicJob?.cancel()
        musicJob = null
    }

    private fun getResourceStream(path: String): InputStream? {
        // Compose resources are usually in the classpath
        return this::class.java.classLoader.getResourceAsStream(path) ?:
               this::class.java.classLoader.getResourceAsStream("files/$path") ?:
               this::class.java.classLoader.getResourceAsStream("drawable/$path")
    }
}

actual fun createAudioPlayer(scope: CoroutineScope): AudioPlayer = JvmAudioPlayer(scope)