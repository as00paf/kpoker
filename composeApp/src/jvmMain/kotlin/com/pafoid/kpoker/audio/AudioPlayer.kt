package com.pafoid.kpoker.audio

import javazoom.jl.decoder.Bitstream
import javazoom.jl.decoder.Decoder
import javazoom.jl.decoder.SampleBuffer
import kotlinx.coroutines.*
import java.io.InputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import javax.sound.sampled.*

class JvmAudioPlayer(private val scope: CoroutineScope) : AudioPlayer {
    private var musicJob: Job? = null
    private var currentMusicPath: String? = null
    private var musicVolume = 0.5f
    private var sfxVolume = 0.7f
    private var musicLine: SourceDataLine? = null

    override fun playMusic(resourcePath: String, volume: Float, loop: Boolean) {
        if (currentMusicPath == resourcePath) return
        
        musicVolume = volume
        currentMusicPath = resourcePath
        stopMusic()
        
        musicJob = scope.launch(Dispatchers.IO) {
            while (isActive) {
                try {
                    val inputStream = getResourceStream(resourcePath) ?: break
                    playMp3WithVolume(inputStream, musicVolume, true, this)
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
                val inputStream = getResourceStream(resourcePath) ?: return@launch
                playMp3WithVolume(inputStream, volume * sfxVolume, false, this)
            } catch (e: Exception) {
                println("Error playing sound: ${e.message}")
            }
        }
    }

    private fun playMp3WithVolume(inputStream: InputStream, volume: Float, isMusic: Boolean, playScope: CoroutineScope) {
        try {
            val bitstream = Bitstream(inputStream)
            val decoder = Decoder()
            var line: SourceDataLine? = null
            
            while (playScope.isActive) {
                val frame = bitstream.readFrame() ?: break
                val sampleBuffer = decoder.decodeFrame(frame, bitstream) as SampleBuffer
                
                if (line == null) {
                    // Standard MP3 sample rate is usually 44100
                    val format = AudioFormat(44100f, 16, 2, true, false)
                    val info = DataLine.Info(SourceDataLine::class.java, format)
                    line = AudioSystem.getLine(info) as SourceDataLine
                    line.open(format)
                    line.start()
                    if (isMusic) musicLine = line
                }
                
                setLineVolume(line, volume)
                
                val pcm = sampleBuffer.buffer
                val pcmBytes = ByteBuffer.allocate(pcm.size * 2).apply {
                    order(ByteOrder.LITTLE_ENDIAN)
                    asShortBuffer().put(pcm)
                }.array()
                
                line.write(pcmBytes, 0, pcmBytes.size)
                bitstream.closeFrame()
            }
            
            line?.drain()
            line?.close()
        } catch (e: Exception) {
            println("Audio playback error: ${e.message}")
        }
    }

    private fun setLineVolume(line: SourceDataLine, volume: Float) {
        try {
            if (line.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
                val gainControl = line.getControl(FloatControl.Type.MASTER_GAIN) as FloatControl
                val dB = (Math.log10(maxOf(volume.toDouble(), 0.0001)) * 20.0).toFloat()
                gainControl.value = maxOf(minOf(dB, gainControl.maximum), gainControl.minimum)
            }
        } catch (e: Exception) {}
    }

    override fun setMusicVolume(volume: Float) {
        musicVolume = volume
        musicLine?.let { setLineVolume(it, volume) }
    }

    override fun setSfxVolume(volume: Float) {
        sfxVolume = volume
    }

    override fun stopMusic() {
        musicJob?.cancel()
        musicJob = null
        musicLine?.stop()
        musicLine?.close()
        musicLine = null
        currentMusicPath = null
    }

    private fun getResourceStream(path: String): InputStream? {
        val classLoader = Thread.currentThread().contextClassLoader ?: this::class.java.classLoader
        return classLoader.getResourceAsStream(path) ?:
               classLoader.getResourceAsStream("/$path")
    }
}

actual fun createAudioPlayer(scope: CoroutineScope): AudioPlayer = JvmAudioPlayer(scope)
