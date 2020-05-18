package render

import kotlinx.html.dom.append
import kotlinx.html.id
import kotlinx.html.js.div
import org.w3c.dom.HTMLElement
import org.w3c.dom.events.MouseEvent
import react.HandDisplayProps
import react.ReactElement
import react.handDisplay
import shared.PrivateGameState
import shared.RenderContext
import shared.UIkit
import shared.render
import kotlin.browser.document
import kotlin.dom.clear

class MyselfComponent(private val myself: PrivateGameState) : Component() {

    override fun RenderContext.addStaticToDOM() {
        div("uk-position-bottom-center poker-my-hand-container") {
            div {
                id = ID_MY_MONEY_CONTAINER
                myself.playerInfo.money.render(this@addStaticToDOM, sizeMod = MY_MONEY_RENDER_SIZE_MOD)
            }
            div {
                id = ID_MY_HAND_CONTAINER
                /*myself.playerInfo.hand!!.render(this@addStaticToDOM)
                    .ondblclick = { onHandDblClick(it) }*/

                react.dom.render(div()) {
                    lateinit var handDisplay: ReactElement
                    handDisplay = handDisplay(myself.playerInfo.hand!!, true) { newOrder ->
                        myself.playerInfo.hand!!.reorderManually(newOrder)
                        @Suppress("UNCHECKED_CAST_TO_EXTERNAL_INTERFACE")
                        (handDisplay.props as HandDisplayProps).hand = myself.playerInfo.hand!!
                    }
                }
            }
        }
    }

    override fun update() {
        (document.getElementById(ID_MY_MONEY_CONTAINER) as HTMLElement).also {
            it.clear()
            myself.playerInfo.money.render(it.append, sizeMod = MY_MONEY_RENDER_SIZE_MOD)
        }
        (document.getElementById(ID_MY_HAND_CONTAINER) as HTMLElement).also {
            it.clear()
            myself.playerInfo.hand!!.render(it.append)
                .ondblclick = { onHandDblClick(it) }
        }
    }

    private fun onHandDblClick(evt: MouseEvent) {
        evt.preventDefault()
        // On double click, display help to explain the hand
        //language=HTML
        UIkit.modal.dialog(
            """<p class='uk-modal-body'>
                    Your hand is a: <span class='uk-text-bold uk-text-capitalize'>${myself.playerInfo.hand!!.evaluateRank().displayName}</span>
                    </p>""".trimIndent()
        )
    }

    companion object {
        private const val ID_MY_MONEY_CONTAINER = "my-money-container"
        private const val ID_MY_HAND_CONTAINER = "my-hand-container"

        private const val MY_MONEY_RENDER_SIZE_MOD = 0.7
    }
}
