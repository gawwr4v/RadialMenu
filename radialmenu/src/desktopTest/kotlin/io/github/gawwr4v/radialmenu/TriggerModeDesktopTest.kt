package io.github.gawwr4v.radialmenu

import kotlin.test.Test
import kotlin.test.assertEquals

class TriggerModeDesktopTest {

    @Test
    fun autoTrigger_desktop_resolvesToSecondaryClick() {
        assertEquals(
            RadialMenuTriggerMode.SecondaryClick(positionAware = false),
            resolveTriggerMode(RadialMenuTriggerMode.Auto)
        )
    }
}
