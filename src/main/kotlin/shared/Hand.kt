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

@Serializable
class Hand(private val cards_: MutableList<ConcreteCard>) : Comparable<Hand> {

    val cards get() = cards_

    // TODO: Add some auto sorters (by value, by faction, etc.)

    fun reorderManually(newIndices: List<Int>) {
        if (newIndices.size != cards_.size) throw IllegalArgumentException()

        val new = Array(cards_.size) { i ->
            cards_[newIndices[i]]
        }

        cards_.clear()
        cards_.addAll(new)
    }

    fun evaluate(): Int = when (cards_.size) {
        5 -> {
            val nr = IntArray(5) { i ->
                val card = cards_[i].card
                when (card.typeName) {
                    "jack" -> RankPokerHandPublic.J
                    "queen" -> RankPokerHandPublic.Q
                    "king" -> RankPokerHandPublic.K
                    "ace" -> RankPokerHandPublic.A
                    else -> card.typeName.toInt()
                }
            }
            val suits = IntArray(5) { i ->
                val card = cards_[i].card
                when (card.faction) {
                    Card.Faction.SPADES -> 1
                    Card.Faction.CLUBS -> 2
                    Card.Faction.HEARTS -> 4
                    Card.Faction.DIAMONDS -> 8
                }
            }
            RankPokerHandPublic.rankPokerHand5(nr, suits)
        }

        7 -> {
            // TODO use faster algorithm from RankPokerHandPublic
            val nr = IntArray(7) { i ->
                val card = cards_[i].card
                when (card.typeName) {
                    "jack" -> RankPokerHandPublic.J
                    "queen" -> RankPokerHandPublic.Q
                    "king" -> RankPokerHandPublic.K
                    "ace" -> RankPokerHandPublic.A
                    else -> card.typeName.toInt()
                }
            }
            val suits = IntArray(7) { i ->
                val card = cards_[i].card
                when (card.faction) {
                    Card.Faction.SPADES -> 1
                    Card.Faction.CLUBS -> 2
                    Card.Faction.HEARTS -> 4
                    Card.Faction.DIAMONDS -> 8
                }
            }
            // Try all combinations and choose best one
            (0 until 7).toList().subsetsOfSize(5)
                .map { indices -> nr.sliceArray(indices) to suits.sliceArray(indices) }
                .map { (nrSub, suitsSub) -> RankPokerHandPublic.rankPokerHand5(nrSub, suitsSub) }
                .max()!!
        }

        else -> error("Evaluating hands with ${cards_.size} cards is not supported")
    }

    fun evaluateRank(): RankPokerHandPublic.Combination {
        return RankPokerHandPublic.Combination.fromRank(evaluate() shr 26)
    }

    override fun compareTo(other: Hand): Int {
        if (cards_.size != other.cards_.size) throw IllegalArgumentException("Cannot compare hands with different number of cards")
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
