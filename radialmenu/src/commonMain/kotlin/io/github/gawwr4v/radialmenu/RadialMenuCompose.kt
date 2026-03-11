package io.github.gawwr4v.radialmenu

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.math.abs

import io.github.gawwr4v.radialmenu.RadialMenuDefaults.CORNER_ITEM_THRESHOLD
import io.github.gawwr4v.radialmenu.RadialMenuDefaults.DEAD_ZONE_PX
import io.github.gawwr4v.radialmenu.RadialMenuDefaults.DOUBLE_TAP_TIMEOUT_MS
import io.github.gawwr4v.radialmenu.RadialMenuDefaults.EDGE_HUG_GAP_DP
import io.github.gawwr4v.radialmenu.RadialMenuDefaults.EDGE_HUG_PAD_DP
import io.github.gawwr4v.radialmenu.RadialMenuDefaults.EDGE_THRESH_DP
import io.github.gawwr4v.radialmenu.RadialMenuDefaults.ICON_SIZE_DP
import io.github.gawwr4v.radialmenu.RadialMenuDefaults.ICON_SPREAD_DEGREES
import io.github.gawwr4v.radialmenu.RadialMenuDefaults.LONG_PRESS_TIMEOUT_MS
import io.github.gawwr4v.radialmenu.RadialMenuDefaults.MENU_RADIUS_DP
import io.github.gawwr4v.radialmenu.RadialMenuDefaults.SELECTION_DEAD_ZONE_DEG
import io.github.gawwr4v.radialmenu.RadialMenuMath.MenuZone
import kotlinx.coroutines.withTimeoutOrNull

private data class RadialMenuState(
    val isVisible: Boolean = false,
    val touchPosition: Offset = Offset.Zero,
    val dragOffset: Offset = Offset.Zero,
    val currentSelectionIndex: Int? = null,
    val centerAngle: Float = 270f,
    val zone: MenuZone = MenuZone.CENTER,
    val edgeHugPositions: List<Offset>? = null
)

private val globalMenuState = mutableStateOf(RadialMenuState())

/**
 * A wrapper composable that detects long presses and shows the radial menu overlay.
 *
 * @param items The items to display. Supports 2-8 items (more is allowed but may degrade UX).
 * @param onItemSelected Callback fired when an item is selected from the radial menu.
 * @param enableEdgeHugLayout When true, items arrange in an L-shape along screen edges
 *   when long-pressed in a corner with 4+ items. Defaults to false (pure radial layout).
 * @param onTap Callback fired when the wrapped content is tapped.
 * @param onDoubleTap Callback fired when the wrapped content is double-tapped.
 * @param modifier Optional modifier for the wrapper box.
 * @param content The content to be wrapped and monitored for gestures.
 * @since 1.0.0
 */
