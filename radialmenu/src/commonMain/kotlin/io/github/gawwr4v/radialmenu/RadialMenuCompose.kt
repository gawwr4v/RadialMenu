package io.github.gawwr4v.radialmenu

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.type
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.isSecondaryPressed
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
private val globalMenuSpreadDegrees = mutableFloatStateOf(ICON_SPREAD_DEGREES)
private val globalKeyboardPointerMoveHandler = mutableStateOf<((Offset) -> Unit)?>(null)

private data class TapCallbacks(
    val onTap: () -> Unit,
    val onDoubleTap: () -> Unit
)

private data class TapStateCallbacks(
    val getLastTapTime: () -> Long,
    val setLastTapTime: (Long) -> Unit
)

/**
 * Resolves a configured trigger mode into the effective platform trigger.
 *
 * [RadialMenuTriggerMode.Auto] maps to [defaultTriggerMode].
 */
internal fun resolveTriggerMode(triggerMode: RadialMenuTriggerMode): RadialMenuTriggerMode {
    return if (triggerMode == RadialMenuTriggerMode.Auto) defaultTriggerMode else triggerMode
}

/**
 * Resolves whether position-aware center-angle logic should be applied.
 */
internal fun resolvePositionAware(triggerMode: RadialMenuTriggerMode): Boolean {
    return when (triggerMode) {
        is RadialMenuTriggerMode.LongPress -> triggerMode.positionAware
        is RadialMenuTriggerMode.SecondaryClick -> triggerMode.positionAware
        is RadialMenuTriggerMode.KeyboardHold -> false
        RadialMenuTriggerMode.Auto -> false
    }
}

internal fun resolveCenterAngle(
    isPositionAware: Boolean,
    position: Offset,
    containerWidth: Float,
    containerHeight: Float,
    isRtl: Boolean
): Float {
    return if (isPositionAware) {
        RadialMenuMath.calculateCenterAngle(
            position.x,
            position.y,
            containerWidth,
            containerHeight,
            isRtl
        )
    } else {
        0f
    }
}

internal expect val usePreviewKeyEventForKeyboardHold: Boolean

internal fun keyboardHoldSpreadDegrees(itemCount: Int): Float {
    return if (itemCount > 0) 360f / itemCount else 360f
}

internal fun centerSpawnedCenterAngle(itemCount: Int): Float {
    val spread = keyboardHoldSpreadDegrees(itemCount)
    return ((itemCount - 1) / 2f) * spread
}

internal fun keyboardHoldShouldOpenMenu(isKeyboardMenuOpen: Boolean): Boolean {
    return !isKeyboardMenuOpen
}

internal fun keyboardHoldCommittedSelection(
    isKeyboardMenuOpen: Boolean,
    hoveredItemIndex: Int?
): Int? {
    return if (isKeyboardMenuOpen) hoveredItemIndex else null
}

internal fun keyboardHoldHoverSelectionFromPointer(
    isCenterSpawned: Boolean,
    pointer: Offset,
    selectionOrigin: Offset,
    centerAngle: Float,
    itemCount: Int,
    itemSpreadDegrees: Float,
    itemPositions: List<Offset>,
    centerDeadZonePx: Float,
    nearestDeadZonePx: Float
): Int? {
    return if (isCenterSpawned) {
        // Center-spawned keyboard menus are selected by flick direction
        // from the cursor's key-down position, not from visual menu center.
        val dragX = pointer.x - selectionOrigin.x
        val dragY = pointer.y - selectionOrigin.y
        val distanceSq = dragX * dragX + dragY * dragY
        val deadZoneSq = centerDeadZonePx * centerDeadZonePx
        if (distanceSq <= deadZoneSq) {
            null
        } else {
            RadialMenuMath.getSelectionFromDrag(
                dragX = dragX,
                dragY = dragY,
                centerAngle = centerAngle,
                itemCount = itemCount,
                spreadDegrees = itemSpreadDegrees,
                deadZonePx = 0f,
                selectionDeadZoneDeg = 180f
            )
        }
    } else {
        RadialMenuMath.getNearestItemSelection(
            pointerX = pointer.x,
            pointerY = pointer.y,
            itemPositions = itemPositions,
            deadZonePx = nearestDeadZonePx
        )
    }
}

