package com.example.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import com.example.data.model.AmbientSound
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.Random
import kotlin.math.sin

class AmbientSoundGenerator {
    private var audioTrack: AudioTrack? = null
    private var playbackJob: Job? = null
    private var isPlaying = false
    private val scope = CoroutineScope(Dispatchers.Default)

    fun start(sound: AmbientSound) {
        stop()
        if (sound == AmbientSound.NONE) return

        isPlaying = true
        val sampleRate = 22050
        val bufferSize = AudioTrack.getMinBufferSize(
            sampleRate,
            AudioFormat.CHANNEL_OUT_MONO,
            AudioFormat.ENCODING_PCM_16BIT
        ) * 2

        try {
            audioTrack = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(sampleRate)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build()
                )
                .setBufferSizeInBytes(bufferSize)
                .setTransferMode(AudioTrack.MODE_STREAM)
                .build()

            audioTrack?.play()

            playbackJob = scope.launch {
                val buffer = ShortArray(1024)
                val random = Random()
                var lastBrownValue = 0.0
                var phase = 0.0

                while (isActive && isPlaying) {
                    when (sound) {
                        AmbientSound.RAIN -> {
                            // Synthesize brown/pink filtered noise for soothing rain
                            for (i in buffer.indices) {
                                val white = (random.nextDouble() * 2.0 - 1.0)
                                lastBrownValue = (lastBrownValue + (0.02 * white)) / 1.02
                                // Subtle random drops
                                val drop = if (random.nextFloat() < 0.003f) (random.nextDouble() * 0.4) else 0.0
                                val sample = ((lastBrownValue * 3.5) + drop).coerceIn(-0.95, 0.95)
                                buffer[i] = (sample * 16000.0).toInt().toShort()
                            }
                        }
                        AmbientSound.LOFI -> {
                            // Synthesize mellow ambient pentatonic chord progression
                            val notes = doubleArrayOf(130.81, 164.81, 196.00, 220.00) // C3, E3, G3, A3
                            val timeSeconds = System.currentTimeMillis() / 1000.0
                            val chordIndex = ((timeSeconds / 3.0).toInt()) % notes.size
                            val baseFreq = notes[chordIndex]

                            for (i in buffer.indices) {
                                phase += (2.0 * Math.PI * baseFreq) / sampleRate
                                val sample1 = sin(phase) * 0.4
                                val sample2 = sin(phase * 1.5) * 0.2 // 5th harmonic
                                val subtleVinylNoise = (random.nextDouble() * 2.0 - 1.0) * 0.03
                                val sample = (sample1 + sample2 + subtleVinylNoise).coerceIn(-0.9, 0.9)
                                buffer[i] = (sample * 14000.0).toInt().toShort()
                            }
                        }
                        AmbientSound.NONE -> break
                    }
                    audioTrack?.write(buffer, 0, buffer.size)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun stop() {
        isPlaying = false
        playbackJob?.cancel()
        playbackJob = null
        try {
            audioTrack?.pause()
            audioTrack?.flush()
            audioTrack?.stop()
            audioTrack?.release()
        } catch (_: Exception) {}
        audioTrack = null
    }
}
