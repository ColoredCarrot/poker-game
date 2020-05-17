package render

import audio.SoundEffects
import kotlinx.html.ButtonType
import kotlinx.html.InputType
import kotlinx.html.dom.append
import kotlinx.html.id
import kotlinx.html.js.button
import kotlinx.html.js.div
import kotlinx.html.js.form
import kotlinx.html.js.h2
import kotlinx.html.js.h3
import kotlinx.html.js.h4
import kotlinx.html.js.input
import kotlinx.html.js.output
import kotlinx.html.js.p
import org.w3c.dom.HTMLDivElement
import org.w3c.dom.HTMLElement
import org.w3c.dom.HTMLHeadingElement
import org.w3c.dom.HTMLInputElement
import org.w3c.dom.HTMLOutputElement
import org.w3c.dom.get
import shared.Chips
import shared.Notify
import shared.RenderContext
import shared.UIkit
import shared.elementById
import kotlin.browser.document
import kotlin.dom.addClass
import kotlin.dom.clear
import kotlin.dom.removeClass

/**
 * The action center containing round controls.
 */
class ActionCenterComponent(
    private val foldFn: () -> Unit,
    private val callFn: () -> Unit,
    private val raiseFn: (raiseAmount: Int) -> Unit,
    private val getAmountToCallFn: () -> Chips,
    private val getMyFundsFn: () -> Chips // funds minus what I put in the put){}
) : Component() {

    private val raiseModal = RaiseComponent()

    private fun findContainerElement() = document.getElementById(ID_CONTAINER) as HTMLDivElement

    private fun findTitleElement() = document.getElementById(ID_TITLE) as HTMLHeadingElement

    var youWinFlag = false
        set(value) {
            if (field == value) return
            field = value

            if (value) {
                findTitleElement().innerHTML = "You Win!"
                findContainerElement().addClass("poker-action-center-win")
                renderYouWinCelebration()
            } else {
                findTitleElement().innerHTML = "Action Center"
                findContainerElement().removeClass("poker-action-center-win")
            }
        }

    override fun RenderContext.addStaticToDOM() {
        div("uk-position-top-center uk-width-1-6 uk-padding-small") {
            id = ID_CONTAINER

            h3 {
                id = ID_TITLE
                +"Action Center"
            }

            div("uk-button-group") {
                button(classes = "uk-button uk-button-danger uk-button-large") {
                    id = ID_BTN_FOLD
                    +"Fold"
                }.onclick = { it.preventDefault(); foldFn() }
                button(classes = "uk-button uk-button-secondary uk-button-large") {
                    id = ID_BTN_CALL
                    +"Call"
                }.onclick = { it.preventDefault(); callFn() }
                button(classes = "uk-button uk-button-primary uk-button-large") {
                    id = ID_BTN_RAISE
                    +"Raise"
                }.onclick = { it.preventDefault(); clickRaise() }
            }

            // Chip stack of amount to call
            div {
                div("uk-margin-top") {
                    h4 { +"Amount to call" }
                }
                div("uk-width-1-1") {
                    id = ID_AMT_TO_CALL
                    getAmountToCallFn().render(this@addStaticToDOM, sizeMod = AMT_TO_CALL_CHIPS_SIZE_MOD)
                }
            }

            raiseModal.addToDOM(this@addStaticToDOM)
        }
    }

    override fun update() {
        raiseModal.update()

        (document.getElementById(ID_AMT_TO_CALL) as HTMLElement).also {
            it.clear()
            val addedToDOM = Notify()
            getAmountToCallFn().render(it.append, sizeMod = AMT_TO_CALL_CHIPS_SIZE_MOD, addedToDOM = addedToDOM)
            addedToDOM.notify()
        }
    }

    fun enableActivePlayerControls() {
        document.getElementById(ID_BTN_FOLD)!!.removeAttribute("disabled")
        document.getElementById(ID_BTN_CALL)!!.removeAttribute("disabled")
        document.getElementById(ID_BTN_RAISE)!!.removeAttribute("disabled")
    }

    fun disableActivePlayerControls() {
        document.getElementById(ID_BTN_FOLD)!!.setAttribute("disabled", "")
        document.getElementById(ID_BTN_CALL)!!.setAttribute("disabled", "")
        document.getElementById(ID_BTN_RAISE)!!.setAttribute("disabled", "")
    }

    private fun clickRaise() {
        UIkit.modal(document.getElementById(ID_RAISE_MODAL)).show()
    }

    private inner class RaiseComponent : Component() {
        // TODO extract a SliderComponent
        override fun RenderContext.addStaticToDOM() {
            div {
                id = ID_RAISE_MODAL
                attributes["uk-modal"] = ""

                div("uk-modal-dialog uk-modal-body") {
                    button(classes = "uk-modal-close-default", type = ButtonType.button) {
                        attributes["uk-close"] = ""
                    }
                    h2("uk-modal-title") { +"Raise" }
                    p { +"Increase the current betting amount" }

                    form {
                        // Drag the bubble with the slider's value along with the thumb
                        fun updateBubble(range: HTMLInputElement, bubble: HTMLOutputElement) {
                            val value = range.value.toInt()
                            val min = range.min.toInt()
                            val max = range.max.toInt()
                            val percent = ((value - min) * 100.0) / (max - min) * 0.85
                            bubble.innerHTML = "$value"
                            // some magic numbers
                            // The constant 30px is the padding of the modal done by UIkit
                            // TODO test whether that "constant" changes with different browser sizes and adjust accordingly
                            bubble.style.left = "calc($percent% + 30px + (${8 + percent * 0.175}px))"

                            // TODO also render slider value as chip stack below the slider
                        }

                        val range = input(InputType.range, classes = "uk-range") {
                            id = ID_RAISE_MODAL_INPUT
                            attributes["min"] = "${getAmountToCallFn().value + 1}"
                            attributes["max"] = "${getMyFundsFn().value}"
                        }
                        val bubble = output("uk-badge range-bubble")
                        range.oninput = { evt ->
                            updateBubble(range, bubble)
                            Unit
                        }
                        updateBubble(range, bubble)

                        input(InputType.submit, classes = "uk-button uk-button-primary")
                    }.onsubmit = { evt ->
                        evt.preventDefault()
                        submitRaise()
                        Unit
                    }
                } // modal body
            }
        }

        private val inputElement by document.elementById<HTMLInputElement>(ID_RAISE_MODAL_INPUT)

        override fun update() {
            inputElement.also {
                it.setAttribute("min", "${getAmountToCallFn().value + 1}")
                it.setAttribute("max", "${getMyFundsFn().value}")
            }
        }

        private fun submitRaise() {
            val money = getMyFundsFn().value
            var amount = inputElement.value.toInt()
            UIkit.modal(document.getElementById(ID_RAISE_MODAL)).hide()
            println("Raise to $amount")

            // The raise amount is the total amount minus the amount to call
            amount -= getAmountToCallFn().value

            if (amount > money) amount = money
            if (amount < 1) return

            raiseFn(amount)
        }
    }

    private fun renderYouWinCelebration() {
        // find our middle card to use as root for celebration particles
        val myCardsContainer = document.getElementById("my-hand-container")!!.firstElementChild!!
        val myCards = myCardsContainer.children
        @Suppress("UNUSED_VARIABLE") val myMiddleCard = myCards[myCards.length / 2]
        js(
            """confetti(myMiddleCard, {
            angle: '90',
                spread: '200',
                startVelocity: '37',
                elementCount: '150',
                dragFriction: '0.05',
                duration: '5000',
                stagger: '0',
                width: '16px',
                height: '16px',
                colors: ["#a864fd", "#29cdff", "#78ff44", "#ff718d", "#fdff6a"]
});"""
        )
        SoundEffects.WIN.play()
        SoundEffects.APPLAUSE.play()
    }

    companion object {
        private const val ID_CONTAINER = "action-center"
        private const val ID_TITLE = "action-center-title"
        private const val ID_BTN_FOLD = "action-center-fold"
        private const val ID_BTN_CALL = "action-center-call"
        private const val ID_BTN_RAISE = "action-center-raise"
        private const val ID_AMT_TO_CALL = "amount-to-call"

        private const val ID_RAISE_MODAL = "action-center-raise-modal"
        private const val ID_RAISE_MODAL_INPUT = "action-center-raise-modal-input"

        private const val AMT_TO_CALL_CHIPS_SIZE_MOD = 0.6
    }
}
