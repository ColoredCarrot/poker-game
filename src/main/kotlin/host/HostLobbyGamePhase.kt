package host

import comm.Host
import kotlinext.js.jsObject
import kotlinx.html.InputType
import kotlinx.html.classes
import kotlinx.html.js.onClickFunction
import kotlinx.html.js.onFocusFunction
import org.w3c.dom.HTMLInputElement
import react.RBuilder
import react.RProps
import react.child
import react.dom.button
import react.dom.div
import react.dom.form
import react.dom.h1
import react.dom.hr
import react.dom.input
import react.dom.span
import react.dom.strong
import react.getValue
import react.setValue
import react.useEffectWithCleanup
import react.useState
import reactutils.functionalComponentEx
import shared.counted
import shared.htmlAttrs
import shared.modifyURLSearchParams
import usingreact.lobbyContainer
import kotlin.browser.window

fun RBuilder.hostLobbyGamePhase(connections: Host, switchToPlayingPhaseFn: () -> Unit) =
    child(HostLobbyGamePhase, jsObject {
        this.connections = connections
        this.switchToPlayingPhaseFn = switchToPlayingPhaseFn
    })

private external interface HostLobbyGamePhaseProps : RProps {
    var connections: Host
    var switchToPlayingPhaseFn: () -> Unit
}

private val HostLobbyGamePhase = functionalComponentEx<HostLobbyGamePhaseProps>("HostLobbyGamePhase") { props ->

    var actualPeerId by useState<String?>(null)
    var playersCount by useState(1)
    var error by useState<dynamic>(null)

    useEffectWithCleanup(listOf()) {
        val connHook = props.connections.hook

        connHook.open { peerId ->
            actualPeerId = peerId
        }
        connHook.close { reconnectFn ->
            actualPeerId = null
        }
        connHook.error { err ->
            error = err
        }

        // TODO replace with onConnect or something and also hook into disconnects to --playersCount
        props.connections.connectionAcceptor = {
            ++playersCount
            true
        }

        return@useEffectWithCleanup {
            connHook.clear()
            props.connections.connectionAcceptor = { false }
        }
    }


    lobbyContainer {
        h1 { +"Host your own game" }

        if (error != null) div("poker-lobby-error") {
            strong { +"$error" }
        } else div {
            +"Status: "

            if (actualPeerId != null) {
                span("uk-text-success") { +"Connected. " }
                +"Waiting for players..."

                // Display game ID, direct link
                form(classes = "uk-form-stacked uk-grid-small poker-lobby-form") {
                    htmlAttrs["uk-grid"] = ""

                    div("uk-width-1-2") {
                        div("poker-lobby-info-label") { +"Your game ID: " }
                        div("uk-form-controls") {
                            input(classes = "uk-input", type = InputType.text) {
                                attrs.value = actualPeerId!!
                                attrs.readonly = true
                                attrs.autoFocus = true
                                attrs.onFocusFunction = { evt ->
                                    val el = evt.currentTarget as? HTMLInputElement
                                    el?.select()
                                }
                            }
                        }
                    }

                    div("uk-width-1-2") {
                        div("poker-lobby-info-label") { +"Direct link: " }
                        div("uk-form-controls") {
                            input(classes = "uk-input", type = InputType.text) {
                                attrs.readonly = true
                                attrs.onFocusFunction = { evt ->
                                    val el = evt.currentTarget as? HTMLInputElement
                                    el?.select()
                                }
                                val directLink = modifyURLSearchParams(window.location.href) {
                                    it.append("game", actualPeerId!!)
                                }
                                attrs.value = directLink
                            }
                        }
                    }
                } // form containing game ID and direct link

                // Button to start game
                hr {}
                button(classes = "uk-button uk-button-large") {
                    attrs.classes += if (playersCount < 2) {
                        attrs.disabled = true
                        "uk-button-disabled"
                    } else "uk-button-primary"

                    +"Start Game with ${"player".counted(playersCount)}"

                    attrs.onClickFunction = { evt ->
                        evt.preventDefault()
                        println("clicked to start game")
                        props.switchToPlayingPhaseFn()
                    }
                }
            } else {
                +"Connecting..."
                div("poker-lobby-spinner") {
                    htmlAttrs["uk-spinner"] = ""
                }
            }
        } // Status
    } // lobbyContainer
}
