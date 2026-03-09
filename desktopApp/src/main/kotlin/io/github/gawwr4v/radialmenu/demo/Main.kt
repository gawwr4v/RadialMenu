package io.github.gawwr4v.radialmenu.demo

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import io.github.gawwr4v.radialmenu.RadialMenuItem
import io.github.gawwr4v.radialmenu.RadialMenuOverlay
import io.github.gawwr4v.radialmenu.RadialMenuWrapper

/** A simple solid-color circle painter used as a placeholder icon. */
private class CirclePainter(private val color: Color) : Painter() {
    override val intrinsicSize = Size(48f, 48f)
    override fun DrawScope.onDraw() {
        drawCircle(color = color, radius = size.minDimension / 2f)
    }
}

/**
 * Entry point for the Desktop JVM demo of the RadialMenu library.
 */
fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "RadialMenu Desktop Demo",
        state = rememberWindowState(width = 480.dp, height = 640.dp)
    ) {
        var statusText by remember { mutableStateOf("Long-press anywhere to open the menu") }

        val items = listOf(
            RadialMenuItem(
                id = 1,
                icon = CirclePainter(Color.Blue),
                label = "Action 1"
            ),
            RadialMenuItem(
                id = 2,
                icon = CirclePainter(Color.Red),
                label = "Action 2"
            ),
            RadialMenuItem(
                id = 3,
                icon = CirclePainter(Color.Green),
                label = "Action 3"
            ),
            RadialMenuItem(
                id = 4,
                icon = CirclePainter(Color.Yellow),
                label = "Action 4"
            )
        )

        MaterialTheme(colorScheme = darkColorScheme()) {
            Surface(modifier = Modifier.fillMaxSize()) {
                Box {
                    RadialMenuWrapper(
                        items = items,
                        onItemSelected = { item ->
                            statusText = "Selected: ${item.label}"
                        },
                        onTap = {
                            statusText = "Tapped"
                        },
                        onDoubleTap = {
                            statusText = "Double-tapped"
                        },
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = statusText,
                                fontSize = 18.sp,
                                color = Color.White
                            )
                        }
                    }

                    RadialMenuOverlay(items = items)
                }
            }
        }
    }
}
