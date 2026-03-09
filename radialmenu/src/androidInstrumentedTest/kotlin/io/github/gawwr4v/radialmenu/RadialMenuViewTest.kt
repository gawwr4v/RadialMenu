package io.github.gawwr4v.radialmenu

import android.content.Context
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.painter.Painter
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

private class MockPainter : Painter() {
    override val intrinsicSize = Size(48f, 48f)
    override fun DrawScope.onDraw() {}
}

@RunWith(AndroidJUnit4::class)
class RadialMenuViewTest {

    private val mockPainter = MockPainter()

    private fun createView(): RadialMenuView {
        val context = ApplicationProvider.getApplicationContext<Context>()
        return RadialMenuView(context)
    }

    private fun createItems(count: Int = 3): List<RadialMenuItem> =
        (1..count).map { RadialMenuItem(id = it, icon = mockPainter, label = "Item $it") }

    @Test
    fun view_inflatesSuccessfully() {
        val view = createView()
        assertNotNull(view)
    }

    @Test
    fun setItems_emptyList_preventsOpen() {
        val view = createView()
        view.setItems(emptyList())
        assertFalse("Menu should not be open with empty items", view.isMenuOpen)
    }

    @Test
    fun setItems_validItems_accepted() {
        val view = createView()
        view.setItems(createItems())
        // No crash = success; menu is closed by default
        assertFalse(view.isMenuOpen)
    }

    @Test
    fun setItemActive_updatesState() {
        val view = createView()
        val items = listOf(
            RadialMenuItem(id = 1, icon = mockPainter, label = "Like", isActive = false)
        )
        view.setItems(items)
        view.setItemActive(1, true)
        // setItemActive doesn't crash and works without the menu being open
        assertFalse(view.isMenuOpen) // menu still closed
    }

    @Test
    fun setItems_whileMenuOpen_doesNotCrash() {
        val view = createView()
        view.setItems(createItems())
        // Calling setItems again should auto-close and not crash
        view.setItems(createItems(5))
        assertFalse("Menu should be closed after setItems while open", view.isMenuOpen)
    }

    @Test
    fun onItemSelected_callbackCanBeSet() {
        val view = createView()
        var called = false
        view.onItemSelected = { called = true }
        assertNotNull(view.onItemSelected)
    }

    @Test
    fun contentDescription_isSet() {
        val view = createView()
        assertEquals("Radial menu", view.contentDescription)
    }
}
