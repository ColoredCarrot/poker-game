package host

import comm.Host
import comm.msg.Messages
import comm.msg.handledBy
import comm.msg.send
import kotlinx.html.InputType
import kotlinx.html.classes
import kotlinx.html.js.onClickFunction
import kotlinx.html.js.onFocusFunction
import org.w3c.dom.HTMLInputElement
import react.RBuilder
import react.RComponent
import react.RProps
import react.RState
import react.dom.button
import react.dom.div
import react.dom.form
import react.dom.h1
import react.dom.hr
import react.dom.input
import react.dom.span
import react.dom.strong
import react.setState
import shared.SessionId
import shared.childEx
import shared.counted
import shared.htmlAttrs
import shared.modifyURLSearchParams
import usingreact.lobbyContainer
import usingreact.lobbyPlayerList
import kotlin.browser.window

fun RBuilder.hostLobbyGamePhase(connections: Host, switchToPlayingPhaseFn: (names: Map<SessionId, String>) -> Unit) =
    childEx(HostLobbyGamePhase::class) {
        this.connections = connections
        this.switchToPlayingPhaseFn = switchToPlayingPhaseFn
    }

private external interface HostLobbyGamePhaseProps : RProps {
    var connections: Host
    var switchToPlayingPhaseFn: (names: Map<SessionId, String>) -> Unit
}

private class HostLobbyGamePhaseState : RState {
    var actualPeerId: String? = null
    var playersCount: Int = 1
    var error: dynamic = null
    var chooseName = ""
    var otherNames = mapOf<SessionId, String>()
}

private class HostLobbyGamePhase : RComponent<HostLobbyGamePhaseProps, HostLobbyGamePhaseState>() {

    init {
        state = HostLobbyGamePhaseState()
    }

    override fun componentDidMount() {
        val connHook = props.connections.hook

        connHook.open { peerId ->
            setState { actualPeerId = peerId }
        }
        connHook.close { reconnectFn ->
            setState { actualPeerId = null }
        }
        connHook.error { err ->
            setState { error = err }
        }

        connHook.connectedToPeer { sid ->
            setState { ++playersCount }
            sendPlayerList()
        }
        connHook.disconnectedFromPeer { sid ->
            setState {
                --playersCount
                otherNames -= sid
            }
        }

        props.connections.connectionAcceptor = { true }

        props.connections.receive(
            Messages.Lobby_SetName.Type handledBy { (m) ->
                val newOtherNames = state.otherNames + (m.myId to m.name)
                setState { otherNames = newOtherNames }
            }
        )

    }

    override fun componentWillUnmount() {
        props.connections.hook.clear()
        props.connections.connectionAcceptor = { false }
        props.connections.receive()
    }

    override fun componentDidUpdate(
        prevProps: HostLobbyGamePhaseProps,
        prevState: HostLobbyGamePhaseState,
        snapshot: Any
    ) {
        // Send update to participants when someone changes their name
        if (prevState.chooseName != state.chooseName || prevState.otherNames != state.otherNames) {
            sendPlayerList()
        }
    }

    private fun sendPlayerList() {
        props.connections.send(
            Messages.Lobby_UpdatePlayerList(
                allNames = state.otherNames.values + state.chooseName,
                sanitize = true // in case our name is blank
            )
        )
    }

    override fun RBuilder.render() {
        lobbyContainer {
            h1 { +"Host your own game" }

            if (state.error != null) div("poker-lobby-error") {
                strong { +"${state.error}" }
            } else div {
                +"Status: "

                if (state.actualPeerId != null) {
                    span("uk-text-success") { +"Connected. " }
                    +"Waiting for players..."

                    // Display game ID, direct link
                    form(classes = "uk-form-stacked uk-grid-small poker-lobby-form") {
                        htmlAttrs["uk-grid"] = ""

                        div("uk-width-1-2") {
                            div("poker-lobby-info-label") { +"Your game ID: " }
                            div("uk-form-controls") {
                                input(classes = "uk-input", type = InputType.text) {
                                    attrs.value = state.actualPeerId!!
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
                                        it.append("game", state.actualPeerId!!)
                                    }
                                    attrs.value = directLink
                                }
                            }
                        }
                    } // form containing game ID and direct link

                    // Button to start game
                    hr {}
                    button(classes = "uk-button uk-button-large") {
                        attrs.classes += if (state.playersCount < 2) {
                            attrs.disabled = true
                            "uk-button-disabled"
                        } else "uk-button-primary"

                        +"Start Game with ${"player".counted(state.playersCount)}"

                        attrs.onClickFunction = { evt ->
                            evt.preventDefault()
                            println("clicked to start game")
                            props.switchToPlayingPhaseFn(state.otherNames + (props.connections.peerId to state.chooseName))
                        }
                    }

                    lobbyPlayerList(state.otherNames.values.toList(), state.chooseName, false) { newName ->
                        setState { chooseName = newName }
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

}