private fun handleKeyboardHoldKeyEvent(
    keyEvent: KeyEvent,
    effectiveTrigger: RadialMenuTriggerMode,
    isKeyboardMenuOpen: Boolean,
    windowWidth: Float,
    windowHeight: Float,
    hoveredItemIndex: Int?,
    hapticFeedback: HapticFeedback,
    openMenuAtCenter: (Offset) -> Unit,
    commitAndClose: (Int?) -> Unit
): Boolean {
    val keyboardMode = effectiveTrigger as? RadialMenuTriggerMode.KeyboardHold ?: return false
    if (keyEvent.key != keyboardMode.key) return false

    return when (keyEvent.type) {
        KeyEventType.KeyDown -> {
            if (keyboardHoldShouldOpenMenu(isKeyboardMenuOpen)) {
                val spawnAtCenter = Offset(
                    windowWidth.coerceAtLeast(1f) / 2f,
                    windowHeight.coerceAtLeast(1f) / 2f
                )
                hapticFeedback.vibrate(50)
                openMenuAtCenter(spawnAtCenter)
            }
            true
        }

        KeyEventType.KeyUp -> {
            if (isKeyboardMenuOpen) {
                commitAndClose(
                    keyboardHoldCommittedSelection(
                        isKeyboardMenuOpen = isKeyboardMenuOpen,
                        hoveredItemIndex = hoveredItemIndex
                    )
                )
            }
            true
        }

        else -> false
    }
}

/**
 * Edge-hug activation gate shared by wrapper/view layers.
 *
 * Edge-hug is automatically skipped for center-spawned menus because corner
 * clipping is geometrically irrelevant when the menu origin is screen center.
 */
internal fun shouldUseEdgeHugLayout(
    enableEdgeHugLayout: Boolean,
    isCenterSpawned: Boolean,
    zone: MenuZone,
    itemsCount: Int
): Boolean {
    return enableEdgeHugLayout &&
        !isCenterSpawned &&
        zone != MenuZone.CENTER &&
        itemsCount > CORNER_ITEM_THRESHOLD
}

/**
 * A wrapper composable that detects trigger gestures and shows the radial menu overlay.
 *
 * @param items The items to display. Supports 2-8 items (more is allowed but may degrade UX).
 * @param onItemSelected Callback fired when an item is selected from the radial menu.
 * @param enableEdgeHugLayout When true, items arrange in an L-shape along screen edges
 *   when long-pressed in a corner with 4+ items. Defaults to false (pure radial layout).
 * @param triggerMode Controls how the radial menu is opened.
 *   Defaults to [RadialMenuTriggerMode.Auto] (LongPress on Android, SecondaryClick on Desktop).
 * @param onTap Callback fired when the wrapped content is tapped.
 * @param onDoubleTap Callback fired when the wrapped content is double-tapped.
 * @param modifier Optional modifier for the wrapper box.
 * @param content The content to be wrapped and monitored for gestures.
 * @since 1.0.0
 */
