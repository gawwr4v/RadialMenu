package io.github.gawwr4v.radialmenu

import android.content.Context
import android.graphics.drawable.Drawable
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat

/**
 * Creates a [RadialMenuItem] from Android drawable resource IDs.
 *
 * This overload is intended for Android View consumers who prefer working
 * with drawable resources instead of Compose painters.
 *
 * @param context Context used to resolve drawable resources.
 * @param id Unique identifier for this item.
 * @param iconRes Drawable resource ID for the default icon.
 * @param label Optional display label.
 * @param iconActiveRes Optional drawable resource ID for active state icon.
 * @param isActive Whether this item is currently active.
 * @param badgeCount Badge count value (0 hides badge).
 * @param badgeText Optional badge text override.
 * @param contentDescription Accessibility description.
 * @throws IllegalArgumentException If [iconRes] or [iconActiveRes] cannot be resolved.
 * @since 1.0.5
 */
fun RadialMenuItem(
    context: Context,
    id: Int,
    @DrawableRes iconRes: Int,
    label: String = "",
    @DrawableRes iconActiveRes: Int? = null,
    isActive: Boolean = false,
    badgeCount: Int = 0,
    badgeText: String? = null,
    contentDescription: String = label
): RadialMenuItem {
    val iconDrawable = requireDrawable(context, iconRes, "iconRes")
    val activeDrawable = iconActiveRes?.let { requireDrawable(context, it, "iconActiveRes") }
    return RadialMenuItem(
        id = id,
        icon = iconDrawable.toPainter(),
        iconActive = activeDrawable?.toPainter(),
        label = label,
        isActive = isActive,
        badgeCount = badgeCount,
        badgeText = badgeText,
        contentDescription = contentDescription
    )
}

/**
 * Creates a [RadialMenuItem] from Android [Drawable] instances.
 *
 * This overload is useful for View-based integration where icons are already
 * available as drawables.
 *
 * @param id Unique identifier for this item.
 * @param icon Drawable used for the default icon state.
 * @param label Optional display label.
 * @param iconActive Optional drawable used when [isActive] is true.
 * @param isActive Whether this item is currently active.
 * @param badgeCount Badge count value (0 hides badge).
 * @param badgeText Optional badge text override.
 * @param contentDescription Accessibility description.
 * @since 1.0.5
 */
fun RadialMenuItem(
    id: Int,
    icon: Drawable,
    label: String = "",
    iconActive: Drawable? = null,
    isActive: Boolean = false,
    badgeCount: Int = 0,
    badgeText: String? = null,
    contentDescription: String = label
): RadialMenuItem {
    return RadialMenuItem(
        id = id,
        icon = icon.toPainter(),
        iconActive = iconActive?.toPainter(),
        label = label,
        isActive = isActive,
        badgeCount = badgeCount,
        badgeText = badgeText,
        contentDescription = contentDescription
    )
}

private fun requireDrawable(
    context: Context,
    @DrawableRes drawableRes: Int,
    paramName: String
): Drawable {
    return ContextCompat.getDrawable(context, drawableRes)
        ?: throw IllegalArgumentException("Unable to resolve drawable for $paramName=$drawableRes")
}
