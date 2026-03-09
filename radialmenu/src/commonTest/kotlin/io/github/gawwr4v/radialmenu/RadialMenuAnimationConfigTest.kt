package io.github.gawwr4v.radialmenu

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class RadialMenuAnimationConfigTest {

    @Test
    fun defaultPreset_hasCorrectValues() {
        val config = RadialMenuAnimationConfig.default()
        assertEquals(300, config.openDurationMs)
        assertEquals(200, config.closeDurationMs)
        assertEquals(100, config.itemScaleDurationMs)
        assertEquals(1.4f, config.selectedItemScale)
        assertEquals(false, config.enableSpringAnimation)
    }

    @Test
    fun snappyPreset_hasShorterDuration() {
        val snappy = RadialMenuAnimationConfig.snappy()
        val default = RadialMenuAnimationConfig.default()
        assertTrue(snappy.openDurationMs < default.openDurationMs,
            "snappy should be shorter than default")
    }

    @Test
    fun slowPreset_hasLongerDuration() {
        val slow = RadialMenuAnimationConfig.slow()
        val default = RadialMenuAnimationConfig.default()
        assertTrue(slow.openDurationMs > default.openDurationMs,
            "slow should be longer than default")
    }

    @Test
    fun bouncyPreset_hasSpringEnabled() {
        val bouncy = RadialMenuAnimationConfig.bouncy()
        assertTrue(bouncy.enableSpringAnimation, "bouncy should have spring enabled")
    }

    @Test
    fun openDurationMs_zeroOrNegative_throws() {
        assertFailsWith<IllegalArgumentException> {
            RadialMenuAnimationConfig(openDurationMs = 0)
        }
        assertFailsWith<IllegalArgumentException> {
            RadialMenuAnimationConfig(openDurationMs = -1)
        }
    }

    @Test
    fun selectedItemScale_zeroOrNegative_throws() {
        assertFailsWith<IllegalArgumentException> {
            RadialMenuAnimationConfig(selectedItemScale = 0f)
        }
        assertFailsWith<IllegalArgumentException> {
            RadialMenuAnimationConfig(selectedItemScale = -1f)
        }
    }

    @Test
    fun springDampingRatio_outsideRange_throws() {
        assertFailsWith<IllegalArgumentException> {
            RadialMenuAnimationConfig(springDampingRatio = -0.1f)
        }
        assertFailsWith<IllegalArgumentException> {
            RadialMenuAnimationConfig(springDampingRatio = 1.1f)
        }
    }

    @Test
    fun allPresetsInstantiateWithoutThrowing() {
        RadialMenuAnimationConfig.default()
        RadialMenuAnimationConfig.snappy()
        RadialMenuAnimationConfig.bouncy()
        RadialMenuAnimationConfig.slow()
        assertTrue(true, "All presets should instantiate")
    }
}
