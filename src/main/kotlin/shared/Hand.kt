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

import eval.RankPokerHandPublic
import kotlinx.serialization.Serializable
import kotlin.random.Random

@Serializable
class ConcreteCard(val card: Card, val isCommunityCard: Boolean) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ConcreteCard) return false
        return card == other.card
    }

    override fun hashCode(): Int {
        return card.hashCode()
    }
}

//TODO make immutable
@Serializable
class Hand(val cards: List<ConcreteCard>) : Comparable<Hand> {

    // TODO: Add some auto sorters (by value, by faction, etc.)
    fun reorderManually(newIndices: List<Int>): Hand {
        if (newIndices.size != cards.size) throw IllegalArgumentException()

        val new = Array(cards.size) { i ->
            cards[newIndices[i]]
        }

        return Hand(new.asList())
    }

    fun evaluate(): Int = when (cards.size) {
        5 -> {
            val nr = IntArray(5) { i ->
                val card = cards[i].card
                when (card.typeName) {
                    "jack" -> RankPokerHandPublic.J
                    "queen" -> RankPokerHandPublic.Q
                    "king" -> RankPokerHandPublic.K
                    "ace" -> RankPokerHandPublic.A
                    else -> card.typeName.toInt()
                }
            }
            val suits = IntArray(5) { i ->
                val card = cards[i].card
                when (card.suit) {
                    Card.Suit.SPADES -> 1
                    Card.Suit.CLUBS -> 2
                    Card.Suit.HEARTS -> 4
                    Card.Suit.DIAMONDS -> 8
                }
            }
            RankPokerHandPublic.rankPokerHand5(nr, suits)
        }

        7 -> {
            // TODO use faster algorithm from RankPokerHandPublic
            val nr = IntArray(7) { i ->
                val card = cards[i].card
                when (card.typeName) {
                    "jack" -> RankPokerHandPublic.J
                    "queen" -> RankPokerHandPublic.Q
                    "king" -> RankPokerHandPublic.K
                    "ace" -> RankPokerHandPublic.A
                    else -> card.typeName.toInt()
                }
            }
            val suits = IntArray(7) { i ->
                val card = cards[i].card
                when (card.suit) {
                    Card.Suit.SPADES -> 1
                    Card.Suit.CLUBS -> 2
                    Card.Suit.HEARTS -> 4
                    Card.Suit.DIAMONDS -> 8
                }
            }
            // Try all combinations and choose best one
            (0 until 7).toList().subsetsOfSize(5)
                .map { indices -> nr.sliceArray(indices) to suits.sliceArray(indices) }
                .map { (nrSub, suitsSub) -> RankPokerHandPublic.rankPokerHand5(nrSub, suitsSub) }
                .max()!!
        }

        else -> error("Evaluating hands with ${cards.size} cards is not supported")
    }

    fun evaluateRank(): RankPokerHandPublic.Combination {
        return RankPokerHandPublic.Combination.fromRank(evaluate() shr 26)
    }

    override fun compareTo(other: Hand): Int {
        if (cards.size != other.cards.size) throw IllegalArgumentException("Cannot compare hands with different number of cards")
        return evaluate().compareTo(other.evaluate())
    }

    companion object {
        @Deprecated("useless")
        fun generateHands(num: Int, handSize: Int, random: Random = Random): List<Hand> {
            if (num < 1) throw IllegalArgumentException("players should be at least 1 but is $num")
            if (handSize < 1) throw IllegalArgumentException("handSize should be at least 1 but is $handSize")

            return Card.drawCardSequence(random)
                .map { ConcreteCard(it, false) }
                .chunked(handSize) { Hand(it.toMutableList()) }
                .take(num)
                .toList()

            /*val shuffledDeck = Card.STANDARD_DECK.shuffled(random)

            val hands = ArrayList<Hand>(num)
            repeat(num) { i ->
                hands += Hand(shuffledDeck.subList(i * handSize, (i + 1) * handSize).toMutableList())
            }
            return hands*/
        }
    }

}

fun DrawCards.takeHand(size: Int) = Hand(
    take(size)
        .mapTo(ArrayList(size)) { ConcreteCard(it, false) }
)

@Deprecated("not the way we went")
fun DrawCards.takeHand(individualCards: Int, communityCards: List<Card>): Hand {
    val hand = ArrayList<ConcreteCard>()
    take(individualCards).mapTo(hand) { ConcreteCard(it, false) }
    communityCards.mapTo(hand) { ConcreteCard(it, true) }
    return Hand(hand)
}

private tailrec fun powerset(a: List<Int>, acc: List<List<Int>>) {
    if (a.isNotEmpty()) {
        powerset(a.drop(1), acc + acc.map { it + a.first() })
    }
}

private fun List<Int>.subsetsOfSize(k: Int) = subsetsOfSize(this, k)

private fun subsetsOfSize(set: List<Int>, k: Int): List<List<Int>> {
    if (set.size < k) return emptyList()
    if (set.size == k) return listOf(set)
    if (set.isEmpty()) return emptyList()
    val x = set.first()
    val setWithoutX = set.drop(1)
    return subsetsOfSize(setWithoutX, k) + subsetsOfSize(setWithoutX, k - 1).map { it + x }
}


operator fun Hand.plus(moreCards: Iterable<ConcreteCard>) = Hand(cards + moreCards)
