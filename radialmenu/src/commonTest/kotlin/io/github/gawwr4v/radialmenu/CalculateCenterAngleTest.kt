package io.github.gawwr4v.radialmenu

import kotlin.test.Test
import kotlin.test.assertTrue

class CalculateCenterAngleTest {

    private val screenWidth = 1080f
    private val screenHeight = 1920f

    @Test
    fun centerOfScreen_pointsUp() {
        val angle = RadialMenuMath.calculateCenterAngle(540f, 960f, screenWidth, screenHeight)
        assertTrue(angle in 250f..290f, "Center of screen should point ~270° (up), got $angle")
    }

    @Test
    fun leftEdge_tiltsRight() {
        val angle = RadialMenuMath.calculateCenterAngle(50f, 960f, screenWidth, screenHeight)
        assertTrue(angle > 290f, "Left edge should tilt right (>290°), got $angle")
    }

    @Test
    fun rightEdge_tiltsLeft() {
        val angle = RadialMenuMath.calculateCenterAngle(1030f, 960f, screenWidth, screenHeight)
        assertTrue(angle < 250f, "Right edge should tilt left (<250°), got $angle")
    }

    @Test
    fun bottomCenter_pointsUp() {
        val angle = RadialMenuMath.calculateCenterAngle(540f, 1800f, screenWidth, screenHeight)
        assertTrue(angle in 250f..290f, "Bottom center should point ~270° (up), got $angle")
    }

    @Test
    fun topCenter_pointsDown() {
        val angle = RadialMenuMath.calculateCenterAngle(540f, 100f, screenWidth, screenHeight)
        assertTrue(angle in 60f..120f, "Top center should point ~90° (down), got $angle")
    }

    @Test
    fun topLeftCorner_pointsDownRight() {
        val angle = RadialMenuMath.calculateCenterAngle(50f, 50f, screenWidth, screenHeight)
        assertTrue(angle in 15f..60f, "Top-left corner should point down-right, got $angle")
    }

    @Test
    fun topRightCorner_pointsDownLeft() {
        val angle = RadialMenuMath.calculateCenterAngle(1030f, 50f, screenWidth, screenHeight)
        assertTrue(angle in 120f..165f, "Top-right corner should point down-left, got $angle")
    }

    @Test
    fun result_alwaysInSafeRange() {
        val testPoints = listOf(
            0f to 0f, 540f to 0f, 1080f to 0f,
            0f to 960f, 1080f to 960f,
            0f to 1920f, 540f to 1920f, 1080f to 1920f
        )
        for ((x, y) in testPoints) {
            val angle = RadialMenuMath.calculateCenterAngle(x, y, screenWidth, screenHeight)
            val inUpperRange = angle in 195f..345f
            val inLowerRange = angle in 15f..165f
            assertTrue(inUpperRange || inLowerRange,
                "Angle at ($x,$y) = $angle must be in 195..345 or 15..165")
        }
    }

    // RTL tests
    @Test
    fun rtl_centerOfScreen_sameAsLtr() {
        val ltrAngle = RadialMenuMath.calculateCenterAngle(540f, 960f, screenWidth, screenHeight, isRtl = false)
        val rtlAngle = RadialMenuMath.calculateCenterAngle(540f, 960f, screenWidth, screenHeight, isRtl = true)
        assertTrue(kotlin.math.abs(ltrAngle - rtlAngle) < 5f,
            "RTL center should mirror LTR center, LTR=$ltrAngle RTL=$rtlAngle")
    }

    @Test
    fun rtl_leftEdge_behavesLikeLtrRightEdge() {
        val ltrRight = RadialMenuMath.calculateCenterAngle(1030f, 960f, screenWidth, screenHeight, isRtl = false)
        val rtlLeft = RadialMenuMath.calculateCenterAngle(50f, 960f, screenWidth, screenHeight, isRtl = true)
        assertTrue(kotlin.math.abs(ltrRight - rtlLeft) < 15f,
            "RTL left edge should behave like LTR right edge, LTR-right=$ltrRight RTL-left=$rtlLeft")
    }

    @Test
    fun rtl_rightEdge_behavesLikeLtrLeftEdge() {
        val ltrLeft = RadialMenuMath.calculateCenterAngle(50f, 960f, screenWidth, screenHeight, isRtl = false)
        val rtlRight = RadialMenuMath.calculateCenterAngle(1030f, 960f, screenWidth, screenHeight, isRtl = true)
        assertTrue(kotlin.math.abs(ltrLeft - rtlRight) < 15f,
            "RTL right edge should behave like LTR left edge, LTR-left=$ltrLeft RTL-right=$rtlRight")
    }
}
