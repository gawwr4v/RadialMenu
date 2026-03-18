package io.github.gawwr4v.radialmenu

import kotlin.test.Test
import kotlin.test.assertEquals

class TriggerModeAndroidTest {

    @Test
    fun autoTrigger_android_resolvesToLongPress() {
        assertEquals(
            RadialMenuTriggerMode.LongPress(positionAware = true),
            resolveTriggerMode(RadialMenuTriggerMode.Auto)
        )
    }
}
