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
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import io.github.gawwr4v.radialmenu.RadialMenuItem
import io.github.gawwr4v.radialmenu.RadialMenuOverlay
import io.github.gawwr4v.radialmenu.RadialMenuTriggerMode
import io.github.gawwr4v.radialmenu.RadialMenuWrapper
import kotlin.math.roundToInt

private enum class DesktopTriggerOption {
    SecondaryClick,
    KeyboardHold,
    LongPress
}

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
        var statusText by remember { mutableStateOf("No selection yet") }
        var itemCount by remember { mutableIntStateOf(4) }
        var edgeHugEnabled by remember { mutableStateOf(false) }
        var triggerOption by remember { mutableStateOf(DesktopTriggerOption.SecondaryClick) }
        var secondaryPositionAware by remember { mutableStateOf(false) }
        var longPressPositionAware by remember { mutableStateOf(true) }

        val items = remember(itemCount) { createItems(itemCount) }
        val triggerMode = when (triggerOption) {
            DesktopTriggerOption.SecondaryClick -> RadialMenuTriggerMode.SecondaryClick(
                positionAware = secondaryPositionAware
            )
            DesktopTriggerOption.KeyboardHold -> RadialMenuTriggerMode.KeyboardHold(Key.Q)
            DesktopTriggerOption.LongPress -> RadialMenuTriggerMode.LongPress(
                positionAware = longPressPositionAware
            )
        }
        val isCenterSpawned = triggerMode is RadialMenuTriggerMode.KeyboardHold
        val triggerHint = when (triggerOption) {
            DesktopTriggerOption.SecondaryClick -> "Right-click anywhere to open"
            DesktopTriggerOption.KeyboardHold -> "Hold Q to open menu"
            DesktopTriggerOption.LongPress -> "Long press anywhere to open menu"
        }

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
                        enableEdgeHugLayout = edgeHugEnabled && !isCenterSpawned,
                        triggerMode = triggerMode,
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
                                text = "$triggerHint\n$statusText",
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
                                checked = edgeHugEnabled && !isCenterSpawned,
                                onCheckedChange = if (isCenterSpawned) null else { value -> edgeHugEnabled = value },
                                enabled = !isCenterSpawned
                            )
                            Text(
                                text = "Edge-Hug",
                                fontSize = 13.sp,
                                color = Color.White,
                                modifier = Modifier.alpha(if (isCenterSpawned) 0.5f else 1f)
                            )
                        }

                        Text(
                            text = "Trigger mode",
                            fontSize = 12.sp,
                            color = Color.White.copy(alpha = 0.9f),
                            modifier = Modifier.padding(top = 8.dp)
                        )

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(top = 4.dp)
                        ) {
                            RadioButton(
                                selected = triggerOption == DesktopTriggerOption.SecondaryClick,
                                onClick = { triggerOption = DesktopTriggerOption.SecondaryClick }
                            )
                            Text("SecondaryClick", color = Color.White, fontSize = 12.sp)
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(
                                selected = triggerOption == DesktopTriggerOption.KeyboardHold,
                                onClick = { triggerOption = DesktopTriggerOption.KeyboardHold }
                            )
                            Text("KeyboardHold (Q)", color = Color.White, fontSize = 12.sp)
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(
                                selected = triggerOption == DesktopTriggerOption.LongPress,
                                onClick = { triggerOption = DesktopTriggerOption.LongPress }
                            )
                            Text("LongPress", color = Color.White, fontSize = 12.sp)
                        }

                        if (triggerOption != DesktopTriggerOption.KeyboardHold) {
                            val positionAwareChecked = when (triggerOption) {
                                DesktopTriggerOption.SecondaryClick -> secondaryPositionAware
                                DesktopTriggerOption.LongPress -> longPressPositionAware
                                DesktopTriggerOption.KeyboardHold -> false
                            }
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(top = 4.dp)
                            ) {
                                Checkbox(
                                    checked = positionAwareChecked,
                                    onCheckedChange = { value ->
                                        when (triggerOption) {
                                            DesktopTriggerOption.SecondaryClick -> secondaryPositionAware = value
                                            DesktopTriggerOption.LongPress -> longPressPositionAware = value
                                            DesktopTriggerOption.KeyboardHold -> Unit
                                        }
                                    }
                                )
                                Text("Position aware", color = Color.White, fontSize = 12.sp)
                            }
                        }

                        Text(
                            text = triggerHint,
                            fontSize = 11.sp,
                            color = Color.White.copy(alpha = 0.85f),
                            modifier = Modifier.padding(top = 6.dp)
                        )
                        if (isCenterSpawned) {
                            Text(
                                text = "Not applicable for center-spawned menus",
                                fontSize = 11.sp,
                                color = Color.White.copy(alpha = 0.6f),
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
