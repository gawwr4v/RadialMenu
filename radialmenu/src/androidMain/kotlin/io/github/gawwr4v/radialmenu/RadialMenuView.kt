package io.github.gawwr4v.radialmenu

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.graphics.Rect
import android.graphics.RectF
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import android.view.ViewGroup
import android.view.accessibility.AccessibilityEvent
import android.widget.FrameLayout
import androidx.compose.ui.geometry.Offset
import androidx.annotation.Keep
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

private const val TAG = "RadialMenuView"
private const val LONG_PRESS_TIMEOUT_MS = 400L
private const val DOUBLE_TAP_TIMEOUT_MS = 300L
private const val DEFAULT_MENU_RADIUS_DP = 90f
private const val DEFAULT_ICON_SIZE_DP = 32f
private const val DEFAULT_SPREAD_DEGREES = 45f

/**
 * A custom, drag-to-select radial menu Android View component.
 *
 * Features dynamic angle calculation to prevent edge clipping, gesture tracking,
 * continuous physics-based selection, and support for an unlimited number of items.
 *
 * This View operates entirely on the Android Canvas API with zero Compose dependencies.
 *
 * **Z-Order Note:** For correct z-ordering (menu above toolbars, bottom nav, FABs),
 * this View expects an Activity context. If the context is not an Activity (e.g.,
 * Dialog, Service), the overlay will render within the current view hierarchy
 * instead of above all UI elements.
 *
 * @since 1.0.0
 */
