package io.github.gawwr4v.radialmenu

import androidx.compose.runtime.Composable

/**
 * Haptic feedback abstraction for providing platform-specific vibration capabilities.
 */
internal expect class HapticFeedback {
    /**
     * Vibrate the device for the given number of milliseconds.
     * @param milliseconds Duration of the vibration.
     */
    fun vibrate(milliseconds: Long)
}

/**
 * Remembers a platform-specific [HapticFeedback] instance.
 */
@Composable
internal expect fun rememberHapticFeedback(): HapticFeedback
