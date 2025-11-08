package com.prettyjson.android.util

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.core.content.getSystemService

/**
 * Utility for haptic feedback
 */
object HapticFeedback {
    
    /**
     * Perform haptic feedback
     * @param context Android context
     * @param duration Duration in milliseconds (default: 50ms for light feedback)
     */
    fun perform(context: Context, duration: Long = 50) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            // Android 12+ - Use HapticFeedbackManager
            val vibratorManager = context.getSystemService<VibratorManager>()
            val vibrator = vibratorManager?.defaultVibrator
            vibrator?.vibrate(
                VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK)
            )
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Android 8+ - Use VibrationEffect
            val vibrator = context.getSystemService<Vibrator>()
            vibrator?.vibrate(
                VibrationEffect.createOneShot(duration, VibrationEffect.DEFAULT_AMPLITUDE)
            )
        } else {
            // Android 7 and below - Legacy vibration
            @Suppress("DEPRECATION")
            val vibrator = context.getSystemService<Vibrator>()
            @Suppress("DEPRECATION")
            vibrator?.vibrate(duration)
        }
    }
    
    /**
     * Light haptic feedback (for button clicks)
     */
    fun light(context: Context) {
        perform(context, 30)
    }
    
    /**
     * Medium haptic feedback (for important actions)
     */
    fun medium(context: Context) {
        perform(context, 50)
    }
    
    /**
     * Heavy haptic feedback (for errors or confirmations)
     */
    fun heavy(context: Context) {
        perform(context, 100)
    }
}

