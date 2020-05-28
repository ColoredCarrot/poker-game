package usingreact

import kotlinext.js.jsObject
import kotlinx.html.InputType
import kotlinx.html.classes
import kotlinx.html.js.onChangeFunction
import org.w3c.dom.HTMLInputElement
import react.RBuilder
import react.RProps
import react.child
import react.dom.div
import react.dom.h4
import react.dom.input
import react.dom.li
import react.dom.ul
import reactutils.functionalComponentEx
import shared.attrsApplyStyle

fun RBuilder.lobbyPlayerList(otherPlayers: List<String>, ownName: String, ownNameValidated: Boolean, setOwnNameFn: (String) -> Unit) =
    child(LobbyPlayerList, jsObject {
        this.ownName = ownName
        this.ownNameValidated = ownNameValidated
        this.setOwnNameFn = setOwnNameFn
        this.otherPlayers = otherPlayers
    })

private external interface LobbyPlayerListProps : RProps {
    var ownName: String
    var ownNameValidated: Boolean
    var setOwnNameFn: (String) -> Unit
    var otherPlayers: List<String>
}

private val LobbyPlayerList = functionalComponentEx<LobbyPlayerListProps>("LobbyPlayerList") { props ->
    div {
        h4 {
            attrsApplyStyle { marginTop = 34; marginBottom = 0 }
            +"Players"
        }
        ul("uk-list uk-list-hyphen") {
            attrsApplyStyle { marginTop = 4 }

            li {
                input(type = InputType.text, classes = "uk-input uk-form-blank uk-form-width-medium") {
                    if (props.ownNameValidated) {
                        attrs.classes += "uk-form-success"
                    }
                    attrs.placeholder = "Enter your name..."
                    attrs.value = props.ownName
                    attrs.onChangeFunction = { evt ->
                        val value = (evt.currentTarget as HTMLInputElement).value
                        props.setOwnNameFn(value)
                    }
                }
                attrsApplyStyle { marginBottom = 8 }
            }

            for (otherPlayer in props.otherPlayers) {
                li {
                    attrsApplyStyle { paddingLeft = 10; height = 40; marginTop = 0 }
                    +otherPlayer
                }
            }
        }
    }
}
