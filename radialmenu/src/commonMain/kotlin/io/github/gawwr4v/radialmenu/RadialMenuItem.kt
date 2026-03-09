package io.github.gawwr4v.radialmenu

import androidx.compose.ui.graphics.painter.Painter

/**
 * Represents a single item in the radial menu.
 *
 * Developers define their own items with arbitrary IDs and icons.
 * The library does **not** prescribe fixed actions.
 *
 * @param id Unique identifier for this item, developer-defined.
 * @param icon The default-state icon [Painter].
 * @param iconActive Optional active/toggled-state icon [Painter] (e.g. filled heart).
 * @param label Human-readable label (used for accessibility and future label rendering).
 * @param isActive Whether this item is currently in its "active" toggle state.
 * @param badgeCount Badge count to display. 0 means no badge.
 * @param badgeText Optional custom badge text (overrides [badgeCount] when non-null).
 * @param contentDescription Accessibility description for screen readers.
 *
 * **Note on equality:** `RadialMenuItem` is a data class, so `equals()`
 * and `hashCode()` are auto-generated. However, since [icon] and
 * [iconActive] are `Painter` instances (an interface), equality
 * comparison for these fields depends on the specific `Painter`
 * implementation. When using `copy()` or comparing items, be aware
 * that two painters pointing to the same resource may not be
 * considered equal.
 *
 * @since 1.0.0
 */
data class RadialMenuItem(
    val id: Int,
    val icon: Painter,
    val iconActive: Painter? = null,
    val label: String = "",
    val isActive: Boolean = false,
    val badgeCount: Int = 0,
    val badgeText: String? = null,
    val contentDescription: String = label
)
