package io.github.gawwr4v.radialmenu

import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class RadialMenuColorsTest {

    @Test
    fun defaultColors_areNotNull() {
        val colors = RadialMenuColors.default()
        assertNotNull(colors.itemBackground)
        assertNotNull(colors.itemBackgroundSelected)
        assertNotNull(colors.iconTint)
        assertNotNull(colors.iconTintSelected)
        assertNotNull(colors.overlayColor)
        assertNotNull(colors.centerIndicatorColor)
        assertNotNull(colors.badgeColor)
        assertNotNull(colors.badgeTextColor)
    }

    @Test
    fun badgeColor_isNotTransparent() {
        val colors = RadialMenuColors.default()
        assertTrue(colors.badgeColor.alpha > 0f, "Badge color should not be transparent")
    }

    @Test
    fun allColorFields_areAccessible() {
        val colors = RadialMenuColors.default()
        // Just accessing all props to ensure no crash
        colors.itemBackground
        colors.itemBackgroundSelected
        colors.iconTint
        colors.iconTintSelected
        colors.overlayColor
        colors.centerIndicatorColor
        colors.badgeColor
        colors.badgeTextColor
        assertTrue(true, "All color fields should be accessible")
    }
}
