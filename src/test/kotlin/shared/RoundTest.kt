/*
 * Copyright 2020 Julian Koch and contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package shared

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class RoundTest {

    @Test
    internal fun main() {

        val table = RoundTable(listOf("a", "fold", "b", "c"))
        val round = Round(table, RoundLabel.PREFLOP, 1, table.index(0))

        assertEquals(0, round.amountToCall)

        // a bets 10
        round.advanceByRaising(10)
        assertEquals("fold", round.activePlayer.get())
        assertEquals(10, round.amountToCall)

        // fold folds
        assertFalse(round.isFinished())
        round.advanceByFolding()
        assertEquals("b", round.activePlayer.get())
        assertEquals(10, round.amountToCall)
        assertTrue(round.roundTable.getIndex("fold") in round.folded)

        // b calls
        assertFalse(round.isFinished())
        round.advanceByCalling()
        assertEquals("c", round.activePlayer.get())

        // c calls
        assertFalse(round.isFinished())
        round.advanceByCalling()
        assertEquals("a", round.activePlayer.get())

        assertTrue(round.isFinished())


        // Action           Pot after action (minus collective ante)
        // a bets 10        10
        // fold folds       10
        // b calls          20
        // c raises 5       35
        // a calls          40
        // b folds          40
        round.reset()
        assertEquals(0, round.amountToCall)

        // a bets 10
        round.advanceByRaising(10)

        // fold folds
        assertFalse(round.isFinished())
        round.advanceByFolding()
        assertEquals("b", round.activePlayer.get())
        assertEquals(10, round.amountToCall)

        // b calls
        assertFalse(round.isFinished())
        round.advanceByCalling()
        assertEquals("c", round.activePlayer.get())

        // c raises 5
        assertFalse(round.isFinished())
        round.advanceByRaising(5)
        assertEquals("a", round.activePlayer.get())

        // a calls
        assertFalse(round.isFinished())
        assertEquals(5, round.amountToCall)
        round.advanceByCalling()
        assertEquals("b", round.activePlayer.get())

        // b folds
        assertFalse(round.isFinished())
        round.advanceByFolding()
        assertEquals("c", round.activePlayer.get())

        assertTrue(round.isFinished())

    }

}