@Composable
fun RadialMenuWrapper(
    items: List<RadialMenuItem>,
    onItemSelected: (RadialMenuItem) -> Unit,
    enableEdgeHugLayout: Boolean = false,
    onTap: () -> Unit = {},
    onDoubleTap: () -> Unit = {},
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    if (items.isEmpty()) return

    val density = LocalDensity.current
    var lastTapTime by remember { mutableStateOf(0L) }
    var containerPosition by remember { mutableStateOf(Offset.Zero) }
    var containerSize by remember { mutableStateOf(IntSize.Zero) }
    // Window dimensions for edge-hug layout and zone detection.
    // These must match the Popup's rendering surface (fullscreen window),
    // NOT the container size (which may be smaller due to controls/toolbars).
    var windowWidth by remember { mutableStateOf(0f) }
    var windowHeight by remember { mutableStateOf(0f) }

    val touchSlop = with(density) { 20.dp.toPx() }
    val hapticFeedback = rememberHapticFeedback()
    val layoutDirection = LocalLayoutDirection.current

    fun calculateCenterAngle(x: Float, y: Float, screenWidth: Float, screenHeight: Float, isRtl: Boolean): Float =
        RadialMenuMath.calculateCenterAngle(x, y, screenWidth, screenHeight, isRtl)

    fun getSelectionFromDrag(dragX: Float, dragY: Float, centerAngle: Float, itemCount: Int): Int? =
        RadialMenuMath.getSelectionFromDrag(dragX, dragY, centerAngle, itemCount)

    Box(
        modifier = modifier
            .onGloballyPositioned { coords ->
                // Use positionInRoot() because the Popup's coordinate origin matches
                // the Compose root (content area). On Android, positionInWindow()
                // includes the status bar offset, but the Popup starts at the content
                // area — causing the menu to appear shifted down by ~statusBarHeight.
                // On Desktop, root = window, so positionInRoot() works on both platforms.
                containerPosition = coords.positionInRoot()
                containerSize = coords.size
                // Compute the root/Popup extent: the container's bottom-right in root
                // space gives the usable drawing area for edge-hug positioning.
                windowWidth = containerPosition.x + coords.size.width.toFloat()
                windowHeight = containerPosition.y + coords.size.height.toFloat()
            }
    ) {
        Box(
            modifier = Modifier
                // Key on both items AND enableEdgeHugLayout so the gesture handler
                // restarts when either changes. Without enableEdgeHugLayout in the key,
                // the lambda captures a stale value and the toggle has no effect.
                .pointerInput(items, enableEdgeHugLayout) {
                    awaitEachGesture {
                        val down = awaitFirstDown(requireUnconsumed = false)
                        val startPosition = down.position
                        val pointerId = down.id
                        val downTime = kotlin.time.TimeSource.Monotonic.markNow()

                        // absolutePosition is in root coordinate space (matches Popup)
                        val absolutePosition = containerPosition + startPosition

                        // Container dimensions for radial layout (draws relative to touch)
                        val containerW = containerSize.width.toFloat().coerceAtLeast(1f)
                        val containerH = containerSize.height.toFloat().coerceAtLeast(1f)
                        // Window dimensions for edge-hug (draws at absolute positions in Popup)
                        val winW = windowWidth.coerceAtLeast(1f)
                        val winH = windowHeight.coerceAtLeast(1f)
                        val isRtl = layoutDirection == LayoutDirection.Rtl

                        var isLongPress = false
                        var moved = false
                        var released = false

                        while (!released && !isLongPress) {
                            if (downTime.elapsedNow().inWholeMilliseconds >= LONG_PRESS_TIMEOUT_MS && !moved) {
                                isLongPress = true
                                break
                            }

                            try {
                                val event = withTimeoutOrNull(50) {
                                    awaitPointerEvent(PointerEventPass.Main)
                                }

                                if (event != null) {
                                    val change = event.changes.find { it.id == pointerId }

                                    if (change == null || !change.pressed) {
                                        released = true
                                        break
                                    }

                                    val dist = sqrt(
                                        (change.position.x - startPosition.x).let { it * it } +
                                        (change.position.y - startPosition.y).let { it * it }
                                    )
                                    if (dist > touchSlop) {
                                        moved = true
                                    }
                                }
                            } catch (_: Exception) {
                            }
                        }

                        if (isLongPress) {
                            // User has held down the finger long enough to trigger the radial menu.
                            // Trigger a distinct vibration to notify them the menu is open.
                            hapticFeedback.vibrate(50)

                            val edgeThreshPx = with(density) { EDGE_THRESH_DP.dp.toPx() }
                            // detectZone uses window coords: touch is in window space,
                            // and W/H must be window dimensions so corners are detected
                            // at true screen boundaries, not container boundaries.
                            val zone = RadialMenuMath.detectZone(
                                absolutePosition.x, absolutePosition.y,
                                winW, winH, edgeThreshPx
                            )

                            // Decision tree: edge-hug requires explicit opt-in,
                            // corner zone, AND 4+ items. Otherwise radial (unchanged).
                            val useEdgeHug = enableEdgeHugLayout &&
                                zone != MenuZone.CENTER && items.size > CORNER_ITEM_THRESHOLD

                            val centerAngle: Float
                            val edgeHugPositions: List<Offset>?

                            if (useEdgeHug) {
                                centerAngle = 0f // unused in edge-hug mode
                                val itemSizePx = with(density) { ICON_SIZE_DP.dp.toPx() }
                                val gapPx = with(density) { EDGE_HUG_GAP_DP.dp.toPx() }
                                val padPx = with(density) { EDGE_HUG_PAD_DP.dp.toPx() }
                                // edgeHugLayout uses window dimensions because positions
                                // are rendered in the fullscreen Popup.
                                edgeHugPositions = RadialMenuMath.edgeHugLayout(
                                    zone, winW, winH,
                                    items.size, itemSizePx * 1.5f, gapPx, padPx
                                )
                            } else {
                                // Radial layout uses container dimensions since items
                                // are positioned relative to the touch center.
                                centerAngle = calculateCenterAngle(
                                    absolutePosition.x, absolutePosition.y,
                                    containerW, containerH, isRtl
                                )
                                edgeHugPositions = null
                            }

                            globalMenuState.value = RadialMenuState(
                                isVisible = true,
                                touchPosition = absolutePosition,
                                dragOffset = Offset.Zero,
                                currentSelectionIndex = null,
                                centerAngle = centerAngle,
                                zone = zone,
                                edgeHugPositions = edgeHugPositions
                            )

                            var lastPosition = startPosition
                            var currentDrag = Offset.Zero
                            var currentSelectionIndex: Int? = null

                            // We now enter a localized event loop to track the drag gesture.
                            while (true) {
                                // Wait for the next active pointer event in the main pass.
                                val event = awaitPointerEvent(PointerEventPass.Main)
                                val change = event.changes.find { it.id == pointerId }

                                if (change == null) break

                                val currentPos = change.position
                                val delta = currentPos - lastPosition
                                lastPosition = currentPos
                                currentDrag += delta

                                change.consume()

                                // If the user lifts their finger, we commit the selection.
                                if (!change.pressed) {
                                    if (currentSelectionIndex != null) {
                                        // Provide shorter haptic feedback to confirm selection completion.
                                        hapticFeedback.vibrate(30)
                                        onItemSelected(items[currentSelectionIndex])
                                    }
                                    break
                                }

                                // Calculate how far the user has dragged from the original press center.
                                val dragDist = sqrt(currentDrag.x * currentDrag.x + currentDrag.y * currentDrag.y)

                                // We only start selecting items if they drag past a minimum threshold
                                // to avoid accidental selections when simply holding the menu open.
                                if (dragDist > 30f) {
                                    val newIndex = if (edgeHugPositions != null) {
                                        // Edge-hug mode: select by nearest distance to absolute pointer
                                        val absolutePointer = containerPosition + currentPos
                                        RadialMenuMath.getNearestItemSelection(
                                            absolutePointer.x, absolutePointer.y, edgeHugPositions
                                        )
                                    } else {
                                        // Radial mode: existing angle-based selection (unchanged)
                                        getSelectionFromDrag(currentDrag.x, currentDrag.y, centerAngle, items.size)
                                    }

                                    if (newIndex != currentSelectionIndex) {
                                        if (newIndex != null) {
                                            // Vibrate subtly as the user drags over different items.
                                            hapticFeedback.vibrate(20)
                                        }
                                        currentSelectionIndex = newIndex
                                    }

                                    globalMenuState.value = globalMenuState.value.copy(
                                        dragOffset = currentDrag,
                                        currentSelectionIndex = currentSelectionIndex
                                    )
                                }
                            }

                            globalMenuState.value = RadialMenuState()

                        } else if (!moved && released) {
                            val timeSinceLastTap = System.currentTimeMillis() - lastTapTime

                            if (timeSinceLastTap < DOUBLE_TAP_TIMEOUT_MS && timeSinceLastTap > 0) {
                                onDoubleTap()
                                lastTapTime = 0L
                            } else {
                                lastTapTime = System.currentTimeMillis()
                                onTap()
                            }
                        }
                    }
                }
        ) {
            content()
        }
    }
}

