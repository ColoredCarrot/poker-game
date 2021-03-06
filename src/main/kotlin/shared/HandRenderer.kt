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
/*

import audio.SoundEffectGroups
import kotlinx.html.TagConsumer
import kotlinx.html.classes
import kotlinx.html.id
import kotlinx.html.js.div
import kotlinx.html.js.img
import kotlinx.html.style
import org.w3c.dom.HTMLElement
import kotlin.browser.document
import kotlin.browser.window
import kotlin.random.Random

fun Hand.render(ctx: TagConsumer<HTMLElement>, skipCommunityCards: Boolean = false) = with(ctx) {
    val idDirectParentOfCards = Random.nextHtmlId("direct-parent-of-cards-")
    div("uk-grid-small uk-child-width-expand") {
        id = idDirectParentOfCards
        attributes["uk-grid"] = ""
        attributes["uk-sortable"] = ""

        for ((index, concreteCard) in cards.withIndex()) {
            if (skipCommunityCards && concreteCard.isCommunityCard) continue

            val card = concreteCard.card
            div {
                style = "min-width: 4vw"

                img(alt = card.displayName) {
                    classes += "poker-hand"
                    attributes["uk-img"] = ""
                    attributes["data-src"] = card.image
                    attributes[ATTR_DATA_POKER_IDX] = "$index"
                    classes += if (concreteCard.isCommunityCard) "poker-community-card" else "poker-non-community-card"
                }
            }
        }

        // TODO: delaying this is an ugly solution; figure this out. For now though, this works okay. Maybe onload of root?
        window.setTimeout({
            UIkit.util.on("#${idDirectParentOfCards}", "moved") {
                println("Card moved")

                // Play sound effect
                SoundEffectGroups.PLACE_CARD.playRandom()

                // The hand has been manually re-sorted
                val firstCardElement = document.getElementById(idDirectParentOfCards)!!.firstElementChild
                val iter = generateSequence(firstCardElement) { it.nextElementSibling }
                val cardElements = iter.map { it.firstElementChild!! }.toList()

                val newOrder = cardElements.map { it.getAttribute(ATTR_DATA_POKER_IDX)!!.toInt() }
                MUTreorderManually(newOrder)

                // Update data-poker-idx
                for ((index, cardElement) in cardElements.withIndex()) {
                    cardElement.setAttribute(ATTR_DATA_POKER_IDX, "$index")
                }

                Unit
            }

            // Match height of all cards to actual height of first card (or width, doesn't really matter, right?)
            UIkitUpdate()
            val divWithDivsWithImgs = document.getElementById(idDirectParentOfCards) ?: error("divWithDivsWithImgs null") // TODO: sometimes null
            val heightOfFirst = divWithDivsWithImgs.firstElementChild?.clientHeight
            if (heightOfFirst != null) {
                var sibling = divWithDivsWithImgs.firstElementChild!!.nextElementSibling
                while (sibling != null) {
                    val siblingImg = sibling.firstElementChild!!
                    siblingImg.setAttribute("style", "${siblingImg.getAttribute("style") ?: ""}; height: ${heightOfFirst}px;")
                    sibling = sibling.nextElementSibling
                }
            }

            Unit
        }, 1)
    }
}

private const val ATTR_DATA_POKER_IDX = "data-poker-idx"
*/
