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

package usingreact

import audio.SoundEffectGroups
import kotlinx.html.classes
import org.w3c.dom.HTMLElement
import react.RBuilder
import react.RProps
import react.RPureComponent
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

private class HandDisplay : RPureComponent<HandDisplayProps, RState>() {

    private lateinit var directParentOfCardsRef: RReadableRef<HTMLElement>

    override fun RState.init() {
        directParentOfCardsRef = vendor.createRef()
    }

    override fun RBuilder.render() {
        div("uk-grid-small uk-child-width-auto uk-flex-center uk-flex-nowrap") {
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

        //TODO this appears to diminish image resolution or quality or something... what is going on??
        /*//FIXME why tf does this need to be in a timeout
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
        }, 300)*/

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