/**
 * The fullscreen overlay that displays the radial menu when active.
 * Uses a Popup to render above all other UI elements (toolbars, FABs, etc.).
 *
 * Place at the root of the app's composition tree (e.g. above navigation).
 *
 * @param items The same items list passed to [RadialMenuWrapper].
 * @param colors The color configuration.
 * @param animationConfig The animation configuration.
 * @since 1.0.0
 */
@Composable
fun RadialMenuOverlay(
    items: List<RadialMenuItem>,
    colors: RadialMenuColors = RadialMenuColors.default(),
    animationConfig: RadialMenuAnimationConfig = RadialMenuAnimationConfig.default()
) {
    val menuState by globalMenuState

    if (menuState.isVisible) {
        Popup(
            properties = PopupProperties(focusable = false)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(colors.overlayColor)
            ) {
                RadialMenuCanvas(
                    center = menuState.touchPosition,
                    dragOffset = menuState.dragOffset,
                    selectionIndex = menuState.currentSelectionIndex,
                    items = items,
                    colors = colors,
                    centerAngle = menuState.centerAngle,
                    animationConfig = animationConfig,
                    edgeHugPositions = menuState.edgeHugPositions
                )
            }
        }
    }
}

/**
 * The internal drawing logic for the radial menu icons, indicators, and badges.
 *
 * @param center The touch origin position.
 * @param dragOffset Current drag delta from [center].
 * @param selectionIndex Index of the currently hovered item, or null.
 * @param items The menu items to draw.
 * @param colors Color configuration.
 * @param centerAngle The computed center angle of the menu fan.
 * @param animationConfig Animation configuration.
 * @param edgeHugPositions Pre-computed item positions for edge-hug mode, or null for radial.
 * @since 1.0.0
 */
