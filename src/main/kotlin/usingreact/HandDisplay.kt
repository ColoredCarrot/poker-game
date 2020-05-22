package usingreact

import audio.SoundEffectGroups
import kotlinx.html.classes
import org.w3c.dom.HTMLElement
import react.RBuilder
import react.RComponent
import react.RProps
import react.RReadableRef
import react.RState
import react.child
import react.dom.div
import react.dom.img
import reactutils.functionalComponentEx
import shared.ConcreteCard
import shared.Hand
import shared.UIkit
import shared.attrsApplyStyle
import kotlin.browser.window

fun RBuilder.handDisplay(
    hand: Hand,
    renderCommunityCards: Boolean,
    requestHandReorderFn: (newOrder: List<Int>) -> Unit
) = child(HandDisplay::class) {
    attrs {
        this.hand = hand
        this.renderCommunityCards = renderCommunityCards
        this.requestHandReorderFn = requestHandReorderFn
    }
}

external interface HandDisplayProps : RProps {
    var hand: Hand
    var renderCommunityCards: Boolean
    var requestHandReorderFn: (newOrder: List<Int>) -> Unit
}

private class HandDisplay : RComponent<HandDisplayProps, RState>() {

    private lateinit var directParentOfCardsRef: RReadableRef<HTMLElement>

    override fun RState.init() {
        directParentOfCardsRef = vendor.createRef()
    }

    override fun RBuilder.render() {
        div("uk-grid-small uk-child-width-expand") {
            ref = directParentOfCardsRef
            attrs {
                attributes["uk-grid"] = ""
                attributes["uk-sortable"] = ""
            }

            for ((index, concreteCard) in props.hand.cards.withIndex()) {
                if (!props.renderCommunityCards && concreteCard.isCommunityCard) continue

                div {
                    attrsApplyStyle { minWidth = "4vw" }

                    cardDisplay(concreteCard, index)
                }
            }
        }
    }

    private val directParentOfCards get() = directParentOfCardsRef.current!!

    override fun componentDidMount() {
        // Match height of all cards to actual height of first card (or width, doesn't really matter, right?)
        val divWithDivsWithImgs = directParentOfCards

        //FIXME why tf does this need to be in a timeout
        window.setTimeout({
            val heightOfFirst = divWithDivsWithImgs.firstElementChild?.clientHeight
            if (heightOfFirst != null) {
                var sibling = divWithDivsWithImgs.firstElementChild!!.nextElementSibling
                while (sibling != null) {
                    val siblingImg = sibling.firstElementChild!!
                    siblingImg.setAttribute(
                        "style",
                        "${siblingImg.getAttribute("style") ?: ""}; height: ${heightOfFirst}px;"
                    )
                    sibling = sibling.nextElementSibling
                }
            }
        }, 300)

        UIkit.util.on(divWithDivsWithImgs, "moved") {
            println("Card moved")

            // Play sound effect
            SoundEffectGroups.PLACE_CARD.playRandom()

            // The hand has been manually re-sorted
            val firstCardElement = directParentOfCards.firstElementChild
            val iter = generateSequence(firstCardElement) { it.nextElementSibling }
            val cardElements = iter.map { it.firstElementChild!! }.toList()

            val newOrder = cardElements.map { it.getAttribute(ATTR_DATA_POKER_IDX)!!.toInt() }
            props.requestHandReorderFn(newOrder)

            Unit
        }
    }

    override fun componentWillUnmount() {
        //TODO unhook UIkit moved event
    }
}


private fun RBuilder.cardDisplay(card: ConcreteCard, index: Int) = child(CardDisplay) {
    attrs {
        this.concreteCard = card
        this.index = index
    }
}

private external interface CardDisplayProps : RProps {
    var concreteCard: ConcreteCard
    var index: Int
}

private val CardDisplay = functionalComponentEx<CardDisplayProps>("CardDisplay") { props ->
    val card = props.concreteCard.card
    img(alt = card.displayName, classes = "poker-hand") {
        attrs {
            attributes["uk-img"] = ""
            attributes["data-src"] = card.image
            attributes[ATTR_DATA_POKER_IDX] = "${props.index}"
            classes += if (props.concreteCard.isCommunityCard) "poker-community-card" else "poker-non-community-card"
        }
    }
}


private const val ATTR_DATA_POKER_IDX = "data-poker-idx"
