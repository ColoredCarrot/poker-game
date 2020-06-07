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

import kotlinx.serialization.Serializable

// https://www.youtube.com/watch?v=CpSewSHZhmo
/**
 * Note: at the start of a round,
 * the first active player must either bet or check.
 * These actions are not represented by their own methods
 * but rather the existing [advanceByRaising] and [advanceByFolding];
 * calling [advanceByCalling] as the first action is undefined behaviour.
 */
@Serializable
data class Round(
    // never to be modified
    val roundTable: RoundTable,
    val originalFirstActivePlayer: RoundTable.Index,

    // only to be modified by [nextRound]
    var label: RoundLabel,
    var ante: Int,
    var firstActivePlayer: RoundTable.Index,

    // modified after every round action (fold/call/raise or check/bet)
    var activePlayer: RoundTable.Index,
    var amountToCall: Int,
    var lastPlayerWhoRaised: RoundTable.Index,
    val folded: MutableList<RoundTable.Index>
) {

    constructor(
        roundTable: RoundTable,
        label: RoundLabel,
        ante: Int,
        firstActivePlayer: RoundTable.Index
    ) : this(
        roundTable, firstActivePlayer, label, ante,
        firstActivePlayer,
        activePlayer = firstActivePlayer,
        amountToCall = 0,
        lastPlayerWhoRaised = firstActivePlayer,
        folded = ArrayList(roundTable.size - 1)
    )

    fun advanceByFolding() {
        folded += activePlayer
        advanceActivePlayer()
    }

    fun advanceByCalling() {
        advanceActivePlayer()
    }

    fun advanceByRaising(raise: Int) {
        val me = activePlayer
        advanceActivePlayer()
        lastPlayerWhoRaised = me
        amountToCall += raise
    }

    private fun advanceActivePlayer() {
        activePlayer = activePlayer.next()
        if (activePlayer == lastPlayerWhoRaised) {
            amountToCall = 0
        } else while (activePlayer in folded) {
            activePlayer = activePlayer.next()
        }
    }

    /**
     * Must be called after each invocation of one of the `advanceXXX`-methods
     * to check whether this round is finished.
     *
     * Once this method has returned `true`,
     * the behaviour of all future invocations of **any** methods
     * on this round is **undefined** unless otherwise specified.
     */
    fun isFinished(): Boolean {
        if (folded.size >= roundTable.size - 1) return true
        if (activePlayer == lastPlayerWhoRaised) return true
        return false
    }

    /**
     * Must be called when [isFinished] returns `true`.
     *
     * @throws IllegalStateException if the method is called without arguments and this is the last round
     */
    fun nextRound(newLabel: RoundLabel = label.next() ?: throw IllegalStateException("No more rounds!")) {
        label = newLabel
        firstActivePlayer = firstActivePlayer.next()
        resetRound()
    }

    /**
     * Resets this round's progress.
     */
    fun resetRound() {
        activePlayer = firstActivePlayer
        lastPlayerWhoRaised = firstActivePlayer
        folded.clear()
    }

    /**
     * Resets this object completely,
     * making it usable for a new game.
     */
    fun reset(newAnte: Int = ante) {
        label = RoundLabel.PREFLOP
        ante = newAnte
        firstActivePlayer = originalFirstActivePlayer
        resetRound()
    }

}

enum class RoundLabel {
    PREFLOP,
    FLOP,
    TURN,
    RIVER;

    fun next() = values().getOrNull(ordinal + 1)

    override fun toString() = name.toLowerCase().capitalize()
}

@Serializable
class RoundTable(val playersInOrder: List<SessionId>) {

    val size get() = playersInOrder.size

    operator fun get(index: Index) = playersInOrder[index.index]

    fun next(from: Index): Index {
        return if (from.index < playersInOrder.lastIndex) Index(from.index + 1)
        else Index(0)
    }

    fun index(index: Int) = Index(index)

    fun indexWrap(index: Int) = index(index % playersInOrder.size)

    fun getIndex(player: SessionId) = index(playersInOrder.indexOf(player))

    @Serializable
    inner class Index(val index: Int) : Comparable<Index> {
        init {
            if (index < 0 || index > playersInOrder.lastIndex) {
                throw IndexOutOfBoundsException("index $index out of bounds for round table with ${playersInOrder.size} players")
            }
        }

        fun next() = next(this)

        fun get() = playersInOrder[index]

        override fun compareTo(other: Index) = index.compareTo(other.index)

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is Index) return false
            return index == other.index
        }

        override fun hashCode(): Int {
            return index
        }

        override fun toString(): String {
            return "$index(${get()})"
        }

    }

    override fun toString(): String {
        return playersInOrder.toString()
    }

}
