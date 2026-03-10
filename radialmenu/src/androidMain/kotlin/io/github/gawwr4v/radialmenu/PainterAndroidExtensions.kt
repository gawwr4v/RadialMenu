package io.github.gawwr4v.radialmenu

import android.graphics.Canvas
import android.graphics.ColorFilter
import android.graphics.Rect
import android.graphics.drawable.Drawable
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.asComposeColorFilter
import androidx.compose.ui.graphics.drawscope.CanvasDrawScope
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection

// Provides mock implementations for Drawable methods on Compose Painter 
// to ensure the untouched Android View implementation compiles and runs with KMP.

internal class MockDrawablePainter(
    val drawable: Drawable
) : Painter() {
    override val intrinsicSize: Size
        get() = Size(drawable.intrinsicWidth.toFloat(), drawable.intrinsicHeight.toFloat())

    override fun DrawScope.onDraw() {
        // Intentionally empty. Drawn via our custom Painter.drawWithCache() extension below.
    }
}

/**
 * Extension to convert an Android [Drawable] into a Jetpack Compose [Painter].
 * @return A [Painter] that wraps the given [Drawable].
 */
fun Drawable.toPainter(): Painter = MockDrawablePainter(this)

// Removed global WeakHashMaps (Bug 1 fix) - visual configuration is now scoped per draw call

/** 
 * Scoped draw method for Compose Painters onto a native Android Canvas.
 * Passed arguments avoid per-frame allocations.
 */
// Cached parameters at instance level — avoids allocation inside onDraw() at 60fps
fun Painter.drawWithCache(
    canvas: Canvas,
    bounds: Rect,
    colorFilter: ColorFilter?,
    drawScope: CanvasDrawScope,
    composeCanvas: androidx.compose.ui.graphics.Canvas,
    baseSize: Size,
    scale: Float
) {
    if (this is MockDrawablePainter) {
        this.drawable.bounds = bounds
        this.drawable.colorFilter = colorFilter
        this.drawable.draw(canvas)
        return
    }
    
    // DrawScope is correctly reused/reset per Compose API contract
    drawScope.draw(
        density = Density(1f),
        layoutDirection = LayoutDirection.Ltr,
        canvas = composeCanvas,
        size = baseSize
    ) {
        val composeColorFilter = colorFilter?.asComposeColorFilter()
        
        val cx = bounds.exactCenterX()
        val cy = bounds.exactCenterY()
        
        translate(cx - baseSize.width / 2f, cy - baseSize.height / 2f) {
            scale(scale) {
                with(this@drawWithCache) {
                    draw(
                        size = baseSize,
                        alpha = 1f,
                        colorFilter = composeColorFilter
                    )
                }
            }
        }
    }
}
