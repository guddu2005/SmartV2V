package com.smartv2v.app.audio

import android.content.Context
import android.media.AudioManager
import android.media.ToneGenerator
import android.speech.tts.TextToSpeech
import java.util.Locale

class VoiceAlertManager(private val context: Context) : TextToSpeech.OnInitListener {
    private var tts: TextToSpeech? = null
    private var isTtsReady = false
    private var toneGenerator: ToneGenerator? = null
    private var lastSpokenTime = 0L

    init {
        try {
            toneGenerator = ToneGenerator(AudioManager.STREAM_ALARM, 95)
            tts = TextToSpeech(context.applicationContext, this)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            tts?.language = Locale.US
            tts?.setSpeechRate(1.05f)
            tts?.setPitch(1.0f)
            isTtsReady = true
        }
    }

    fun triggerWarning(riskLevel: String, distance: Double, ttc: Double) {
        if (riskLevel.equals("HIGH", ignoreCase = true)) {
            // Play alarm tone
            try {
                toneGenerator?.startTone(ToneGenerator.TONE_CDMA_EMERGENCY_RINGBACK, 350)
            } catch (e: Exception) {
                e.printStackTrace()
            }

            // Debounce spoken voice warning (every 4 seconds)
            val now = System.currentTimeMillis()
            if (now - lastSpokenTime > 4000L && isTtsReady) {
                lastSpokenTime = now
                val distMeters = distance.toInt()
                val message = if (distMeters in 1..40) {
                    "Warning! Vehicle $distMeters meters ahead. Brake now!"
                } else {
                    "Warning! High collision risk. Brake immediately!"
                }
                tts?.speak(message, TextToSpeech.QUEUE_FLUSH, null, "v2v_hazard_voice")
            }
        }
    }

    fun stop() {
        try {
            tts?.stop()
            toneGenerator?.stopTone()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun release() {
        stop()
        tts?.shutdown()
        tts = null
        toneGenerator?.release()
        toneGenerator = null
    }
}
