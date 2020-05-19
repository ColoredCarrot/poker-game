package host

import comm.Host
import kotlinext.js.jsObject
import kotlinx.html.js.onClickFunction
import react.RBuilder
import react.RProps
import react.child
import react.dom.a
import react.dom.p
import react.functionalComponent
import react.getValue
import react.setValue
import react.useEffect
import react.useState

fun RBuilder.hostLobbyGamePhase(connections: Host, switchToPlayingPhaseFn: () -> Unit) =
    child(HostLobbyGamePhase, jsObject {
        this.connections = connections
        this.switchToPlayingPhaseFn = switchToPlayingPhaseFn
    })

private external interface HostLobbyGamePhaseProps : RProps {
    var connections: Host
    var switchToPlayingPhaseFn: () -> Unit
}

private val HostLobbyGamePhase = functionalComponent<HostLobbyGamePhaseProps> { props ->

    var playersCount by useState(1)

    useEffect(listOf()) {
        // TODO replace with onConnect or something and also hook into disconnects to --playersCount
        props.connections.connectionRejector = {
            ++playersCount
            true
        }
    }

    p {
        +"Waiting for players... "

        a("#") {
            +"Start game with $playersCount players"

            attrs.onClickFunction = { evt ->
                evt.preventDefault()
                println("clicked to start game")
                props.switchToPlayingPhaseFn()
            }
        }
    }
}
