package io.github.gawwr4v.radialmenu

import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.painter.Painter
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull



class RadialMenuItemTest {

    private val mockPainter = MockPainter()

    @Test
    fun defaultValues_areCorrect() {
        val item = RadialMenuItem(id = 1, icon = mockPainter, label = "Test")
        assertEquals(0, item.badgeCount)
        assertNull(item.badgeText)
        assertFalse(item.isActive)
        assertNull(item.iconActive)
        assertEquals("Test", item.contentDescription)
    }

    @Test
    fun badgeText_overridesCountDisplayLogic() {
        val item = RadialMenuItem(id = 1, icon = mockPainter, badgeCount = 5, badgeText = "NEW")
        assertEquals("NEW", item.badgeText)
        assertEquals(5, item.badgeCount)
    }

    @Test
    fun contentDescription_defaultsToLabel() {
        val item = RadialMenuItem(id = 1, icon = mockPainter, label = "Share")
        assertEquals("Share", item.contentDescription)
    }

    @Test
    fun contentDescription_canBeOverriddenIndependently() {
        val item = RadialMenuItem(id = 1, icon = mockPainter, label = "♥", contentDescription = "Like")
        assertEquals("Like", item.contentDescription)
    }
}
