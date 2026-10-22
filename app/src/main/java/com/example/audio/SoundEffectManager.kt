package com.example.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import com.example.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SoundEffectManager(private val context: Context) {

    private val soundPool: SoundPool
    private var tapSoundId: Int = 0
    private var selectSoundId: Int = 0
    private val scope = CoroutineScope(Dispatchers.Default)

    init {
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        soundPool = SoundPool.Builder()
            .setMaxStreams(5)
            .setAudioAttributes(audioAttributes)
            .build()

        try {
            tapSoundId = soundPool.load(context, R.raw.ui_tap, 1)
            selectSoundId = soundPool.load(context, R.raw.ui_select, 1)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Subtle haptic feedback for UI interactions.
     */
    fun performHaptic(durationMs: Long = 20) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vibratorManager?.defaultVibrator?.vibrate(
                    VibrationEffect.createOneShot(durationMs, VibrationEffect.DEFAULT_AMPLITUDE)
                )
            } else {
                @Suppress("DEPRECATION")
                val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                @Suppress("DEPRECATION")
                vibrator?.vibrate(durationMs)
            }
        } catch (_: Exception) {}
    }

    /**
     * Play light click sound for buttons/checkboxes + subtle haptic.
     */
    fun playUiClick() {
        performHaptic(20)
        try {
            if (tapSoundId != 0) {
                soundPool.play(tapSoundId, 0.7f, 0.7f, 1, 0, 1.0f)
            }
        } catch (_: Exception) {}
    }

    /**
     * Play selection pop sound + haptic.
     */
    fun playUiSelect() {
        performHaptic(30)
        try {
            if (selectSoundId != 0) {
                soundPool.play(selectSoundId, 0.85f, 0.85f, 1, 0, 1.0f)
            }
        } catch (_: Exception) {}
    }

    /**
     * Play soft triumphant chime when a tree successfully finishes growing.
     * Generates a warm ascending major chord chime (C5 -> E5 -> G5 -> C6).
     */
    fun playTreeGrownChime() {
        performHaptic(100)
        scope.launch {
            try {
                playToneSequence(listOf(523.25, 659.25, 783.99, 1046.50), durationMs = 180, isAscending = true)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    /**
     * Play sad sound when a tree dies / withers.
     * Generates a somber descending minor progression (G4 -> Eb4 -> C4).
     */
    fun playTreeDiedSound() {
        performHaptic(350)
        scope.launch {
            try {
                playToneSequence(listOf(392.00, 311.13, 261.63, 196.00), durationMs = 280, isAscending = false)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun playToneSequence(frequencies: List<Double>, durationMs: Int, isAscending: Boolean) {
        val sampleRate = 22050
        val totalSamplesPerTone = (sampleRate * (durationMs / 1000.0)).toInt()
        val totalBuffer = ShortArray(totalSamplesPerTone * frequencies.size)

        var offset = 0
        for (freq in frequencies) {
            for (i in 0 until totalSamplesPerTone) {
                val t = i.toDouble() / sampleRate
                // Envelope (attack + gentle decay)
                val envelope = when {
                    i < totalSamplesPerTone * 0.15 -> i / (totalSamplesPerTone * 0.15)
                    else -> 1.0 - (i.toDouble() / totalSamplesPerTone)
                }
                // Fundamental + soft harmonic
                val sample = (kotlin.math.sin(2.0 * Math.PI * freq * t) * 0.75 +
                        kotlin.math.sin(4.0 * Math.PI * freq * t) * 0.25) * envelope
                totalBuffer[offset++] = (sample * Short.MAX_VALUE * 0.45).toInt().toShort()
            }
        }

        val track = android.media.AudioTrack.Builder()
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
            )
            .setAudioFormat(
                android.media.AudioFormat.Builder()
                    .setEncoding(android.media.AudioFormat.ENCODING_PCM_16BIT)
                    .setSampleRate(sampleRate)
                    .setChannelMask(android.media.AudioFormat.CHANNEL_OUT_MONO)
                    .build()
            )
            .setBufferSizeInBytes(totalBuffer.size * 2)
            .setTransferMode(android.media.AudioTrack.MODE_STATIC)
            .build()

        track.write(totalBuffer, 0, totalBuffer.size)
        track.play()
    }

    fun release() {
        try {
            soundPool.release()
        } catch (_: Exception) {}
    }
}
