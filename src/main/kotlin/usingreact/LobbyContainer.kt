package usingreact

import react.RBuilder
import react.RElementBuilder
import react.RProps
import react.child
import react.dom.a
import react.dom.div
import react.dom.span
import reactutils.functionalComponentEx
import shared.attrsApplyStyle
import shared.htmlAttrs
import kotlin.js.Date

fun RBuilder.lobbyContainer(handler: RElementBuilder<RProps>.() -> Unit) =
    child(LobbyContainer, handler = handler)

private val LobbyContainer = functionalComponentEx<RProps>("LobbyContainer") { props ->
    div("poker-lobby-bg") {
        div("poker-lobby-main uk-container") {
            htmlAttrs["uk-height-viewport"] = "expand: true"

            props.children()

            // Footer
            div("uk-position-bottom uk-section uk-section-small") {
                div("uk-container") {
                    div("uk-panel uk-text-muted uk-text-small") {
                        span("uk-align-left") {
                            attrsApplyStyle { cursor = "default" }
                            +"Poker Game © Julian Koch ${Date().getFullYear()}"
                        }
                        span("uk-align-right") {
                            a(classes = "uk-link-reset uk-button-text", href = "https://github.com/ColoredCarrot/poker-game") {
                                +"https://github.com/ColoredCarrot/poker-game"
                            }
                        }
                    }
                }
            }
        }
    }
}