@Suppress("LongMethod")
@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun RadialMenuWrapper(
    items: List<RadialMenuItem>,
    onItemSelected: (RadialMenuItem) -> Unit,
    enableEdgeHugLayout: Boolean = false,
    triggerMode: RadialMenuTriggerMode = RadialMenuTriggerMode.Auto,
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
    val touchSlopSq = touchSlop * touchSlop
    val dragSelectionThresholdSq = 30f * 30f
    val edgeHugHitRadiusPx = with(density) { ICON_SIZE_DP.dp.toPx() * 0.75f }
    val menuRadiusPx = with(density) { MENU_RADIUS_DP.dp.toPx() }
    val hapticFeedback = rememberHapticFeedback()
    val layoutDirection = LocalLayoutDirection.current
    val effectiveTrigger = remember(triggerMode) { resolveTriggerMode(triggerMode) }
    val resolvedPositionAware = remember(effectiveTrigger) { resolvePositionAware(effectiveTrigger) }
    val focusRequester = remember { FocusRequester() }

    fun getSelectionFromDrag(
        dragX: Float,
        dragY: Float,
        centerAngle: Float,
        itemCount: Int,
        spreadDegrees: Float
    ): Int? = RadialMenuMath.getSelectionFromDrag(
        dragX = dragX,
        dragY = dragY,
        centerAngle = centerAngle,
        itemCount = itemCount,
        spreadDegrees = spreadDegrees
    )

    var isKeyboardMenuOpen by remember { mutableStateOf(false) }
    var keyboardInitialState by remember { mutableStateOf<InitialMenuState?>(null) }
    var keyboardSpawnPosition by remember { mutableStateOf(Offset.Zero) }
    var keyboardSelectionOrigin by remember { mutableStateOf(Offset.Zero) }
    var keyboardSelectionIndex by remember { mutableStateOf<Int?>(null) }
    var lastKnownPointerAbsolute by remember { mutableStateOf<Offset?>(null) }

    fun openMenu(
        spawnAbsolute: Offset,
        isCenterSpawned: Boolean,
        isPositionAware: Boolean,
        useFullCircleLayout: Boolean
    ): InitialMenuState {
        val containerW = containerSize.width.toFloat().coerceAtLeast(1f)
        val containerH = containerSize.height.toFloat().coerceAtLeast(1f)
        val winW = windowWidth.coerceAtLeast(1f)
        val winH = windowHeight.coerceAtLeast(1f)
        val isRtl = layoutDirection == LayoutDirection.Rtl

        val initialState = computeInitialMenuState(
            absolutePosition = spawnAbsolute,
            winW = winW,
            winH = winH,
            containerW = containerW,
            containerH = containerH,
            isRtl = isRtl,
            density = density,
            itemsCount = items.size,
            enableEdgeHugLayout = enableEdgeHugLayout,
            isCenterSpawned = isCenterSpawned,
            isPositionAware = isPositionAware,
            useFullCircleLayout = useFullCircleLayout
        )

        globalMenuState.value = RadialMenuState(
            isVisible = true,
            touchPosition = spawnAbsolute,
            dragOffset = Offset.Zero,
            currentSelectionIndex = null,
            centerAngle = initialState.centerAngle,
            zone = initialState.zone,
            edgeHugPositions = initialState.edgeHugPositions
        )
        globalMenuSpreadDegrees.floatValue = initialState.itemSpreadDegrees
        return initialState
    }

    fun updateSelection(
        initialState: InitialMenuState,
        absolutePointer: Offset,
        dragOffset: Offset
    ): Int? {
        val dragDistSq = dragOffset.x * dragOffset.x + dragOffset.y * dragOffset.y
        if (dragDistSq <= dragSelectionThresholdSq) {
            globalMenuState.value = globalMenuState.value.copy(
                dragOffset = dragOffset,
                currentSelectionIndex = null
            )
            return null
        }

        val newIndex = if (initialState.edgeHugPositions != null) {
            RadialMenuMath.getNearestItemSelection(
                pointerX = absolutePointer.x,
                pointerY = absolutePointer.y,
                itemPositions = initialState.edgeHugPositions,
                deadZonePx = edgeHugHitRadiusPx
            )
        } else {
            getSelectionFromDrag(
                dragX = dragOffset.x,
                dragY = dragOffset.y,
                centerAngle = initialState.centerAngle,
                itemCount = items.size,
                spreadDegrees = initialState.itemSpreadDegrees
            )
        }

        globalMenuState.value = globalMenuState.value.copy(
            dragOffset = dragOffset,
            currentSelectionIndex = newIndex
        )
        return newIndex
    }

    fun closeMenuAndCommit(selectionIndex: Int?) {
        if (selectionIndex != null) {
            hapticFeedback.vibrate(30)
            onItemSelected(items[selectionIndex])
        }
        isKeyboardMenuOpen = false
        keyboardInitialState = null
        keyboardSelectionOrigin = Offset.Zero
        keyboardSelectionIndex = null
        globalMenuSpreadDegrees.floatValue = ICON_SPREAD_DEGREES
        globalMenuState.value = RadialMenuState()
    }

    fun updateKeyboardHoverSelection(absolutePointer: Offset): Int? {
        val initialState = keyboardInitialState ?: return null
        val selectionOrigin = keyboardSelectionOrigin

        val itemPositions = initialState.edgeHugPositions ?: List(items.size) { index ->
            val itemAngle =
                initialState.centerAngle + ((index - (items.size - 1) / 2f) * initialState.itemSpreadDegrees)
            val angleRad = kotlin.math.PI / 180 * itemAngle
            Offset(
                keyboardSpawnPosition.x + (menuRadiusPx * cos(angleRad)).toFloat(),
                keyboardSpawnPosition.y + (menuRadiusPx * sin(angleRad)).toFloat()
            )
        }

        val newIndex = keyboardHoldHoverSelectionFromPointer(
            isCenterSpawned = true,
            pointer = absolutePointer,
            selectionOrigin = selectionOrigin,
            centerAngle = initialState.centerAngle,
            itemCount = items.size,
            itemSpreadDegrees = initialState.itemSpreadDegrees,
            itemPositions = itemPositions,
            centerDeadZonePx = DEAD_ZONE_PX,
            nearestDeadZonePx = edgeHugHitRadiusPx
        )

        val dragOffset = absolutePointer - selectionOrigin
        globalMenuState.value = globalMenuState.value.copy(
            dragOffset = dragOffset,
            currentSelectionIndex = newIndex
        )
        return newIndex
    }

    val handleKeyboardEvent: (KeyEvent) -> Boolean = { keyEvent ->
        handleKeyboardHoldKeyEvent(
            keyEvent = keyEvent,
            effectiveTrigger = effectiveTrigger,
            isKeyboardMenuOpen = isKeyboardMenuOpen,
            windowWidth = windowWidth,
            windowHeight = windowHeight,
            hoveredItemIndex = keyboardSelectionIndex,
            hapticFeedback = hapticFeedback,
            openMenuAtCenter = { spawnAtCenter ->
                isKeyboardMenuOpen = true
                keyboardSpawnPosition = spawnAtCenter
                keyboardSelectionOrigin = lastKnownPointerAbsolute ?: spawnAtCenter
                keyboardInitialState = openMenu(
                    spawnAbsolute = spawnAtCenter,
                    isCenterSpawned = true,
                    isPositionAware = false,
                    useFullCircleLayout = true
                )
                keyboardSelectionIndex = null
            },
            commitAndClose = { selectionIndex -> closeMenuAndCommit(selectionIndex) }
        )
    }

    val handleKeyboardPointerMove by rememberUpdatedState<(Offset) -> Unit> { absolutePointer ->
        lastKnownPointerAbsolute = absolutePointer
        val newIndex = updateKeyboardHoverSelection(absolutePointer)
        if (newIndex != keyboardSelectionIndex) {
            if (newIndex != null) {
                hapticFeedback.vibrate(20)
            }
            keyboardSelectionIndex = newIndex
        }
    }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }
    LaunchedEffect(effectiveTrigger) {
        if (effectiveTrigger is RadialMenuTriggerMode.KeyboardHold) {
            focusRequester.requestFocus()
        }
    }
    LaunchedEffect(effectiveTrigger, isKeyboardMenuOpen) {
        globalKeyboardPointerMoveHandler.value = if (
            effectiveTrigger is RadialMenuTriggerMode.KeyboardHold && isKeyboardMenuOpen
        ) {
            handleKeyboardPointerMove
        } else {
            null
        }
    }
    DisposableEffect(Unit) {
        onDispose {
            globalKeyboardPointerMoveHandler.value = null
        }
    }

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
            .pointerInput(containerPosition) {
                awaitPointerEventScope {
                    while (true) {
                        val event = awaitPointerEvent(PointerEventPass.Main)
                        val pointer = event.changes.firstOrNull() ?: continue
                        lastKnownPointerAbsolute = containerPosition + pointer.position
                    }
                }
            }
            .focusRequester(focusRequester)
            .focusable()
            .onPreviewKeyEvent { keyEvent ->
                if (usePreviewKeyEventForKeyboardHold) {
                    handleKeyboardEvent(keyEvent)
                } else {
                    false
                }
            }
            .onKeyEvent { keyEvent ->
                if (!usePreviewKeyEventForKeyboardHold) {
                    handleKeyboardEvent(keyEvent)
                } else {
                    false
                }
            }
    ) {
        val triggerModifier = Modifier
            .applyLongPressTrigger(
                items = items,
                enableEdgeHugLayout = enableEdgeHugLayout,
                effectiveTrigger = effectiveTrigger,
                touchSlopSq = touchSlopSq,
                tapCallbacks = TapCallbacks(onTap = onTap, onDoubleTap = onDoubleTap),
                tapStateCallbacks = TapStateCallbacks(
                    getLastTapTime = { lastTapTime },
                    setLastTapTime = { lastTapTime = it }
                ),
                hapticFeedback = hapticFeedback,
                containerPosition = containerPosition,
                openMenuAt = { spawnAbsolute, isCenterSpawned ->
                    openMenu(
                        spawnAbsolute = spawnAbsolute,
                        isCenterSpawned = isCenterSpawned,
                        isPositionAware = resolvedPositionAware,
                        useFullCircleLayout = false
                    )
                },
                updateSelectionForPointer = { initialState, absolutePointer, dragOffset ->
                    updateSelection(initialState, absolutePointer, dragOffset)
                },
                closeAndCommit = { selectionIndex -> closeMenuAndCommit(selectionIndex) }
            )
            .applySecondaryClickTrigger(
                items = items,
                enableEdgeHugLayout = enableEdgeHugLayout,
                effectiveTrigger = effectiveTrigger,
                hapticFeedback = hapticFeedback,
                containerPosition = containerPosition,
                openMenuAt = { spawnAbsolute, isCenterSpawned ->
                    openMenu(
                        spawnAbsolute = spawnAbsolute,
                        isCenterSpawned = isCenterSpawned,
                        isPositionAware = resolvedPositionAware,
                        useFullCircleLayout = true
                    )
                },
                updateSelectionForPointer = { initialState, absolutePointer, dragOffset ->
                    updateSelection(initialState, absolutePointer, dragOffset)
                },
                closeAndCommit = { selectionIndex -> closeMenuAndCommit(selectionIndex) }
            )

        Box(modifier = triggerModifier) {
            content()
        }
    }
}

