package shared

import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlin.random.Random

@Serializable
class Card private constructor(
    val faction: Faction,
    val typeName: String
) {

    @Transient
    val displayName = "$typeName of ${faction.displayName}"
    @Transient
    val image = "cards/${typeName}_of_${faction.filenameSuffix}.svg"

    companion object {

        val CLUBS = generateSet(Faction.CLUBS)
        val DIAMONDS = generateSet(Faction.DIAMONDS)
        val HEARTS = generateSet(Faction.HEARTS)
        val SPADES = generateSet(Faction.SPADES)

        val STANDARD_DECK: List<Card> = ArrayList<Card>(52).apply {
            this += CLUBS
            this += DIAMONDS
            this += HEARTS
            this += SPADES
        }

        fun get(faction: Faction) = when (faction) {
            Faction.CLUBS -> CLUBS
            Faction.DIAMONDS -> DIAMONDS
            Faction.HEARTS -> HEARTS
            Faction.SPADES -> SPADES
        }

        private fun generateSet(faction: Faction): List<Card> {
            val result = ArrayList<Card>(13)

            for (num in 2..10) {
                result += Card(faction, "$num")
            }

            result += Card(faction, "jack")
            result += Card(faction, "queen")
            result += Card(faction, "king")
            result += Card(faction, "ace")

            return result
        }

        fun drawCardSequence(random: Random = Random): Sequence<Card> {
            val shuffledDeck = STANDARD_DECK.shuffled(random)
            return shuffledDeck.asSequence()
        }

        fun drawCards(random: Random = Random) = DrawCards(drawCardSequence(random).iterator())

    }

    enum class Faction {
        CLUBS, DIAMONDS, HEARTS, SPADES;

        val filenameSuffix get() = name.toLowerCase()
        val displayName get() = name.toLowerCase()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Card) return false

        if (faction != other.faction) return false
        if (typeName != other.typeName) return false

        return true
    }

    override fun hashCode(): Int {
        var result = faction.hashCode()
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
