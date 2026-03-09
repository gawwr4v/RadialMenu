package io.github.gawwr4v.radialmenu

import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Core mathematical functions for RadialMenu positioning and selection.
 *
 * These functions are public to allow developers building custom
 * RadialMenu implementations to reuse the positioning algorithms.
 *
 * The key algorithm is [calculateCenterAngle] which ensures menu items
 * never appear off-screen regardless of where the user touches.
 *
 * @since 1.0.0
 */
object RadialMenuMath {

    /**
     * Calculates the optimal center angle for the radial menu fan
     * based on touch position, ensuring items always appear on-screen.
     *
     * When the touch is in the lower ~70% of the screen, the fan points
     * **upward** (away from the finger). When near the top edge, the fan
     * flips **downward** so items don't clip off-screen.
     *
     * Horizontal edge-boost tilts items away from the nearest screen edge.
     *
     * @param x Touch X position in pixels.
     * @param y Touch Y position in pixels.
     * @param screenWidth Screen/container width in pixels.
     * @param screenHeight Screen/container height in pixels.
     * @param isRtl Whether the layout direction is right-to-left.
     * @return Angle in degrees (0 = right, 90 = down, 180 = left, 270 = up).
     */
    fun calculateCenterAngle(
        x: Float,
        y: Float,
        screenWidth: Float,
        screenHeight: Float,
        isRtl: Boolean = false
    ): Float {
        val safeWidth = screenWidth.coerceAtLeast(1f)
        val safeHeight = screenHeight.coerceAtLeast(1f)
        
        // Mirror the X coordinate if the layout is RTL. This allows the math to act as if it is LTR.
        val effectiveX = if (isRtl) safeWidth - x else x
        val xRatio = (effectiveX / safeWidth).coerceIn(0f, 1f)
        val yRatio = (y / safeHeight).coerceIn(0f, 1f)

        // If the user taps near the top of the screen, we must flip the menu so it points DOWNWARD.
        // Otherwise, the items would render off the top edge of the device.
        if (yRatio < 0.3f) {
            // Base angle interpolates from 30 (down-right) to 90 (straight down) to 150 (down-left)
            val baseAngle = 30f + (xRatio * 120f)

            // If the tap is exceptionally close to the horizontal edges, we drastically tilt 
            // the menu inward to prevent clipping the outermost items.
            val edgeBoost = when {
                xRatio < 0.15f -> (0.15f - xRatio) * -100f
                xRatio > 0.85f -> (xRatio - 0.85f) * 100f
                else -> 0f
            }

            return (baseAngle + edgeBoost).coerceIn(15f, 165f)
        }

        // Normal case where the tap is in the lower 70% of the screen.
        // The menu points UPWARD, keeping the icons visible above the thumb.
        // Base angle interpolates from 330 (up-right) to 270 (straight up) to 210 (up-left)
        val baseAngle = 330f - (xRatio * 120f)

        // Dynamic edge protection for upward pointing menus.
        val edgeBoost = when {
            xRatio < 0.15f -> (0.15f - xRatio) * 100f
            xRatio > 0.85f -> (xRatio - 0.85f) * -100f
            else -> 0f
        }

        // Minor adjustment if tapping moderately high on the screen, to shift items a bit more inward.
        val topAdjust = if (yRatio < 0.4f) {
            if (xRatio < 0.5f) -15f else 15f
        } else 0f

        // Ensure the final angle never points below the horizontal axis to prevent thumb occlusion.
        return (baseAngle + edgeBoost + topAdjust).coerceIn(195f, 345f)
    }

    /**
     * Normalizes any angle to the 0 to 360 degree range.
     *
     * @param angle Input angle in degrees (any value).
     * @return Normalized angle safely inside the 0 to 360 boundary.
     */
    fun normalizeAngle(angle: Float): Float = ((angle % 360f) + 360f) % 360f

    /**
     * Determines which item index the user is dragging toward.
     *
     * The algorithm computes the angle of the drag vector and finds the
     * closest menu item. If the angular distance exceeds [selectionDeadZoneDeg],
     * no item is selected.
     *
     * @param dragX Horizontal drag delta from touch origin in pixels.
     * @param dragY Vertical drag delta from touch origin in pixels.
     * @param centerAngle The menu's center angle from [calculateCenterAngle].
     * @param itemCount Number of items in the menu.
     * @param spreadDegrees Angular spacing between items.
     * @param deadZonePx Minimum drag distance to register.
     * @param selectionDeadZoneDeg Maximum angle from nearest item to count as selected.
     * @return Index of the selected item, or null if in dead zone or no match.
     */
    fun getSelectionFromDrag(
        dragX: Float,
        dragY: Float,
        centerAngle: Float,
        itemCount: Int,
        spreadDegrees: Float = RadialMenuDefaults.ICON_SPREAD_DEGREES,
        deadZonePx: Float = RadialMenuDefaults.DEAD_ZONE_PX,
        selectionDeadZoneDeg: Float = RadialMenuDefaults.SELECTION_DEAD_ZONE_DEG
    ): Int? {
        if (itemCount == 0) return null
        val distance = sqrt(dragX * dragX + dragY * dragY)
        if (distance < deadZonePx) return null

        val rawAngle = Math.toDegrees(atan2(dragY.toDouble(), dragX.toDouble())).toFloat()
        val dragAngle = normalizeAngle(rawAngle)

        var closestIndex: Int? = null
        var closestDist = Float.MAX_VALUE

        for (i in 0 until itemCount) {
            val itemAngle = normalizeAngle(centerAngle + ((i - (itemCount - 1) / 2f) * spreadDegrees))
            var diff = abs(normalizeAngle(dragAngle - itemAngle))
            if (diff > 180f) diff = 360f - diff
            if (diff < closestDist) {
                closestDist = diff
                closestIndex = i
            }
        }

        return if (closestDist <= selectionDeadZoneDeg) closestIndex else null
    }
}
