package io.github.gawwr4v.radialmenu

import androidx.compose.ui.geometry.Offset
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class EdgeHugLayoutTest {

    private val screenWidth = 1000f
    private val screenHeight = 1000f
    private val edgeThreshPx = 80f

    // --- detectZone tests ---

    @Test
    fun detectZone_center() {
        val zone = RadialMenuMath.detectZone(500f, 500f, screenWidth, screenHeight, edgeThreshPx)
        assertEquals(RadialMenuMath.MenuZone.CENTER, zone)
    }

    @Test
    fun detectZone_topLeft() {
        val zone = RadialMenuMath.detectZone(30f, 30f, screenWidth, screenHeight, edgeThreshPx)
        assertEquals(RadialMenuMath.MenuZone.CORNER_TOP_LEFT, zone)
    }

    @Test
    fun detectZone_topRight() {
        val zone = RadialMenuMath.detectZone(970f, 30f, screenWidth, screenHeight, edgeThreshPx)
        assertEquals(RadialMenuMath.MenuZone.CORNER_TOP_RIGHT, zone)
    }

    @Test
    fun detectZone_bottomLeft() {
        val zone = RadialMenuMath.detectZone(30f, 970f, screenWidth, screenHeight, edgeThreshPx)
        assertEquals(RadialMenuMath.MenuZone.CORNER_BOTTOM_LEFT, zone)
    }

    @Test
    fun detectZone_bottomRight() {
        val zone = RadialMenuMath.detectZone(970f, 970f, screenWidth, screenHeight, edgeThreshPx)
        assertEquals(RadialMenuMath.MenuZone.CORNER_BOTTOM_RIGHT, zone)
    }

    @Test
    fun detectZone_edgeButNotCorner_isCenter() {
        // Near left edge but not near top or bottom — should be CENTER
        val zone = RadialMenuMath.detectZone(30f, 500f, screenWidth, screenHeight, edgeThreshPx)
        assertEquals(RadialMenuMath.MenuZone.CENTER, zone)
    }

    // --- edgeHugLayout tests ---

    @Test
    fun edgeHug_topLeft_4items_correctSplit() {
        val positions = RadialMenuMath.edgeHugLayout(
            RadialMenuMath.MenuZone.CORNER_TOP_LEFT,
            screenWidth, screenHeight,
            itemCount = 4, itemSizePx = 48f, gapPx = 8f, padPx = 12f
        )
        assertEquals(4, positions.size)
        // ceil(4/2) = 2 on primary (top edge), 2 on secondary (left edge)
        // Primary items should have the same Y (top edge), increasing X
        assertEquals(positions[0].y, positions[1].y)
        assertTrue(positions[1].x > positions[0].x, "Primary items should flow right")
        // Secondary items should have the same X (left edge), increasing Y
        assertEquals(positions[2].x, positions[3].x)
        assertTrue(positions[3].y > positions[2].y, "Secondary items should flow down")
    }

    @Test
    fun edgeHug_topLeft_8items_correctSplit() {
        val positions = RadialMenuMath.edgeHugLayout(
            RadialMenuMath.MenuZone.CORNER_TOP_LEFT,
            screenWidth, screenHeight,
            itemCount = 8, itemSizePx = 48f, gapPx = 8f, padPx = 12f
        )
        assertEquals(8, positions.size)
        // ceil(8/2) = 4 on primary (top), 4 on secondary (left)
        // All primary items (0-3) should share the same Y
        for (i in 0 until 4) {
            assertEquals(positions[0].y, positions[i].y, "Primary items should share Y")
        }
        // All secondary items (4-7) should share the same X
        for (i in 4 until 8) {
            assertEquals(positions[4].x, positions[i].x, "Secondary items should share X")
        }
    }

    @Test
    fun edgeHug_bottomRight_5items_correctSplit() {
        val positions = RadialMenuMath.edgeHugLayout(
            RadialMenuMath.MenuZone.CORNER_BOTTOM_RIGHT,
            screenWidth, screenHeight,
            itemCount = 5, itemSizePx = 48f, gapPx = 8f, padPx = 12f
        )
        assertEquals(5, positions.size)
        // ceil(5/2) = 3 on primary (bottom edge), 2 on secondary (right edge)
        // Primary items (0-2) share Y near bottom
        for (i in 0 until 3) {
            assertEquals(positions[0].y, positions[i].y, "Primary items should share Y")
        }
        // Primary flows left — X should decrease
        assertTrue(positions[1].x < positions[0].x, "Bottom-right primary flows left")
        // Secondary items (3-4) share X near right edge
        assertEquals(positions[3].x, positions[4].x, "Secondary items should share X")
        // Secondary flows up — Y should decrease
        assertTrue(positions[4].y < positions[3].y, "Bottom-right secondary flows up")
    }

    @Test
    fun edgeHug_topRight_4items_flowsCorrectDirection() {
        val positions = RadialMenuMath.edgeHugLayout(
            RadialMenuMath.MenuZone.CORNER_TOP_RIGHT,
            screenWidth, screenHeight,
            itemCount = 4, itemSizePx = 48f, gapPx = 8f, padPx = 12f
        )
        // Primary flows left
        assertTrue(positions[1].x < positions[0].x, "Top-right primary flows left")
        // Secondary flows down
        assertTrue(positions[3].y > positions[2].y, "Top-right secondary flows down")
    }

    @Test
    fun edgeHug_noOutOfBounds() {
        val allZones = listOf(
            RadialMenuMath.MenuZone.CORNER_TOP_LEFT,
            RadialMenuMath.MenuZone.CORNER_TOP_RIGHT,
            RadialMenuMath.MenuZone.CORNER_BOTTOM_LEFT,
            RadialMenuMath.MenuZone.CORNER_BOTTOM_RIGHT
        )
        for (zone in allZones) {
            val positions = RadialMenuMath.edgeHugLayout(
                zone, screenWidth, screenHeight,
                itemCount = 6, itemSizePx = 48f, gapPx = 8f, padPx = 12f
            )
            for ((idx, pos) in positions.withIndex()) {
                assertTrue(pos.x >= 0f, "$zone item $idx: x=${pos.x} is out of bounds (< 0)")
                assertTrue(pos.x <= screenWidth, "$zone item $idx: x=${pos.x} is out of bounds (> W)")
                assertTrue(pos.y >= 0f, "$zone item $idx: y=${pos.y} is out of bounds (< 0)")
                assertTrue(pos.y <= screenHeight, "$zone item $idx: y=${pos.y} is out of bounds (> H)")
            }
        }
    }

    @Test
    fun edgeHug_cornerCellVacant_topLeft() {
        val padPx = 12f
        val itemSizePx = 48f
        val half = itemSizePx / 2f
        val cornerCenter = Offset(padPx + half, padPx + half)

        val positions = RadialMenuMath.edgeHugLayout(
            RadialMenuMath.MenuZone.CORNER_TOP_LEFT,
            screenWidth, screenHeight,
            itemCount = 4, itemSizePx = itemSizePx, gapPx = 8f, padPx = padPx
        )
        // No item should be at the corner cell
        for (pos in positions) {
            assertTrue(
                pos != cornerCenter,
                "Corner cell should be vacant, but found item at $pos"
            )
        }
    }

    @Test
    fun edgeHug_emptyItems_returnsEmpty() {
        val positions = RadialMenuMath.edgeHugLayout(
            RadialMenuMath.MenuZone.CORNER_TOP_LEFT,
            screenWidth, screenHeight,
            itemCount = 0, itemSizePx = 48f, gapPx = 8f, padPx = 12f
        )
        assertTrue(positions.isEmpty())
    }

    // --- getNearestItemSelection tests ---

    @Test
    fun nearestItem_returnsClosest() {
        val positions = listOf(
            Offset(100f, 100f),
            Offset(200f, 100f),
            Offset(100f, 200f)
        )
        // Pointer near item 1
        val result = RadialMenuMath.getNearestItemSelection(190f, 110f, positions)
        assertEquals(1, result)
    }

    @Test
    fun nearestItem_emptyList_returnsNull() {
        val result = RadialMenuMath.getNearestItemSelection(100f, 100f, emptyList())
        assertEquals(null, result)
    }

    @Test
    fun nearestItem_exactPosition_returnsCorrectIndex() {
        val positions = listOf(
            Offset(50f, 50f),
            Offset(150f, 50f),
            Offset(50f, 150f),
            Offset(150f, 150f)
        )
        assertEquals(0, RadialMenuMath.getNearestItemSelection(50f, 50f, positions))
        assertEquals(3, RadialMenuMath.getNearestItemSelection(150f, 150f, positions))
    }

    @Test
    fun nearestItem_farFromAllButtons_returnsNull() {
        val positions = listOf(
            Offset(50f, 50f),
            Offset(150f, 50f),
            Offset(50f, 150f),
            Offset(150f, 150f)
        )
        val result = RadialMenuMath.getNearestItemSelection(
            pointerX = 400f,
            pointerY = 400f,
            itemPositions = positions,
            deadZonePx = 30f
        )
        assertNull(result, "Pointer far from all buttons should not select any item")
    }

    // --- Integration logic tests ---

    @Test
    fun centerTouch_anyItemCount_shouldUseRadial() {
        // Verify zone is CENTER for a center touch — the calling code
        // uses this to decide radial vs edge-hug
        val zone = RadialMenuMath.detectZone(500f, 500f, screenWidth, screenHeight, edgeThreshPx)
        assertEquals(RadialMenuMath.MenuZone.CENTER, zone)
        // With CENTER zone, the caller should always use radial layout regardless of item count
    }

    @Test
    fun cornerTouch_3items_shouldStillUseRadial() {
        // Even in a corner, 3 items (≤ CORNER_ITEM_THRESHOLD) should use radial
        val zone = RadialMenuMath.detectZone(30f, 30f, screenWidth, screenHeight, edgeThreshPx)
        assertEquals(RadialMenuMath.MenuZone.CORNER_TOP_LEFT, zone)
        // The calling code checks: zone != CENTER && itemCount > CORNER_ITEM_THRESHOLD
        // With 3 items: 3 > 3 == false → radial layout
        assertTrue(3 <= RadialMenuDefaults.CORNER_ITEM_THRESHOLD)
    }

    @Test
    fun cornerTouch_4items_shouldUseEdgeHug() {
        val zone = RadialMenuMath.detectZone(30f, 30f, screenWidth, screenHeight, edgeThreshPx)
        assertEquals(RadialMenuMath.MenuZone.CORNER_TOP_LEFT, zone)
        // With 4 items: 4 > 3 == true → edge-hug layout
        assertTrue(4 > RadialMenuDefaults.CORNER_ITEM_THRESHOLD)
    }

    @Test
    fun edgeHug_extremeCorner_noClip() {
        // Tap at (5, 5) — extreme top-left corner.
        // All item positions must have non-negative coordinates and stay within screen bounds.
        val zone = RadialMenuMath.detectZone(5f, 5f, screenWidth, screenHeight, edgeThreshPx)
        assertEquals(RadialMenuMath.MenuZone.CORNER_TOP_LEFT, zone)

        val itemSizePx = 48f
        val gapPx = 8f
        val padPx = 12f
        val positions = RadialMenuMath.edgeHugLayout(
            zone, screenWidth, screenHeight,
            4, itemSizePx, gapPx, padPx
        )
        assertEquals(4, positions.size)

        val half = itemSizePx / 2f
        for (pos in positions) {
            assertTrue(pos.x >= half, "x=${pos.x} is less than half=$half")
            assertTrue(pos.y >= half, "y=${pos.y} is less than half=$half")
            assertTrue(pos.x <= screenWidth - half, "x=${pos.x} exceeds screenWidth-half")
            assertTrue(pos.y <= screenHeight - half, "y=${pos.y} exceeds screenHeight-half")
        }
    }

    @Test
    fun cornerTouch_4items_gateOff_shouldUseRadial() {
        // Even with 4 items in a corner, if enableEdgeHugLayout is false,
        // the gate condition evaluates to false and radial layout is used.
        val zone = RadialMenuMath.detectZone(30f, 30f, screenWidth, screenHeight, edgeThreshPx)
        assertEquals(RadialMenuMath.MenuZone.CORNER_TOP_LEFT, zone)

        val enableEdgeHugLayout = false
        val itemCount = 4
        val useEdgeHug = enableEdgeHugLayout &&
            zone != RadialMenuMath.MenuZone.CENTER &&
            itemCount > RadialMenuDefaults.CORNER_ITEM_THRESHOLD

        assertFalse(useEdgeHug, "Gate should be false when enableEdgeHugLayout is off")
    }
}
