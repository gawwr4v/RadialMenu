package io.github.gawwr4v.radialmenu

import androidx.compose.ui.geometry.Offset
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
        val distanceSq = dragX * dragX + dragY * dragY
        val deadZoneSq = deadZonePx * deadZonePx
        if (distanceSq < deadZoneSq) return null

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

    /**
     * Zones describing where on the screen the touch occurred.
     *
     * Corner zones trigger the edge-hug layout when the item count
     * exceeds [RadialMenuDefaults.CORNER_ITEM_THRESHOLD].
     *
     * @since 1.0.3
     */
    enum class MenuZone {
        CENTER,
        CORNER_TOP_LEFT,
        CORNER_TOP_RIGHT,
        CORNER_BOTTOM_LEFT,
        CORNER_BOTTOM_RIGHT
    }

    /**
     * Detects which screen zone the touch point falls in.
     *
     * Corners are checked first (both x AND y within [edgeThreshPx]
     * of the respective edges). Anything else returns [MenuZone.CENTER].
     *
     * @param x Touch X position in pixels.
     * @param y Touch Y position in pixels.
     * @param screenWidth Screen width in pixels.
     * @param screenHeight Screen height in pixels.
     * @param edgeThreshPx Pixel threshold for corner detection.
     * @return The [MenuZone] the touch falls in.
     * @since 1.0.3
     */
    fun detectZone(
        x: Float,
        y: Float,
        screenWidth: Float,
        screenHeight: Float,
        edgeThreshPx: Float
    ): MenuZone {
        val nearLeft = x < edgeThreshPx
        val nearRight = x > screenWidth - edgeThreshPx
        val nearTop = y < edgeThreshPx
        val nearBottom = y > screenHeight - edgeThreshPx

        return when {
            nearLeft && nearTop -> MenuZone.CORNER_TOP_LEFT
            nearRight && nearTop -> MenuZone.CORNER_TOP_RIGHT
            nearLeft && nearBottom -> MenuZone.CORNER_BOTTOM_LEFT
            nearRight && nearBottom -> MenuZone.CORNER_BOTTOM_RIGHT
            else -> MenuZone.CENTER
        }
    }

    /**
     * Computes item center positions for the edge-hug L-shaped layout.
     *
     * Items are split across the two available edges adjacent to the corner.
     * The primary edge gets `ceil(itemCount / 2)` items, the secondary gets
     * the remainder. The corner cell itself (the intersection of the two edges)
     * is always vacant — items start one full step away from the corner.
     *
     * @param zone Which corner the touch is in. Must be a CORNER_* value.
     * @param screenWidth Screen width in pixels.
     * @param screenHeight Screen height in pixels.
     * @param itemCount Number of items to position.
     * @param itemSizePx Item diameter in pixels.
     * @param gapPx Gap between items in pixels.
     * @param padPx Padding from screen edge in pixels.
     * @return List of [Offset] item center positions, primary edge first.
     * @since 1.0.3
     */
    fun edgeHugLayout(
        zone: MenuZone,
        screenWidth: Float,
        screenHeight: Float,
        itemCount: Int,
        itemSizePx: Float,
        gapPx: Float,
        padPx: Float
    ): List<Offset> {
        if (itemCount == 0) return emptyList()

        val step = itemSizePx + gapPx
        val offset = step // skip the corner cell
        val half = itemSizePx / 2f

        val primaryCount = (itemCount + 1) / 2 // ceil(itemCount / 2)
        val secondaryCount = itemCount - primaryCount

        val positions = mutableListOf<Offset>()

        when (zone) {
            MenuZone.CORNER_TOP_LEFT -> {
                // Primary: top edge, flowing right
                for (i in 0 until primaryCount) {
                    positions.add(Offset(padPx + offset + (i * step) + half, padPx + half))
                }
                // Secondary: left edge, flowing down
                for (i in 0 until secondaryCount) {
                    positions.add(Offset(padPx + half, padPx + offset + (i * step) + half))
                }
            }
            MenuZone.CORNER_TOP_RIGHT -> {
                // Primary: top edge, flowing left
                for (i in 0 until primaryCount) {
                    positions.add(Offset(screenWidth - padPx - offset - (i * step) - half, padPx + half))
                }
                // Secondary: right edge, flowing down
                for (i in 0 until secondaryCount) {
                    positions.add(Offset(screenWidth - padPx - half, padPx + offset + (i * step) + half))
                }
            }
            MenuZone.CORNER_BOTTOM_LEFT -> {
                // Primary: bottom edge, flowing right
                for (i in 0 until primaryCount) {
                    positions.add(Offset(padPx + offset + (i * step) + half, screenHeight - padPx - half))
                }
                // Secondary: left edge, flowing up
                for (i in 0 until secondaryCount) {
                    positions.add(Offset(padPx + half, screenHeight - padPx - offset - (i * step) - half))
                }
            }
            MenuZone.CORNER_BOTTOM_RIGHT -> {
                // Primary: bottom edge, flowing left
                for (i in 0 until primaryCount) {
                    positions.add(Offset(screenWidth - padPx - offset - (i * step) - half, screenHeight - padPx - half))
                }
                // Secondary: right edge, flowing up
                for (i in 0 until secondaryCount) {
                    positions.add(Offset(screenWidth - padPx - half, screenHeight - padPx - offset - (i * step) - half))
                }
            }
            MenuZone.CENTER -> {
                // Should not be called for CENTER, but return empty for safety
            }
        }

        // Safety clamp: ensure no item center is closer than half from any edge,
        // guaranteeing the full visual circle stays within screen bounds.
        return positions.map { pos ->
            Offset(
                pos.x.coerceIn(half, screenWidth - half),
                pos.y.coerceIn(half, screenHeight - half)
            )
        }
    }

    /**
     * Selects the nearest item by Euclidean distance from the pointer.
     *
     * This replaces angle-based selection when the menu is in edge-hug mode,
     * since items are no longer arranged radially.
     *
     * @param pointerX Pointer X position in pixels (absolute screen coords).
     * @param pointerY Pointer Y position in pixels (absolute screen coords).
     * @param itemPositions List of item center positions from [edgeHugLayout].
     * @param deadZonePx Maximum distance from the nearest item center required
     *   to register selection. Returns null if the pointer is farther than this threshold.
     * @return Index of the nearest item, or null if no item is within [deadZonePx].
     * @since 1.0.3
     */
    fun getNearestItemSelection(
        pointerX: Float,
        pointerY: Float,
        itemPositions: List<Offset>,
        deadZonePx: Float = RadialMenuDefaults.DEAD_ZONE_PX
    ): Int? {
        if (itemPositions.isEmpty()) return null

        var closestIndex = 0
        var closestDistSq = Float.MAX_VALUE

        for (i in itemPositions.indices) {
            val dx = pointerX - itemPositions[i].x
            val dy = pointerY - itemPositions[i].y
            val distSq = dx * dx + dy * dy
            if (distSq < closestDistSq) {
                closestDistSq = distSq
                closestIndex = i
            }
        }

        val deadZoneSq = deadZonePx * deadZonePx
        return if (closestDistSq <= deadZoneSq) closestIndex else null
    }
}
