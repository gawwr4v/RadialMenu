package io.github.gawwr4v.radialmenu

import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.Spring

/**
 * Controls animation behavior of the RadialMenu.
 *
 * Use the companion [default], [snappy], [bouncy], or [slow] presets,
 * or create a fully custom configuration.
 *
 * @param openDurationMs Duration of the menu open animation in ms.
 * @param closeDurationMs Duration of the menu close animation in ms.
 * @param itemScaleDurationMs Duration of item hover scale animation in ms.
 * @param easing Compose [Easing] curve for open/close animation.
 * @param selectedItemScale Scale factor for the hovered item.
 * @param enableSpringAnimation If true, uses spring physics instead of tween.
 * @param springDampingRatio Spring damping ratio (used if [enableSpringAnimation] is true).
 * @param springStiffness Spring stiffness (used if [enableSpringAnimation] is true).
 * @throws IllegalArgumentException if [openDurationMs] ≤ 0, [selectedItemScale] ≤ 0,
 *   or [springDampingRatio] is outside 0..1.
 * @since 1.0.0
 */
data class RadialMenuAnimationConfig(
    val openDurationMs: Int = 300,
    val closeDurationMs: Int = 200,
    val itemScaleDurationMs: Int = 100,
    val easing: Easing = FastOutSlowInEasing,
    val selectedItemScale: Float = 1.4f,
    val enableSpringAnimation: Boolean = false,
    val springDampingRatio: Float = Spring.DampingRatioMediumBouncy,
    val springStiffness: Float = Spring.StiffnessMedium
) {
    init {
        require(openDurationMs > 0) { "openDurationMs must be > 0, was $openDurationMs" }
        require(closeDurationMs > 0) { "closeDurationMs must be > 0, was $closeDurationMs" }
        require(itemScaleDurationMs > 0) { "itemScaleDurationMs must be > 0, was $itemScaleDurationMs" }
        require(selectedItemScale > 0f) { "selectedItemScale must be > 0, was $selectedItemScale" }
        require(springDampingRatio in 0f..1f) { "springDampingRatio must be in 0..1, was $springDampingRatio" }
    }

    companion object {
        /** Smooth, balanced default preset. */
        fun default() = RadialMenuAnimationConfig()

        /** Fast and responsive preset. */
        fun snappy() = RadialMenuAnimationConfig(
            openDurationMs = 180,
            closeDurationMs = 120,
            itemScaleDurationMs = 80,
            easing = LinearOutSlowInEasing
        )

        /** Spring physics-based bouncy preset. */
        fun bouncy() = RadialMenuAnimationConfig(
            enableSpringAnimation = true,
            springDampingRatio = Spring.DampingRatioLowBouncy,
            springStiffness = Spring.StiffnessMediumLow
        )

        /** Deliberate, cinematic preset. */
        fun slow() = RadialMenuAnimationConfig(
            openDurationMs = 500,
            closeDurationMs = 350,
            itemScaleDurationMs = 200
        )
    }
}