private fun Modifier.applyLongPressTrigger(
    items: List<RadialMenuItem>,
    enableEdgeHugLayout: Boolean,
    effectiveTrigger: RadialMenuTriggerMode,
    touchSlopSq: Float,
    tapCallbacks: TapCallbacks,
    tapStateCallbacks: TapStateCallbacks,
    hapticFeedback: HapticFeedback,
    containerPosition: Offset,
    openMenuAt: (Offset, Boolean) -> InitialMenuState,
    updateSelectionForPointer: (InitialMenuState, Offset, Offset) -> Int?,
    closeAndCommit: (Int?) -> Unit
): Modifier {
    return pointerInput(items, enableEdgeHugLayout, effectiveTrigger) {
        if (effectiveTrigger !is RadialMenuTriggerMode.LongPress) return@pointerInput

        awaitEachGesture {
            val down = awaitFirstDown(requireUnconsumed = false)
            val startPosition = down.position
            val pointerId = down.id
            val downTime = kotlin.time.TimeSource.Monotonic.markNow()
            val absolutePosition = containerPosition + startPosition

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

                        val distSq =
                            (change.position.x - startPosition.x).let { it * it } +
                                (change.position.y - startPosition.y).let { it * it }
                        if (distSq > touchSlopSq) {
                            moved = true
                        }
                    }
                } catch (_: Exception) {
                }
            }

            if (isLongPress) {
                hapticFeedback.vibrate(50)
                val initialState = openMenuAt(absolutePosition, false)

                var lastPosition = startPosition
                var currentDrag = Offset.Zero
                var currentSelectionIndex: Int? = null

                while (true) {
                    val event = awaitPointerEvent(PointerEventPass.Main)
                    val change = event.changes.find { it.id == pointerId } ?: break

                    val currentPos = change.position
                    val delta = currentPos - lastPosition
                    lastPosition = currentPos
                    currentDrag += delta
                    change.consume()

                    if (!change.pressed) {
                        break
                    }

                    val absolutePointer = containerPosition + currentPos
                    val newIndex = updateSelectionForPointer(initialState, absolutePointer, currentDrag)

                    if (newIndex != currentSelectionIndex) {
                        if (newIndex != null) {
                            hapticFeedback.vibrate(20)
                        }
                        currentSelectionIndex = newIndex
                    }
                }

                closeAndCommit(currentSelectionIndex)
            } else if (!moved && released) {
                val timeSinceLastTap = System.currentTimeMillis() - tapStateCallbacks.getLastTapTime()
                if (timeSinceLastTap < DOUBLE_TAP_TIMEOUT_MS && timeSinceLastTap > 0) {
                    tapCallbacks.onDoubleTap()
                    tapStateCallbacks.setLastTapTime(0L)
                } else {
                    tapStateCallbacks.setLastTapTime(System.currentTimeMillis())
                    tapCallbacks.onTap()
                }
            }
        }
    }
}