@Keep
class RadialMenuView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    /** Callback invoked on a single tap. */
    var onTap: (() -> Unit)? = null

    /** Callback invoked on a double tap. */
    var onDoubleTap: (() -> Unit)? = null

    /**
     * Callback invoked when a radial menu item is selected.
     * @since 1.0.0
     */
    var onItemSelected: ((RadialMenuItem) -> Unit)? = null

    /** Callback invoked when the radial menu is opened via long press. */
    var onMenuOpened: (() -> Unit)? = null

    /** Callback invoked when the radial menu is closed after a gesture finishes. */
    var onMenuClosed: (() -> Unit)? = null

    /**
     * When true, items arrange in an L-shape along screen edges when
     * long-pressed in a corner with 4+ items. Defaults to false (pure radial layout).
     *
     * Set to true to opt in to the edge-hug layout for corner touches.
     * @since 1.0.3
     */
    var enableEdgeHugLayout: Boolean = false

    /**
     * Controls how the menu is triggered in [RadialMenuView].
     *
     * [RadialMenuTriggerMode.LongPress] is the default and fully supported.
     * [RadialMenuTriggerMode.SecondaryClick] is supported for mouse/desktop-class
     * input on Android form factors.
     *
     * [RadialMenuTriggerMode.KeyboardHold] is accepted for API symmetry but not
     * supported in this View-based implementation. Use [RadialMenuWrapper] for
     * keyboard-hold behavior.
     *
     * @since 1.0.4
     */
    var triggerMode: RadialMenuTriggerMode = RadialMenuTriggerMode.LongPress(positionAware = true)
        set(value) {
            field = value
            if (value is RadialMenuTriggerMode.KeyboardHold) {
                logKeyboardHoldNotSupported()
            }
        }

    // XML Attributes
    private var accentColor: Int = Color.WHITE
    private var menuRadiusPx: Float = 0f
    private var iconSizePx: Float = 0f
    private var overlayColor: Int = Color.argb(128, 0, 0, 0)
    private var badgeColorInt: Int = Color.parseColor("#FF4444")
    private var animationDurationMs: Long = 100L

    // Cached at instance level — avoids allocation inside onDraw() at 60fps (Bug 3 fix)
    private val selectedColorFilter = PorterDuffColorFilter(Color.BLACK, PorterDuff.Mode.SRC_IN)
    private val unselectedColorFilter = PorterDuffColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN)

    // Cached Compose drawing objects — avoids allocation inside onDraw() at 60fps (Bug 2 fix)
    private val composeDrawScope = androidx.compose.ui.graphics.drawscope.CanvasDrawScope()
    private var composeCanvas: androidx.compose.ui.graphics.Canvas? = null
    private var lastAndroidCanvas: android.graphics.Canvas? = null
    private var cachedIconSize: androidx.compose.ui.geometry.Size = androidx.compose.ui.geometry.Size.Zero
    
    // Cached Rect for Painter bounds
    private val itemBoundsRect = android.graphics.Rect()

    // Items
    private var menuItems: List<RadialMenuItem> = emptyList()

    /** Returns true if the radial menu is currently visible and active. */
    var isMenuOpen: Boolean = false
        private set

    private var touchX: Float = 0f
    private var touchY: Float = 0f
    private var dragX: Float = 0f
    private var dragY: Float = 0f
    private var currentSelectionIndex: Int? = null
    private var centerAngle: Float = 270f

    // Edge-hug layout state (null = radial mode, non-null = edge-hug mode)
    private var edgeHugPositions: List<Offset>? = null

    // DecorView overlay for z-ordering above all UI elements
    private var overlayView: View? = null
    private val visibleDisplayRect = Rect()

    // Paint & Allocation objects for onDraw
    private val overlayPaint = Paint()
    private val backgroundPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        alpha = (255 * 0.3f).toInt()
    }
    private val bgCirclePaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        alpha = (255 * 0.7f).toInt()
        strokeWidth = 4f
    }
    private val badgePaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val badgeTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        textAlign = Paint.Align.CENTER
    }
    private val defaultItemBackgroundColor = Color.parseColor("#424242")
    private val tmpLocationOnScreen = IntArray(2)

    // Gesture tracking
    private var lastTapTime: Long = 0L
    private val handler = Handler(Looper.getMainLooper())
    private var longPressRunnable: Runnable? = null
    private var touchSlop: Int = ViewConfiguration.get(context).scaledTouchSlop
    private var moved: Boolean = false
    private var startX: Float = 0f
    private var startY: Float = 0f
    private var keyboardHoldWarningLogged: Boolean = false

    // Dynamic animation state: one scale per item
    private var itemScales: FloatArray = FloatArray(0)
    private var itemAnimators: Array<ValueAnimator?> = emptyArray()

    private val vibrator: Vibrator by lazy {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }
    }

    init {
        val density = context.resources.displayMetrics.density
        val defaultRadiusPx = DEFAULT_MENU_RADIUS_DP * density
        val defaultIconSizePx = DEFAULT_ICON_SIZE_DP * density

        if (attrs != null) {
            val typedArray = context.obtainStyledAttributes(attrs, R.styleable.RadialMenuView)
            accentColor = typedArray.getColor(R.styleable.RadialMenuView_rm_accentColor, Color.WHITE)
            menuRadiusPx = typedArray.getDimension(R.styleable.RadialMenuView_rm_menuRadius, defaultRadiusPx)
            iconSizePx = typedArray.getDimension(R.styleable.RadialMenuView_rm_iconSize, defaultIconSizePx)
            overlayColor = typedArray.getColor(R.styleable.RadialMenuView_rm_overlayColor, Color.argb(128, 0, 0, 0))
            badgeColorInt = typedArray.getColor(R.styleable.RadialMenuView_rm_badgeColor, Color.parseColor("#FF4444"))
            animationDurationMs = typedArray.getInt(R.styleable.RadialMenuView_rm_animationDurationMs, 100).toLong()
            typedArray.recycle()
        } else {
            accentColor = Color.WHITE
            menuRadiusPx = defaultRadiusPx
            iconSizePx = defaultIconSizePx
        }
        overlayPaint.color = overlayColor
        badgePaint.color = badgeColorInt
        
        cachedIconSize = androidx.compose.ui.geometry.Size(iconSizePx, iconSizePx)

        // Accessibility
        contentDescription = "Radial menu"
        importantForAccessibility = IMPORTANT_FOR_ACCESSIBILITY_YES
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        // Cached at instance level — avoids allocation inside onDraw() at 60fps
        cachedIconSize = androidx.compose.ui.geometry.Size(iconSizePx, iconSizePx)
    }

    private fun getOrCreateComposeCanvas(canvas: Canvas): androidx.compose.ui.graphics.Canvas {
        if (composeCanvas == null || lastAndroidCanvas !== canvas) {
            composeCanvas = androidx.compose.ui.graphics.Canvas(canvas)
            lastAndroidCanvas = canvas
        }
        return composeCanvas!!
    }

    /**
     * Sets the list of items to display in the menu.
     *
     * If called while the menu is open, the menu will close first, then update.
     * An empty list will prevent the menu from opening.
     *
     * @param items The list of [RadialMenuItem] to display. Supports 2–8 items optimally.
     * @since 1.0.0
     */
    fun setItems(items: List<RadialMenuItem>) {
        if (items.isEmpty()) {
            Log.w(TAG, "setItems() called with empty list. Menu will not open.")
        }
        if (items.size > 8) {
            Log.w(TAG, "More than 8 items may cause poor UX on small screens")
        }
        if (isMenuOpen) {
            closeMenu()
        }
        this.menuItems = items
        // Resize animation arrays
        itemScales = FloatArray(items.size) { 1.0f }
        itemAnimators = Array(items.size) { null }
        post { invalidate() }
    }

    /**
     * Updates the active/toggle state of an item by its [id].
     *
     * @param id The [RadialMenuItem.id] to update.
     * @param active The new active state.
     * @since 1.0.0
     */
    fun setItemActive(id: Int, active: Boolean) {
        menuItems = menuItems.map {
            if (it.id == id) it.copy(isActive = active) else it
        }
        post { invalidate() }
    }

    private fun vibrate(ms: Long = 30) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(ms, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(ms)
            }
        } catch (e: Exception) {
            Log.w(TAG, "Vibration failed", e)
        }
    }

    private fun calculateCenterAngle(x: Float, y: Float, screenWidth: Float, screenHeight: Float): Float {
        val isRtl = layoutDirection == View.LAYOUT_DIRECTION_RTL
        return RadialMenuMath.calculateCenterAngle(x, y, screenWidth, screenHeight, isRtl)
    }

    private fun getSelectionFromDrag(dragX: Float, dragY: Float, centerAngle: Float): Int? =
        RadialMenuMath.getSelectionFromDrag(dragX, dragY, centerAngle, menuItems.size)

    private fun logKeyboardHoldNotSupported() {
        if (!keyboardHoldWarningLogged) {
            Log.w(
                TAG,
                "RadialMenuTriggerMode.KeyboardHold is not supported in RadialMenuView. " +
                    "Use RadialMenuWrapper for keyboard-hold triggers."
            )
            keyboardHoldWarningLogged = true
        }
    }

    private fun effectiveTriggerMode(): RadialMenuTriggerMode {
        return when (val mode = triggerMode) {
            RadialMenuTriggerMode.Auto -> RadialMenuTriggerMode.LongPress(positionAware = true)
            is RadialMenuTriggerMode.KeyboardHold -> {
                logKeyboardHoldNotSupported()
                RadialMenuTriggerMode.LongPress(positionAware = true)
            }

            else -> mode
        }
    }

    private fun isSecondaryButtonEvent(event: MotionEvent): Boolean {
        return (event.buttonState and MotionEvent.BUTTON_SECONDARY) != 0
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (menuItems.isEmpty()) return super.onTouchEvent(event)

        val x = event.x
        val y = event.y
        val effectiveTriggerMode = effectiveTriggerMode()
        val resolvedPositionAware = when (effectiveTriggerMode) {
            is RadialMenuTriggerMode.LongPress -> effectiveTriggerMode.positionAware
            is RadialMenuTriggerMode.SecondaryClick -> effectiveTriggerMode.positionAware
            is RadialMenuTriggerMode.KeyboardHold -> false
            RadialMenuTriggerMode.Auto -> false
        }

        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                if (effectiveTriggerMode is RadialMenuTriggerMode.SecondaryClick && isSecondaryButtonEvent(event)) {
                    openMenuAt(x, y, isCenterSpawned = false, isPositionAware = resolvedPositionAware)
                    return true
                }

                startX = x
                startY = y
                moved = false

                // Post a delayed runnable to trigger the radial menu if the user 
                // holds their finger down without moving significantly.
                longPressRunnable = Runnable {
                    handleLongPress(x, y, resolvedPositionAware)
                }
                if (effectiveTriggerMode is RadialMenuTriggerMode.LongPress) {
                    handler.postDelayed(longPressRunnable!!, LONG_PRESS_TIMEOUT_MS)
                }
                return true
            }
            MotionEvent.ACTION_BUTTON_PRESS -> {
                if (effectiveTriggerMode is RadialMenuTriggerMode.SecondaryClick && isSecondaryButtonEvent(event)) {
                    openMenuAt(x, y, isCenterSpawned = false, isPositionAware = resolvedPositionAware)
                    return true
                }
            }
            MotionEvent.ACTION_MOVE -> {
                if (isMenuOpen) {
                    val deltaX = x - touchX
                    val deltaY = y - touchY

                    dragX = deltaX
                    dragY = deltaY

                    val dragDistSq = dragX * dragX + dragY * dragY
                    if (dragDistSq > 30f * 30f) {
                        val positions = edgeHugPositions
                        val newIndex = if (positions != null) {
                            // Edge-hug mode: select only when the pointer is actually on an item.
                            RadialMenuMath.getNearestItemSelection(
                                pointerX = x,
                                pointerY = y,
                                itemPositions = positions,
                                deadZonePx = iconSizePx * 0.75f
                            )
                        } else {
                            // Radial mode: existing angle-based selection
                            getSelectionFromDrag(dragX, dragY, centerAngle)
                        }

                        if (newIndex != currentSelectionIndex) {
                            if (newIndex != null) {
                                vibrate(20)
                                // Accessibility announcement
                                val item = menuItems[newIndex]
                                announceForAccessibility(item.contentDescription)
                            }
                            animateSelectionChange(currentSelectionIndex, newIndex)
                            currentSelectionIndex = newIndex
                        }
                    }
                    post { invalidate() }
                } else {
                    val distSq = (x - startX) * (x - startX) + (y - startY) * (y - startY)
                    val touchSlopSq = touchSlop.toFloat() * touchSlop.toFloat()
                    if (distSq > touchSlopSq && !moved) {
                        moved = true
                        longPressRunnable?.let { handler.removeCallbacks(it) }
                    }
                }
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL, MotionEvent.ACTION_BUTTON_RELEASE -> {
                longPressRunnable?.let { handler.removeCallbacks(it) }

                if (isMenuOpen) {
                    if (currentSelectionIndex != null) {
                        vibrate(30)
                        onItemSelected?.invoke(menuItems[currentSelectionIndex!!])
                    }
                    closeMenu()
                } else if (
                    effectiveTriggerMode is RadialMenuTriggerMode.LongPress &&
                    !moved && event.actionMasked == MotionEvent.ACTION_UP
                ) {
                    val timeSinceLastTap = System.currentTimeMillis() - lastTapTime
                    if (timeSinceLastTap in 1..<DOUBLE_TAP_TIMEOUT_MS) {
                        onDoubleTap?.invoke()
                        lastTapTime = 0L
                    } else {
                        lastTapTime = System.currentTimeMillis()
                        onTap?.invoke()
                    }
                }
            }
        }
        return super.onTouchEvent(event)
    }

    private fun handleLongPress(x: Float, y: Float, isPositionAware: Boolean) {
        if (!moved && menuItems.isNotEmpty()) {
            openMenuAt(x, y, isCenterSpawned = false, isPositionAware = isPositionAware)
        }
    }

    private fun openMenuAt(
        x: Float,
        y: Float,
        isCenterSpawned: Boolean,
        isPositionAware: Boolean
    ) {
        if (menuItems.isEmpty()) return

        vibrate(50)
        val density = context.resources.displayMetrics.density
        val edgeThreshPx = RadialMenuDefaults.EDGE_THRESH_DP * density

        // Use the window's visible display frame (excluding system bars)
        // for zone detection so we only detect true screen corners,
        // not edges near toolbars or nav bars.
        rootView.getWindowVisibleDisplayFrame(visibleDisplayRect)
        val usableWidth = visibleDisplayRect.width().toFloat()
        val usableHeight = visibleDisplayRect.height().toFloat()

        // Map touch coordinates relative to the visible display frame
        getLocationOnScreen(tmpLocationOnScreen)
        val screenX = tmpLocationOnScreen[0] + x - visibleDisplayRect.left
        val screenY = tmpLocationOnScreen[1] + y - visibleDisplayRect.top

        val zone = RadialMenuMath.detectZone(screenX, screenY, usableWidth, usableHeight, edgeThreshPx)
        val useEdgeHug = shouldUseEdgeHugLayout(
            enableEdgeHugLayout = enableEdgeHugLayout,
            isCenterSpawned = isCenterSpawned,
            zone = zone,
            itemsCount = menuItems.size
        )

        // Use the view's own dimensions for layout positioning (unchanged)
        val screenWidth = width.toFloat()
        val screenHeight = height.toFloat()

        if (useEdgeHug) {
            val itemSizePx = RadialMenuDefaults.ICON_SIZE_DP * density * 1.5f
            val gapPx = RadialMenuDefaults.EDGE_HUG_GAP_DP * density
            val padPx = RadialMenuDefaults.EDGE_HUG_PAD_DP * density
            edgeHugPositions = RadialMenuMath.edgeHugLayout(
                zone, screenWidth, screenHeight,
                menuItems.size, itemSizePx, gapPx, padPx
            )
            centerAngle = 0f // unused in edge-hug mode
        } else {
            edgeHugPositions = null
            centerAngle = if (isPositionAware) {
                calculateCenterAngle(x, y, screenWidth, screenHeight)
            } else {
                0f
            }
        }

        isMenuOpen = true
        touchX = x
        touchY = y
        dragX = 0f
        dragY = 0f
        currentSelectionIndex = null

        resetAllScales()
        attachOverlayToDecorView()

        onMenuOpened?.invoke()
        post { invalidate() }
    }

    private fun closeMenu() {
        isMenuOpen = false
        currentSelectionIndex = null
        dragX = 0f
        dragY = 0f
        edgeHugPositions = null
        resetAllScales()
        detachOverlayFromDecorView()
        onMenuClosed?.invoke()
        post { invalidate() }
    }

    /**
     * Attaches a transparent fullscreen overlay to the window's decor view
     * so menu items render above all other UI elements (toolbars, FABs, etc.).
     * Falls back to invalidating in-place if the context is not an Activity.
     */
    private fun attachOverlayToDecorView() {
        val activity = context as? Activity ?: return // Graceful fallback: draw in-place
        val decorView = activity.window?.decorView as? ViewGroup ?: return

        val overlay = object : View(context) {
            override fun onDraw(canvas: Canvas) {
                super.onDraw(canvas)
                this@RadialMenuView.drawMenuOverlay(canvas)
            }
        }
        overlay.layoutParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.MATCH_PARENT
        )
        decorView.addView(overlay)
        overlayView = overlay
    }

    /**
     * Removes the overlay view from the decor view when the menu closes.
     */
    private fun detachOverlayFromDecorView() {
        overlayView?.let { overlay ->
            (overlay.parent as? ViewGroup)?.removeView(overlay)
        }
        overlayView = null
    }

    private fun resetAllScales() {
        for (i in itemScales.indices) {
            itemAnimators[i]?.cancel()
            itemScales[i] = 1.0f
        }
    }

    private fun animateSelectionChange(oldIndex: Int?, newIndex: Int?) {
        if (oldIndex != null && oldIndex in itemScales.indices) {
            animateItemScale(oldIndex, 1.0f)
        }
        if (newIndex != null && newIndex in itemScales.indices) {
            animateItemScale(newIndex, 1.4f)
        }
    }

    private fun animateItemScale(index: Int, target: Float) {
        itemAnimators[index]?.cancel()
        itemAnimators[index] = ValueAnimator.ofFloat(itemScales[index], target).apply {
            duration = animationDurationMs
            addUpdateListener {
                itemScales[index] = it.animatedValue as Float
                post { invalidate() }
            }
            start()
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (!isMenuOpen) return

        if (overlayView != null) {
            // Overlay is attached to decorView — it handles drawing.
            // Trigger a redraw on the overlay instead.
            overlayView?.postInvalidate()
            return
        }

        // No overlay (non-Activity context fallback) — draw in-place
        drawMenuOverlay(canvas)
    }

    /**
     * Draws the full menu overlay (scrim, items, badges, drag indicator).
     * Called either from this view's onDraw (fallback) or from the
     * decorView overlay (z-ordered above all UI).
     */
    internal fun drawMenuOverlay(canvas: Canvas) {
        if (!isMenuOpen) return

        // When drawing on the decorView overlay, translate to this view's
        // position on screen so all coordinates stay correct.
        val isOverlay = overlayView != null
        if (isOverlay) {
            getLocationOnScreen(tmpLocationOnScreen)
            canvas.save()
            canvas.translate(tmpLocationOnScreen[0].toFloat(), tmpLocationOnScreen[1].toFloat())
            // Clip to this view's bounds
            canvas.clipRect(0f, 0f, width.toFloat(), height.toFloat())
        }

        // Draw scrim
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), overlayPaint)

        val positions = edgeHugPositions

        // Center indicator (only in radial mode)
        if (positions == null) {
            canvas.drawCircle(touchX, touchY, 16f, backgroundPaint)
        }

        val itemCount = menuItems.size
        for (i in 0 until itemCount) {
            val item = menuItems[i]

            // Determine icon center: edge-hug positions or radial angle calculation
            val iconCX: Float
            val iconCY: Float
            if (positions != null && i < positions.size) {
                iconCX = positions[i].x
                iconCY = positions[i].y
            } else {
                val itemAngle = centerAngle + ((i - (itemCount - 1) / 2f) * DEFAULT_SPREAD_DEGREES)
                val angleRad = Math.toRadians(itemAngle.toDouble())
                iconCX = touchX + menuRadiusPx * cos(angleRad).toFloat()
                iconCY = touchY + menuRadiusPx * sin(angleRad).toFloat()
            }

            val isSelected = currentSelectionIndex == i
            val scale = if (i < itemScales.size) itemScales[i] else 1.0f
            val scaledSize = iconSizePx * scale
            val bgRadius = scaledSize * 0.75f

            bgCirclePaint.color = if (isSelected) accentColor else defaultItemBackgroundColor
            canvas.drawCircle(iconCX, iconCY, bgRadius, bgCirclePaint)

            val drawable = if (item.isActive && item.iconActive != null) item.iconActive else item.icon
            val cf = if (isSelected) selectedColorFilter else unselectedColorFilter

            itemBoundsRect.set(
                (iconCX - scaledSize / 2).toInt(),
                (iconCY - scaledSize / 2).toInt(),
                (iconCX + scaledSize / 2).toInt(),
                (iconCY + scaledSize / 2).toInt()
            )

            drawable.drawWithCache(
                canvas = canvas,
                bounds = itemBoundsRect,
                colorFilter = cf,
                drawScope = composeDrawScope,
                composeCanvas = getOrCreateComposeCanvas(canvas),
                baseSize = cachedIconSize,
                scale = scale
            )

            // Badge
            val badgeText = item.badgeText ?: if (item.badgeCount > 0) {
                if (item.badgeCount > 99) "99+" else item.badgeCount.toString()
            } else null

            if (badgeText != null) {
                val badgeRadius = iconSizePx * 0.28f
                val badgeCX = iconCX + bgRadius * 0.6f
                val badgeCY = iconCY - bgRadius * 0.6f

                canvas.drawCircle(badgeCX, badgeCY, badgeRadius, badgePaint)

                badgeTextPaint.textSize = iconSizePx * 0.35f
                val fontMetrics = badgeTextPaint.fontMetrics
                val textHeight = fontMetrics.descent - fontMetrics.ascent
                canvas.drawText(
                    badgeText,
                    badgeCX,
                    badgeCY + textHeight / 2f - fontMetrics.descent,
                    badgeTextPaint
                )
            }
        }

        // Draw drag direction indicator (only in radial mode)
        if (positions == null) {
            val dragDist = sqrt(dragX * dragX + dragY * dragY)
            if (dragDist > 20f) {
                val indicatorLen = minOf(dragDist * 0.6f, menuRadiusPx * 0.5f)
                val normalizedX = dragX / dragDist
                val normalizedY = dragY / dragDist
                canvas.drawLine(
                    touchX, touchY,
                    touchX + normalizedX * indicatorLen,
                    touchY + normalizedY * indicatorLen,
                    linePaint
                )
            }
        }

        if (isOverlay) {
            canvas.restore()
        }
    }
}
