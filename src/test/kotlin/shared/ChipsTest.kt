package shared

import kotlin.test.Test
import kotlin.test.assertEquals

class ChipsTest {

    @Test
    internal fun name() {
        val c = Chips(7)
        val d = c.calculateDistribution()
        assertEquals(listOf(1 to 2, 5 to 1), d)
    }

}
