package com.smartv2v.app.audio

import android.content.Context
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Handler
import android.os.Looper

class SirenAlertManager(private val context: Context) {
    private var toneGenerator: ToneGenerator? = null
    private var isSirenActive = false
    private val handler = Handler(Looper.getMainLooper())
    private var sirenRunnable: Runnable? = null

    init {
        try {
            toneGenerator = ToneGenerator(AudioManager.STREAM_ALARM, 100)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun startSiren() {
        if (isSirenActive) return
        isSirenActive = true

        sirenRunnable = object : Runnable {
            override fun run() {
                if (!isSirenActive) return
                try {
                    // Play loud dual-tone emergency alarm (500ms tone)
                    toneGenerator?.startTone(ToneGenerator.TONE_CDMA_EMERGENCY_RINGBACK, 500)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                // Repeat every 700ms while HIGH risk persists
                handler.postDelayed(this, 700L)
            }
        }
        handler.post(sirenRunnable!!)
    }

    fun stopSiren() {
        isSirenActive = false
        sirenRunnable?.let { handler.removeCallbacks(it) }
        try {
            toneGenerator?.stopTone()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun updateRiskLevel(riskLevel: String) {
        if (riskLevel.equals("HIGH", ignoreCase = true)) {
            startSiren()
        } else {
            stopSiren()
        }
    }

    fun release() {
        stopSiren()
        toneGenerator?.release()
        toneGenerator = null
    }
}
