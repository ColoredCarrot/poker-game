package host

import comm.Host
import kotlinext.js.jsObject
import kotlinx.html.InputType
import kotlinx.html.js.onClickFunction
import kotlinx.html.js.onFocusFunction
import org.w3c.dom.HTMLInputElement
import react.RBuilder
import react.RProps
import react.child
import react.dom.a
import react.dom.div
import react.dom.form
import react.dom.h1
import react.dom.input
import react.dom.span
import react.dom.strong
import react.getValue
import react.setValue
import react.useEffectWithCleanup
import react.useState
import reactutils.functionalComponentEx
import shared.htmlAttrs
import shared.modifyURLSearchParams
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

        connHook.open.handle { peerId ->
            actualPeerId = peerId
        }
        connHook.close.handle { reconnectFn ->
            actualPeerId = null
        }
        connHook.error.handle { err ->
            error = err
        }

        // TODO replace with onConnect or something and also hook into disconnects to --playersCount
        props.connections.connectionRejector = {
            ++playersCount
            true
        }

        return@useEffectWithCleanup {
            connHook.clearHandlers()
            props.connections.connectionRejector = { false }
        }
    }



    div("poker-lobby-bg") {

        div("uk-container poker-lobby-main") {
            htmlAttrs["uk-height-viewport"] = "expand: true"

            h1 { +"Host your own game" }

            if (error != null) div("poker-lobby-error") {
                strong { +"$error" }
            } else div {
                +"Status: "

                if (actualPeerId != null) {
                    span("uk-text-success") { +"Connected. " }
                    +"Waiting for players... "
                    a("#") {
                        +"Start game with $playersCount players"

                        attrs.onClickFunction = { evt ->
                            evt.preventDefault()
                            println("clicked to start game")
                            props.switchToPlayingPhaseFn()
                        }
                    }

                    form(classes = "uk-form-stacked uk-grid-small") {
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
                    }
                } else {
                    +"Connecting..."
                    div("poker-lobby-spinner") {
                        htmlAttrs["uk-spinner"] = ""
                    }
                }
            } // Status

        }

    }
}
