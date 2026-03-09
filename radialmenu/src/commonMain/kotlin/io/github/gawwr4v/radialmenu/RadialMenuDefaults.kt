package io.github.gawwr4v.radialmenu

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.Spring

/**
 * Default constants and dimensions used in the radial menu components.
 *
 * @since 1.0.0
 */
object RadialMenuDefaults {
    /** Duration before a press is considered a long press (ms). */
    const val LONG_PRESS_TIMEOUT_MS = 400L

    /** Maximum interval between two taps for a double-tap (ms). */
    const val DOUBLE_TAP_TIMEOUT_MS = 300L

    /** Minimum drag distance (px) before selection starts. */
    const val DEAD_ZONE_PX = 50f

    /** Default menu radius from touch center to icon centers (dp). */
    const val MENU_RADIUS_DP = 90f

    /** Default icon size (dp). */
    const val ICON_SIZE_DP = 32f

    /** Angular spacing between adjacent items (degrees). */
    const val ICON_SPREAD_DEGREES = 45f

    /** Maximum angular distance from an item to still count as selected (degrees). */
    const val SELECTION_DEAD_ZONE_DEG = 30f

    /** Default animation duration for open/close transitions (ms). */
    const val ANIMATION_DURATION_MS = 300

    /** Default item scale when selected/hovered. */
    const val ITEM_SCALE_SELECTED = 1.4f

    /** Default item scale at rest. */
    const val ITEM_SCALE_NORMAL = 1.0f

    /** Alpha of the drag direction indicator line. */
    const val DRAG_INDICATOR_ALPHA = 0.7f
}
