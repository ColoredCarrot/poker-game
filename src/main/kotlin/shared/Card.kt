package shared

import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlin.random.Random

@Serializable
class Card private constructor(
    val suit: Suit,
    val typeName: String
) {

    @Transient
    val displayName = "$typeName of ${suit.displayName}"
    @Transient
    val image = "cards/${typeName}_of_${suit.filenameSuffix}.svg"

    companion object {

        val CLUBS = generateSet(Suit.CLUBS)
        val DIAMONDS = generateSet(Suit.DIAMONDS)
        val HEARTS = generateSet(Suit.HEARTS)
        val SPADES = generateSet(Suit.SPADES)

        val STANDARD_DECK: List<Card> = ArrayList<Card>(52).apply {
            this += CLUBS
            this += DIAMONDS
            this += HEARTS
            this += SPADES
        }

        fun get(suit: Suit) = when (suit) {
            Suit.CLUBS -> CLUBS
            Suit.DIAMONDS -> DIAMONDS
            Suit.HEARTS -> HEARTS
            Suit.SPADES -> SPADES
        }

        private fun generateSet(suit: Suit): List<Card> {
            val result = ArrayList<Card>(13)

            for (num in 2..10) {
                result += Card(suit, "$num")
            }

            result += Card(suit, "jack")
            result += Card(suit, "queen")
            result += Card(suit, "king")
            result += Card(suit, "ace")

            return result
        }

        fun drawCardSequence(random: Random = Random): Sequence<Card> {
            val shuffledDeck = STANDARD_DECK.shuffled(random)
            return shuffledDeck.asSequence()
        }

        fun drawCards(random: Random = Random) = DrawCards(drawCardSequence(random).iterator())

    }

    enum class Suit {
        CLUBS, DIAMONDS, HEARTS, SPADES;

        val filenameSuffix get() = name.toLowerCase()
        val displayName get() = name.toLowerCase()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Card) return false

        if (suit != other.suit) return false
        if (typeName != other.typeName) return false

        return true
    }

    override fun hashCode(): Int {
        var result = suit.hashCode()
        result = 31 * result + typeName.hashCode()
        return result
    }

}

class DrawCards(private val iter: Iterator<Card>) {

    fun take(): Card {
        if (!iter.hasNext()) error("Out of cards!")
        return iter.next()
    }

    fun take(n: Int): List<Card> = take(n, ArrayList())

    fun <C : MutableCollection<in Card>> take(n: Int, c: C): C {
        repeat(n) { c += take() }
        return c
    }

}