private fun Modifier.applySecondaryClickTrigger(
    items: List<RadialMenuItem>,
    enableEdgeHugLayout: Boolean,
    effectiveTrigger: RadialMenuTriggerMode,
    hapticFeedback: HapticFeedback,
    containerPosition: Offset,
    openMenuAt: (Offset, Boolean) -> InitialMenuState,
    updateSelectionForPointer: (InitialMenuState, Offset, Offset) -> Int?,
    closeAndCommit: (Int?) -> Unit
): Modifier {
    return pointerInput(items, enableEdgeHugLayout, effectiveTrigger) {
        if (effectiveTrigger !is RadialMenuTriggerMode.SecondaryClick) return@pointerInput

        awaitEachGesture {
            var initialState: InitialMenuState? = null
            var spawnAbsolute = Offset.Zero
            var currentSelectionIndex: Int? = null

            while (true) {
                val event = awaitPointerEvent(PointerEventPass.Main)
                val pointer = event.changes.firstOrNull() ?: continue
                val absolutePointer = containerPosition + pointer.position

                if (initialState == null) {
                    if (event.type == PointerEventType.Press && event.buttons.isSecondaryPressed) {
                        hapticFeedback.vibrate(50)
                        spawnAbsolute = absolutePointer
                        initialState = openMenuAt(spawnAbsolute, false)
                        pointer.consume()
                    } else if (event.type == PointerEventType.Release) {
                        break
                    }
                    continue
                }

                val dragOffset = absolutePointer - spawnAbsolute
                val newIndex = updateSelectionForPointer(initialState, absolutePointer, dragOffset)

                if (newIndex != currentSelectionIndex) {
                    if (newIndex != null) {
                        hapticFeedback.vibrate(20)
                    }
                    currentSelectionIndex = newIndex
                }

                if (event.type == PointerEventType.Release || !event.buttons.isSecondaryPressed) {
                    closeAndCommit(currentSelectionIndex)
                    break
                }
            }
        }
    }
}

