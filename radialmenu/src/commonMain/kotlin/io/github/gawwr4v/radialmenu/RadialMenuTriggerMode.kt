package io.github.gawwr4v.radialmenu

import androidx.compose.ui.input.key.Key

/**
 * Controls how the radial menu is triggered.
 *
 * Use [Auto] (default) to automatically select the appropriate trigger
 * per platform: [LongPress] on Android and [SecondaryClick] on Desktop.
 *
 * @since 1.0.4
 */
sealed class RadialMenuTriggerMode {

    /**
     * Platform default trigger mode.
     *
     * Resolves to [LongPress] on Android and [SecondaryClick] on Desktop.
     */
    object Auto : RadialMenuTriggerMode()

    /**
     * Long press to open.
     *
     * This is the default mobile trigger and preserves the existing
     * hold-drag-release interaction.
     *
     * @param positionAware If true, the menu fan uses position-aware angle
     * logic to avoid finger occlusion and clipping on touch surfaces.
     */
    data class LongPress(val positionAware: Boolean = true) : RadialMenuTriggerMode()

    /**
     * Secondary click (right click) to open.
     *
     * Recommended for desktop workflows. The menu spawns at cursor position,
     * so edge-hug remains applicable.
     *
     * @param positionAware If true, applies position-aware fan rotation.
     * Defaults to false for desktop precision-pointer UX.
     */
    data class SecondaryClick(val positionAware: Boolean = false) : RadialMenuTriggerMode()

    /**
     * Hold a keyboard key to open.
     *
     * The menu spawns at screen center. Edge-hug is automatically skipped in
     * this mode because center-spawned menus are not constrained by corners.
     *
     * @param key The key to hold. Defaults to [Key.Q].
     */
    data class KeyboardHold(val key: Key = Key.Q) : RadialMenuTriggerMode()
}

/**
 * Platform default trigger mode used when [RadialMenuTriggerMode.Auto] is selected.
 *
 * Android: [RadialMenuTriggerMode.LongPress]
 * Desktop: [RadialMenuTriggerMode.SecondaryClick]
 *
 * @since 1.0.4
 */
expect val defaultTriggerMode: RadialMenuTriggerMode
