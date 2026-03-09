package io.github.gawwr4v.radialmenu

import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.painter.Painter

/** A no-op Painter for unit tests that don't need actual rendering. */
internal class MockPainter : Painter() {
    override val intrinsicSize = Size(48f, 48f)
    override fun DrawScope.onDraw() {}
}
