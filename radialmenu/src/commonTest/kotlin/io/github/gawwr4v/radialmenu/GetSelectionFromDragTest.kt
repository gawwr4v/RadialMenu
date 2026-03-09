package io.github.gawwr4v.radialmenu

import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.painter.Painter
import kotlin.math.cos
import kotlin.math.sin
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue



class GetSelectionFromDragTest {

    private val mockPainter = MockPainter()
    private val items = listOf(
        RadialMenuItem(id = 1, icon = mockPainter, label = "Item 1"),
        RadialMenuItem(id = 2, icon = mockPainter, label = "Item 2"),
        RadialMenuItem(id = 3, icon = mockPainter, label = "Item 3")
    )

    @Test
    fun insideDeadZone_returnsNull() {
        val result = RadialMenuMath.getSelectionFromDrag(10f, 10f, 270f, items.size)
        assertNull(result, "Inside dead zone should return null")
    }

    @Test
    fun straightUp_selectsMiddleItem() {
        val result = RadialMenuMath.getSelectionFromDrag(0f, -100f, 270f, items.size)
        assertEquals(1, result, "Straight up should select center item")
    }

    @Test
    fun upLeft_selectsLeftItem() {
        val angle = Math.toRadians((270f - 45f).toDouble())
        val dx = (cos(angle) * 100f).toFloat()
        val dy = (sin(angle) * 100f).toFloat()
        val result = RadialMenuMath.getSelectionFromDrag(dx, dy, 270f, items.size)
        assertEquals(0, result, "Up-left drag should select left item")
    }

    @Test
    fun upRight_selectsRightItem() {
        val angle = Math.toRadians((270f + 45f).toDouble())
        val dx = (cos(angle) * 100f).toFloat()
        val dy = (sin(angle) * 100f).toFloat()
        val result = RadialMenuMath.getSelectionFromDrag(dx, dy, 270f, items.size)
        assertEquals(2, result, "Up-right drag should select right item")
    }

    @Test
    fun straightDown_returnsNull() {
        val result = RadialMenuMath.getSelectionFromDrag(0f, 100f, 270f, items.size)
        assertNull(result, "Downward drag should return null")
    }

    @Test
    fun worksWithTwoItems() {
        val result = RadialMenuMath.getSelectionFromDrag(0f, -100f, 270f, 2)
        assertNotNull(result, "Should select something with 2 items")
        assertTrue(result in 0..1, "Index must be valid for 2 items")
    }

    @Test
    fun worksWithEightItems() {
        val result = RadialMenuMath.getSelectionFromDrag(0f, -100f, 270f, 8)
        assertNotNull(result, "Should select something with 8 items")
        assertTrue(result in 0..7, "Index must be valid for 8 items")
    }

    @Test
    fun emptyItemsList_returnsNull() {
        val result = RadialMenuMath.getSelectionFromDrag(0f, -100f, 270f, 0)
        assertNull(result, "Empty items should return null")
    }
}
