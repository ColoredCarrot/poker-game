package usingreact

import react.RBuilder
import react.RElementBuilder
import react.RProps
import react.child
import react.dom.div
import reactutils.functionalComponentEx
import shared.htmlAttrs

fun RBuilder.lobbyContainer(handler: RElementBuilder<RProps>.() -> Unit) =
    child(LobbyContainer, handler = handler)

private val LobbyContainer = functionalComponentEx<RProps>("LobbyContainer") { props ->
    div("poker-lobby-bg") {
        div("poker-lobby-main uk-container") {
            htmlAttrs["uk-height-viewport"] = "expand: true"
            props.children()
        }
    }
}
