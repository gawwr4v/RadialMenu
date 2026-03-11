package io.github.gawwr4v.radialmenu.demo

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import io.github.gawwr4v.radialmenu.RadialMenuItem
import io.github.gawwr4v.radialmenu.RadialMenuOverlay
import io.github.gawwr4v.radialmenu.RadialMenuWrapper
import kotlin.math.roundToInt

/** A simple solid-color circle painter used as a placeholder icon. */
private class CirclePainter(private val color: Color) : Painter() {
    override val intrinsicSize = Size(48f, 48f)
    override fun DrawScope.onDraw() {
        drawCircle(color = color, radius = size.minDimension / 2f)
    }
}

private val iconColors = listOf(
    Color.Blue, Color.Red, Color.Green, Color.Yellow,
    Color.Cyan, Color.Magenta, Color(0xFFFF8800), Color(0xFF8800FF)
)

private fun createItems(count: Int): List<RadialMenuItem> {
    return (0 until count.coerceIn(2, 8)).map { i ->
        RadialMenuItem(
            id = i + 1,
            icon = CirclePainter(iconColors[i]),
            label = "Action ${i + 1}"
        )
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
        var itemCount by remember { mutableIntStateOf(4) }
        var edgeHugEnabled by remember { mutableStateOf(false) }

        val items = remember(itemCount) { createItems(itemCount) }

        // Draggable panel offset (default: bottom-center, set after first layout)
        var panelOffset by remember { mutableStateOf(Offset.Zero) }
        var panelInitialized by remember { mutableStateOf(false) }
        var windowSize by remember { mutableStateOf(IntSize.Zero) }
        var panelSize by remember { mutableStateOf(IntSize.Zero) }

        MaterialTheme(colorScheme = darkColorScheme()) {
            Surface(modifier = Modifier.fillMaxSize()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .onGloballyPositioned { coords ->
                            windowSize = coords.size
                            if (!panelInitialized && panelSize.width > 0) {
                                // Default: bottom-center of the window
                                panelOffset = Offset(
                                    (windowSize.width - panelSize.width) / 2f,
                                    (windowSize.height - panelSize.height - 24f)
                                )
                                panelInitialized = true
                            }
                        }
                ) {
                    // Full-screen long-press target area
                    RadialMenuWrapper(
                        items = items,
                        onItemSelected = { item ->
                            statusText = "Selected: ${item.label}"
                        },
                        enableEdgeHugLayout = edgeHugEnabled,
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

                    // Floating draggable controls panel
                    Column(
                        modifier = Modifier
                            .offset { IntOffset(panelOffset.x.roundToInt(), panelOffset.y.roundToInt()) }
                            .onGloballyPositioned { coords ->
                                panelSize = coords.size
                                if (!panelInitialized && windowSize.width > 0) {
                                    panelOffset = Offset(
                                        (windowSize.width - coords.size.width) / 2f,
                                        (windowSize.height - coords.size.height - 24f)
                                    )
                                    panelInitialized = true
                                }
                            }
                            .pointerInput(Unit) {
                                detectDragGestures { _, dragAmount ->
                                    val newX = (panelOffset.x + dragAmount.x)
                                        .coerceIn(0f, (windowSize.width - panelSize.width).toFloat().coerceAtLeast(0f))
                                    val newY = (panelOffset.y + dragAmount.y)
                                        .coerceIn(0f, (windowSize.height - panelSize.height).toFloat().coerceAtLeast(0f))
                                    panelOffset = Offset(newX, newY)
                                }
                            }
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color.DarkGray.copy(alpha = 0.85f))
                            .padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Drag handle
                        Box(
                            modifier = Modifier
                                .size(width = 40.dp, height = 4.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(Color.White.copy(alpha = 0.5f))
                                .padding(bottom = 8.dp)
                        )

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(top = 8.dp)
                        ) {
                            Text(
                                text = "Items: $itemCount",
                                fontSize = 13.sp,
                                color = Color.White
                            )
                            Slider(
                                value = itemCount.toFloat(),
                                onValueChange = { itemCount = it.toInt() },
                                valueRange = 2f..8f,
                                steps = 5,
                                modifier = Modifier.width(120.dp).padding(horizontal = 8.dp)
                            )
                            Checkbox(
                                checked = edgeHugEnabled,
                                onCheckedChange = { edgeHugEnabled = it }
                            )
                            Text(
                                text = "Edge-Hug",
                                fontSize = 13.sp,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }
    }
}
