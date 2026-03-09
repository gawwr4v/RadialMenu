package io.github.gawwr4v.radialmenu

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/**
 * Configuration for colors used in the radial menu.
 *
 * Use the companion factory functions [dark], [light], or [autoTheme]
 * for convenient presets, or construct a fully custom configuration.
 *
 * @property itemBackground The normal background color of an item circle.
 * @property itemBackgroundSelected The background color of an item circle when selected.
 * @property iconTint The tint color of the normal icon.
 * @property iconTintSelected The tint color of the icon when its item is selected.
 * @property overlayColor The color of the background dimming scrim.
 * @property centerIndicatorColor The color of the center dot indicator.
 * @property badgeColor The background color of item badges.
 * @property badgeTextColor The text color inside badges.
 * @since 1.0.0
 */
data class RadialMenuColors(
    val itemBackground: Color,
    val itemBackgroundSelected: Color,
    val iconTint: Color,
    val iconTintSelected: Color,
    val overlayColor: Color,
    val centerIndicatorColor: Color,
    val badgeColor: Color,
    val badgeTextColor: Color
) {
    companion object {
        /**
         * Returns the default color configuration (same as [dark]).
         */
        fun default() = dark()

        /**
         * Colors optimized for dark backgrounds (default).
         * Items appear as dark circles with white icons.
         */
        fun dark() = RadialMenuColors(
            itemBackground = Color(0xFF424242),
            itemBackgroundSelected = Color.White,
            iconTint = Color.White,
            iconTintSelected = Color.Black,
            overlayColor = Color.Black.copy(alpha = 0.5f),
            centerIndicatorColor = Color.White.copy(alpha = 0.3f),
            badgeColor = Color(0xFFFF4444),
            badgeTextColor = Color.White
        )

        /**
         * Colors optimized for light backgrounds.
         * Items appear as light circles with dark icons.
         */
        fun light() = RadialMenuColors(
            itemBackground = Color(0xFFE0E0E0),
            itemBackgroundSelected = Color(0xFF212121),
            iconTint = Color(0xFF212121),
            iconTintSelected = Color.White,
            overlayColor = Color.Black.copy(alpha = 0.35f),
            centerIndicatorColor = Color.Black.copy(alpha = 0.2f),
            badgeColor = Color(0xFFFF4444),
            badgeTextColor = Color.White
        )

        /**
         * Automatically selects [dark] or [light] based on
         * the current system theme setting.
         * Use this for automatic theme-aware coloring.
         */
        @Composable
        fun autoTheme(): RadialMenuColors {
            return if (isSystemInDarkTheme()) dark() else light()
        }
    }
}
