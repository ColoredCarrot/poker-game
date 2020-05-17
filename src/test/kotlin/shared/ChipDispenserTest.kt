package shared

import kotlin.test.Test
import kotlin.test.assertEquals

class ChipDispenserTest {

    @Test
    internal fun search() {
        val cd = ChipDispenser.DEFAULT
        assertEquals(1, cd.getNextSmallerChip(5))
    }

}
