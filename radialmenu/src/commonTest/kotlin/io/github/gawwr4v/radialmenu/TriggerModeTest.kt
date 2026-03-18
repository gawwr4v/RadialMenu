package io.github.gawwr4v.radialmenu

import androidx.compose.ui.input.key.Key
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class TriggerModeTest {
    @Test
    fun longPress_positionAware_default_true() {
        assertTrue(RadialMenuTriggerMode.LongPress().positionAware)
    }

    @Test
    fun secondaryClick_positionAware_default_false() {
        assertFalse(RadialMenuTriggerMode.SecondaryClick().positionAware)
    }

    @Test
    fun keyboardHold_positionAware_alwaysFalse() {
        assertFalse(resolvePositionAware(RadialMenuTriggerMode.KeyboardHold()))
    }

    @Test
    fun keyboardHold_edgeHugAlwaysSkipped() {
        val withEdgeHugEnabled = shouldUseEdgeHugLayout(
            enableEdgeHugLayout = true,
            isCenterSpawned = true,
            zone = RadialMenuMath.MenuZone.CORNER_TOP_LEFT,
            itemsCount = 8
        )
        val withEdgeHugDisabled = shouldUseEdgeHugLayout(
            enableEdgeHugLayout = false,
            isCenterSpawned = true,
            zone = RadialMenuMath.MenuZone.CORNER_TOP_LEFT,
            itemsCount = 8
        )

        assertFalse(withEdgeHugEnabled)
        assertFalse(withEdgeHugDisabled)
    }

    @Test
    fun secondaryClick_edgeHugFollowsFlag() {
        val enabled = shouldUseEdgeHugLayout(
            enableEdgeHugLayout = true,
            isCenterSpawned = false,
            zone = RadialMenuMath.MenuZone.CORNER_TOP_LEFT,
            itemsCount = 4
        )
        val disabled = shouldUseEdgeHugLayout(
            enableEdgeHugLayout = false,
            isCenterSpawned = false,
            zone = RadialMenuMath.MenuZone.CORNER_TOP_LEFT,
            itemsCount = 4
        )

        assertTrue(enabled)
        assertFalse(disabled)
    }

    @Test
    fun longPress_edgeHugFollowsFlag() {
        val enabled = shouldUseEdgeHugLayout(
            enableEdgeHugLayout = true,
            isCenterSpawned = false,
            zone = RadialMenuMath.MenuZone.CORNER_TOP_RIGHT,
            itemsCount = 4
        )
        val disabled = shouldUseEdgeHugLayout(
            enableEdgeHugLayout = false,
            isCenterSpawned = false,
            zone = RadialMenuMath.MenuZone.CORNER_TOP_RIGHT,
            itemsCount = 4
        )

        assertTrue(enabled)
        assertFalse(disabled)
    }

    @Test
    fun keyboardHold_defaultKey_isQ() {
        assertEquals(Key.Q, RadialMenuTriggerMode.KeyboardHold().key)
    }

    @Test
    fun keyboardHold_customKey() {
        assertEquals(Key.W, RadialMenuTriggerMode.KeyboardHold(Key.W).key)
    }

    @Test
    fun positionAware_false_centerAngleIsZero() {
        val angle = resolveCenterAngle(
            isPositionAware = false,
            position = androidx.compose.ui.geometry.Offset(100f, 200f),
            containerWidth = 500f,
            containerHeight = 1000f,
            isRtl = false
        )
        assertEquals(0f, angle)
    }

    @Test
    fun positionAware_true_centerAngleFromMath() {
        val position = androidx.compose.ui.geometry.Offset(100f, 200f)
        val expected = RadialMenuMath.calculateCenterAngle(
            x = position.x,
            y = position.y,
            screenWidth = 500f,
            screenHeight = 1000f,
            isRtl = false
        )

        val actual = resolveCenterAngle(
            isPositionAware = true,
            position = position,
            containerWidth = 500f,
            containerHeight = 1000f,
            isRtl = false
        )

        assertEquals(expected, actual)
    }

    @Test
    fun keyboardHold_keyDown_opensMenu_noSelection() {
        assertTrue(keyboardHoldShouldOpenMenu(isKeyboardMenuOpen = false))
        assertFalse(keyboardHoldShouldOpenMenu(isKeyboardMenuOpen = true))
        assertEquals(
            null,
            keyboardHoldCommittedSelection(
                isKeyboardMenuOpen = true,
                hoveredItemIndex = null
            )
        )
    }

    @Test
    fun keyboardHold_cursorMove_updatesHoverByAngle() {
        val center = androidx.compose.ui.geometry.Offset(100f, 100f)
        val itemCount = 4
        val itemPositions = listOf(
            androidx.compose.ui.geometry.Offset(40f, 100f),
            androidx.compose.ui.geometry.Offset(100f, 40f),
            androidx.compose.ui.geometry.Offset(160f, 100f),
            androidx.compose.ui.geometry.Offset(100f, 160f)
        )
        val hoveredIndex = keyboardHoldHoverSelectionFromPointer(
            isCenterSpawned = true,
            pointer = androidx.compose.ui.geometry.Offset(170f, 100f),
            selectionOrigin = center,
            centerAngle = centerSpawnedCenterAngle(itemCount),
            itemCount = itemCount,
            itemSpreadDegrees = keyboardHoldSpreadDegrees(itemCount),
            itemPositions = itemPositions,
            centerDeadZonePx = RadialMenuDefaults.DEAD_ZONE_PX,
            nearestDeadZonePx = 20f
        )
        assertEquals(0, hoveredIndex)
    }

    @Test
    fun keyboardHold_cursorMove_deadZone_noUpdate() {
        val center = androidx.compose.ui.geometry.Offset(100f, 100f)
        val hoveredIndex = keyboardHoldHoverSelectionFromPointer(
            isCenterSpawned = true,
            pointer = androidx.compose.ui.geometry.Offset(105f, 105f),
            selectionOrigin = center,
            centerAngle = centerSpawnedCenterAngle(4),
            itemCount = 4,
            itemSpreadDegrees = keyboardHoldSpreadDegrees(4),
            itemPositions = emptyList(),
            centerDeadZonePx = RadialMenuDefaults.DEAD_ZONE_PX,
            nearestDeadZonePx = 20f
        )
        assertEquals(null, hoveredIndex)
    }

    @Test
    fun keyboardHold_keyUp_commitsHoveredItem() {
        val committed = keyboardHoldCommittedSelection(
            isKeyboardMenuOpen = true,
            hoveredItemIndex = 2
        )
        assertEquals(2, committed)
    }

    @Test
    fun keyboardHold_keyUp_noSelection_doesNotFire() {
        val committed = keyboardHoldCommittedSelection(
            isKeyboardMenuOpen = true,
            hoveredItemIndex = null
        )
        assertEquals(null, committed)
    }

    @Test
    fun centerSpawned_usesAngleSelection() {
        val center = androidx.compose.ui.geometry.Offset(100f, 100f)
        val misleadingPositions = listOf(
            androidx.compose.ui.geometry.Offset(20f, 20f),
            androidx.compose.ui.geometry.Offset(20f, 40f),
            androidx.compose.ui.geometry.Offset(20f, 60f),
            androidx.compose.ui.geometry.Offset(170f, 100f)
        )

        val hoveredIndex = keyboardHoldHoverSelectionFromPointer(
            isCenterSpawned = true,
            pointer = androidx.compose.ui.geometry.Offset(170f, 100f),
            selectionOrigin = center,
            centerAngle = centerSpawnedCenterAngle(4),
            itemCount = 4,
            itemSpreadDegrees = keyboardHoldSpreadDegrees(4),
            itemPositions = misleadingPositions,
            centerDeadZonePx = RadialMenuDefaults.DEAD_ZONE_PX,
            nearestDeadZonePx = 20f
        )

        assertEquals(0, hoveredIndex)
    }

    @Test
    fun cursorSpawned_usesNearestDistance() {
        val itemPositions = listOf(
            androidx.compose.ui.geometry.Offset(100f, 100f),
            androidx.compose.ui.geometry.Offset(160f, 100f),
            androidx.compose.ui.geometry.Offset(220f, 100f)
        )
        val hoveredIndex = keyboardHoldHoverSelectionFromPointer(
            isCenterSpawned = false,
            pointer = androidx.compose.ui.geometry.Offset(162f, 100f),
            selectionOrigin = androidx.compose.ui.geometry.Offset(100f, 100f),
            centerAngle = 0f,
            itemCount = 3,
            itemSpreadDegrees = keyboardHoldSpreadDegrees(3),
            itemPositions = itemPositions,
            centerDeadZonePx = RadialMenuDefaults.DEAD_ZONE_PX,
            nearestDeadZonePx = 30f
        )

        assertEquals(1, hoveredIndex)
    }

    @Test
    fun keyboardHold_centerSpawned_usesFlickOrigin_notMenuCenter() {
        val menuCenter = androidx.compose.ui.geometry.Offset(500f, 500f)
        val flickOrigin = androidx.compose.ui.geometry.Offset(100f, 100f)

        // Cursor is still left/up of visual center, but flicked right from origin.
        val hoveredIndex = keyboardHoldHoverSelectionFromPointer(
            isCenterSpawned = true,
            pointer = androidx.compose.ui.geometry.Offset(180f, 100f),
            selectionOrigin = flickOrigin,
            centerAngle = centerSpawnedCenterAngle(4),
            itemCount = 4,
            itemSpreadDegrees = keyboardHoldSpreadDegrees(4),
            itemPositions = listOf(menuCenter),
            centerDeadZonePx = RadialMenuDefaults.DEAD_ZONE_PX,
            nearestDeadZonePx = 30f
        )

        assertEquals(0, hoveredIndex)
    }
}