@Composable
fun RadialMenuCanvas(
    center: Offset,
    dragOffset: Offset,
    selectionIndex: Int?,
    items: List<RadialMenuItem>,
    colors: RadialMenuColors,
    centerAngle: Float,
    animationConfig: RadialMenuAnimationConfig,
    edgeHugPositions: List<Offset>? = null
) {
    val density = LocalDensity.current
    val menuRadius = with(density) { MENU_RADIUS_DP.dp.toPx() }
    val iconSize = with(density) { ICON_SIZE_DP.dp.toPx() }
    val textMeasurer = rememberTextMeasurer()

    // Animate scale for each item
    val scales = items.indices.map { index ->
        val targetScale = if (selectionIndex == index) animationConfig.selectedItemScale else 1.0f
        if (animationConfig.enableSpringAnimation) {
            animateFloatAsState(
                targetValue = targetScale,
                animationSpec = spring(
                    dampingRatio = animationConfig.springDampingRatio,
                    stiffness = animationConfig.springStiffness
                ),
                label = "itemScale$index"
            )
        } else {
            animateFloatAsState(
                targetValue = targetScale,
                animationSpec = tween(animationConfig.itemScaleDurationMs),
                label = "itemScale$index"
            )
        }
    }

    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .semantics {
                contentDescription = "Radial menu"
            }
    ) {
        // Center indicator dot (only in radial mode)
        if (edgeHugPositions == null) {
            drawCircle(
                color = colors.centerIndicatorColor,
                radius = 16f,
                center = center
            )
        }

        // Draw each item dynamically
        val itemCount = items.size
        for (i in 0 until itemCount) {
            val item = items[i]

            // Determine icon center: edge-hug positions or radial angle calculation
            val iconCenter = if (edgeHugPositions != null && i < edgeHugPositions.size) {
                edgeHugPositions[i]
            } else {
                val itemAngle = centerAngle + ((i - (itemCount - 1) / 2f) * ICON_SPREAD_DEGREES)
                val angleRad = kotlin.math.PI / 180 * itemAngle
                Offset(
                    center.x + (menuRadius * cos(angleRad)).toFloat(),
                    center.y + (menuRadius * sin(angleRad)).toFloat()
                )
            }

            val isSelected = selectionIndex == i
            val scale = scales[i].value
            val scaledSize = iconSize * scale
            val bgRadius = scaledSize * 0.75f

            // Background circle
            drawCircle(
                color = if (isSelected) colors.itemBackgroundSelected else colors.itemBackground,
                radius = bgRadius,
                center = iconCenter
            )

            // Icon
            val painter = if (item.isActive && item.iconActive != null) item.iconActive else item.icon
            translate(
                left = iconCenter.x - scaledSize / 2,
                top = iconCenter.y - scaledSize / 2
            ) {
                with(painter) {
                    draw(
                        size = Size(scaledSize, scaledSize),
                        alpha = 1f,
                        colorFilter = ColorFilter.tint(
                            if (isSelected) colors.iconTintSelected else colors.iconTint
                        )
                    )
                }
            }

            // Badge
            val badgeText = item.badgeText ?: if (item.badgeCount > 0) {
                if (item.badgeCount > 99) "99+" else item.badgeCount.toString()
            } else null

            if (badgeText != null) {
                val badgeRadius = iconSize * 0.28f
                val badgeCenterX = iconCenter.x + bgRadius * 0.6f
                val badgeCenterY = iconCenter.y - bgRadius * 0.6f

                drawCircle(
                    color = colors.badgeColor,
                    radius = badgeRadius,
                    center = Offset(badgeCenterX, badgeCenterY)
                )

                val textSize = (iconSize * 0.35f).sp
                val textResult = textMeasurer.measure(
                    text = badgeText,
                    style = TextStyle(
                        color = colors.badgeTextColor,
                        fontSize = textSize,
                        textAlign = TextAlign.Center
                    )
                )
                drawText(
                    textLayoutResult = textResult,
                    topLeft = Offset(
                        badgeCenterX - textResult.size.width / 2f,
                        badgeCenterY - textResult.size.height / 2f
                    )
                )
            }
        }

        // Drag direction indicator (only in radial mode)
        if (edgeHugPositions == null) {
            val dragDist = sqrt(dragOffset.x * dragOffset.x + dragOffset.y * dragOffset.y)
            if (dragDist > 20f) {
                val indicatorLen = minOf(dragDist * 0.6f, menuRadius * 0.5f)
                val normalized = Offset(dragOffset.x / dragDist, dragOffset.y / dragDist)
                drawLine(
                    color = colors.centerIndicatorColor,
                    start = center,
                    end = Offset(
                        center.x + normalized.x * indicatorLen,
                        center.y + normalized.y * indicatorLen
                    ),
                    strokeWidth = 4f
                )
            }
        }
    }
}
