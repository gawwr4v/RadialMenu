package io.github.gawwr4v.radialmenu

import android.graphics.Canvas
import android.graphics.ColorFilter
import android.graphics.PorterDuffColorFilter
import android.graphics.Rect
import android.graphics.drawable.Drawable
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.asComposeColorFilter
import androidx.compose.ui.graphics.drawscope.CanvasDrawScope
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import java.util.WeakHashMap

// Provides mock implementations for Drawable methods on Compose Painter 
// to ensure the untouched Android View implementation compiles and runs with KMP.

internal class MockDrawablePainter(
    val drawable: Drawable
) : Painter() {
    override val intrinsicSize: Size
        get() = Size(drawable.intrinsicWidth.toFloat(), drawable.intrinsicHeight.toFloat())

    override fun DrawScope.onDraw() {
        // Intentionally empty. Drawn via our custom Painter.draw(Canvas) extension below.
    }
}

/**
 * Extension to convert an Android [Drawable] into a Jetpack Compose [Painter].
 * @return A [Painter] that wraps the given [Drawable].
 */
fun Drawable.toPainter(): Painter = MockDrawablePainter(this)

private val boundsMap = WeakHashMap<Painter, Rect>()
private val colorFilterMap = WeakHashMap<Painter, ColorFilter>()

/** Mocks Drawable.setBounds for a Compose Painter. */
fun Painter.setBounds(left: Int, top: Int, right: Int, bottom: Int) {
    boundsMap[this] = Rect(left, top, right, bottom)
}

/** Mocks Drawable.colorFilter for a Compose Painter. */
var Painter.colorFilter: ColorFilter?
    get() = colorFilterMap[this]
    set(value) {
        if (value != null) {
            colorFilterMap[this] = value
        } else {
            colorFilterMap.remove(this)
        }
    }

/** Mocks Drawable.draw for a Compose Painter onto a native Android Canvas. */
fun Painter.draw(canvas: Canvas) {
    val painter = this
    val bounds = boundsMap[painter] ?: return
    
    if (painter is MockDrawablePainter) {
        painter.drawable.bounds = bounds
        painter.drawable.colorFilter = colorFilterMap[painter]
        painter.drawable.draw(canvas)
        return
    }
    
    val composeCanvas = androidx.compose.ui.graphics.Canvas(canvas)
    val drawScope = CanvasDrawScope()
    val size = Size(bounds.width().toFloat(), bounds.height().toFloat())
    
    drawScope.draw(
        density = Density(1f),
        layoutDirection = LayoutDirection.Ltr,
        canvas = composeCanvas,
        size = size
    ) {
        val cf = colorFilterMap[painter]
        val composeColorFilter = cf?.asComposeColorFilter()
        
        translate(bounds.left.toFloat(), bounds.top.toFloat()) {
            with(painter) {
                draw(
                    size = size,
                    alpha = 1f,
                    colorFilter = composeColorFilter
                )
            }
        }
    }
}