private data class InitialMenuState(
    val zone: MenuZone,
    val centerAngle: Float,
    val itemSpreadDegrees: Float,
    val edgeHugPositions: List<Offset>?
)

@Suppress("LongParameterList")
private fun computeInitialMenuState(
    absolutePosition: Offset,
    winW: Float,
    winH: Float,
    containerW: Float,
    containerH: Float,
    isRtl: Boolean,
    density: androidx.compose.ui.unit.Density,
    itemsCount: Int,
    enableEdgeHugLayout: Boolean,
    isCenterSpawned: Boolean,
    isPositionAware: Boolean,
    useFullCircleLayout: Boolean
): InitialMenuState {
    val edgeThreshPx = with(density) { EDGE_THRESH_DP.dp.toPx() }
    val zone = RadialMenuMath.detectZone(
        absolutePosition.x, absolutePosition.y,
        winW, winH, edgeThreshPx
    )

    // Center-spawned menus (KeyboardHold) are never corner-constrained,
    // so edge-hug is intentionally bypassed even when enableEdgeHugLayout is true.
    val useEdgeHug = shouldUseEdgeHugLayout(
        enableEdgeHugLayout = enableEdgeHugLayout,
        isCenterSpawned = isCenterSpawned,
        zone = zone,
        itemsCount = itemsCount
    )

    return if (useEdgeHug) {
        val itemSizePx = with(density) { ICON_SIZE_DP.dp.toPx() }
        val gapPx = with(density) { EDGE_HUG_GAP_DP.dp.toPx() }
        val padPx = with(density) { EDGE_HUG_PAD_DP.dp.toPx() }
        InitialMenuState(
            zone = zone,
            centerAngle = 0f,
            itemSpreadDegrees = ICON_SPREAD_DEGREES,
            edgeHugPositions = RadialMenuMath.edgeHugLayout(
                zone, winW, winH,
                itemsCount, itemSizePx * 1.5f, gapPx, padPx
            )
        )
    } else {
        val spreadDegrees = if (useFullCircleLayout) {
            keyboardHoldSpreadDegrees(itemsCount)
        } else {
            ICON_SPREAD_DEGREES
        }
        val centerAngle = if (useFullCircleLayout) {
            if (!isCenterSpawned && isPositionAware) {
                resolveCenterAngle(
                    isPositionAware = true,
                    position = absolutePosition,
                    containerWidth = containerW,
                    containerHeight = containerH,
                    isRtl = isRtl
                )
            } else {
                centerSpawnedCenterAngle(itemsCount)
            }
        } else {
            resolveCenterAngle(
                isPositionAware = isPositionAware,
                position = absolutePosition,
                containerWidth = containerW,
                containerHeight = containerH,
                isRtl = isRtl
            )
        }
        InitialMenuState(
            zone = zone,
            centerAngle = centerAngle,
            itemSpreadDegrees = spreadDegrees,
            edgeHugPositions = null
        )
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
    val keyboardPointerMoveHandler = globalKeyboardPointerMoveHandler.value

    if (menuState.isVisible) {
        Popup(
            properties = PopupProperties(focusable = false)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(colors.overlayColor)
                    .pointerInput(keyboardPointerMoveHandler) {
                        val pointerHandler = keyboardPointerMoveHandler ?: return@pointerInput
                        awaitPointerEventScope {
                            while (true) {
                                val event = awaitPointerEvent(PointerEventPass.Main)
                                if (event.type != PointerEventType.Move) continue
                                val pointer = event.changes.firstOrNull() ?: continue
                                pointerHandler(pointer.position)
                            }
                        }
                    }
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
                val itemAngle = centerAngle + ((i - (itemCount - 1) / 2f) * globalMenuSpreadDegrees.floatValue)
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
