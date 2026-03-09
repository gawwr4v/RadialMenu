package io.github.gawwr4v.radialmenu

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
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
import android.view.accessibility.AccessibilityEvent
import androidx.annotation.Keep
import kotlin.math.abs
import kotlin.math.atan2
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

    // XML Attributes
    private var accentColor: Int = Color.WHITE
    private var menuRadiusPx: Float = 0f
    private var iconSizePx: Float = 0f
    private var overlayColor: Int = Color.argb(128, 0, 0, 0)
    private var badgeColorInt: Int = Color.parseColor("#FF4444")
    private var animationDurationMs: Long = 100L

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

    // Gesture tracking
    private var lastTapTime: Long = 0L
    private val handler = Handler(Looper.getMainLooper())
    private var longPressRunnable: Runnable? = null
    private var touchSlop: Int = ViewConfiguration.get(context).scaledTouchSlop
    private var moved: Boolean = false
    private var startX: Float = 0f
    private var startY: Float = 0f

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

        // Accessibility
        contentDescription = "Radial menu"
        importantForAccessibility = IMPORTANT_FOR_ACCESSIBILITY_YES
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

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (menuItems.isEmpty()) return super.onTouchEvent(event)

        val x = event.x
        val y = event.y

        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                startX = x
                startY = y
                moved = false

                // Post a delayed runnable to trigger the radial menu if the user 
                // holds their finger down without moving significantly.
                longPressRunnable = Runnable {
                    handleLongPress(x, y)
                }
                handler.postDelayed(longPressRunnable!!, LONG_PRESS_TIMEOUT_MS)
                return true
            }
            MotionEvent.ACTION_MOVE -> {
                if (isMenuOpen) {
                    val deltaX = x - touchX
                    val deltaY = y - touchY

                    dragX = deltaX
                    dragY = deltaY

                    val dragDist = sqrt(dragX * dragX + dragY * dragY)
                    if (dragDist > 30f) {
                        val newIndex = getSelectionFromDrag(dragX, dragY, centerAngle)

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
                    val dist = sqrt((x - startX) * (x - startX) + (y - startY) * (y - startY))
                    if (dist > touchSlop && !moved) {
                        moved = true
                        longPressRunnable?.let { handler.removeCallbacks(it) }
                    }
                }
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                longPressRunnable?.let { handler.removeCallbacks(it) }

                if (isMenuOpen) {
                    if (currentSelectionIndex != null) {
                        vibrate(30)
                        onItemSelected?.invoke(menuItems[currentSelectionIndex!!])
                    }
                    closeMenu()
                } else if (!moved && event.actionMasked == MotionEvent.ACTION_UP) {
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

    private fun handleLongPress(x: Float, y: Float) {
        if (!moved && menuItems.isNotEmpty()) {
            vibrate(50)
            val screenWidth = width.toFloat()
            val screenHeight = height.toFloat()

            centerAngle = calculateCenterAngle(x, y, screenWidth, screenHeight)
            isMenuOpen = true
            touchX = x
            touchY = y
            dragX = 0f
            dragY = 0f
            currentSelectionIndex = null

            resetAllScales()

            onMenuOpened?.invoke()
            post { invalidate() }
        }
    }

    private fun closeMenu() {
        isMenuOpen = false
        currentSelectionIndex = null
        dragX = 0f
        dragY = 0f
        resetAllScales()
        onMenuClosed?.invoke()
        post { invalidate() }
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

        // Draw scrim
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), overlayPaint)

        // Draw center indicator
        canvas.drawCircle(touchX, touchY, 16f, backgroundPaint)

        val itemCount = menuItems.size
        for (i in 0 until itemCount) {
            val item = menuItems[i]
            val itemAngle = centerAngle + ((i - (itemCount - 1) / 2f) * DEFAULT_SPREAD_DEGREES)
            val angleRad = Math.toRadians(itemAngle.toDouble())
            val iconCX = touchX + menuRadiusPx * cos(angleRad).toFloat()
            val iconCY = touchY + menuRadiusPx * sin(angleRad).toFloat()

            val isSelected = currentSelectionIndex == i
            val scale = if (i < itemScales.size) itemScales[i] else 1.0f
            val scaledSize = iconSizePx * scale
            val bgRadius = scaledSize * 0.75f

            bgCirclePaint.color = if (isSelected) accentColor else Color.parseColor("#424242")
            canvas.drawCircle(iconCX, iconCY, bgRadius, bgCirclePaint)

            val drawable = if (item.isActive && item.iconActive != null) item.iconActive else item.icon
            drawable.setBounds(
                (iconCX - scaledSize / 2).toInt(),
                (iconCY - scaledSize / 2).toInt(),
                (iconCX + scaledSize / 2).toInt(),
                (iconCY + scaledSize / 2).toInt()
            )
            drawable.colorFilter = if (isSelected) {
                PorterDuffColorFilter(Color.BLACK, PorterDuff.Mode.SRC_IN)
            } else {
                PorterDuffColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN)
            }
            drawable.draw(canvas)

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

        // Draw drag direction indicator
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
}
