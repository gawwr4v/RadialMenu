package io.github.gawwr4v.radialmenu

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

/**
 * Desktop implementation of [HapticFeedback] which acts as a no-op as 
 * standard desktop hardware doesn't typically provide haptic motors.
 */
internal actual class HapticFeedback {
    actual fun vibrate(milliseconds: Long) {
        // No-op for Desktop 
    }
}

@Composable
internal actual fun rememberHapticFeedback(): HapticFeedback = remember { HapticFeedback() }
