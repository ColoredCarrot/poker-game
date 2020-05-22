package usingreact

import kotlinext.js.jsObject
import kotlinx.html.classes
import org.w3c.dom.HTMLElement
import react.RBuilder
import react.RProps
import react.child
import react.dom.div
import react.dom.h3
import react.dom.img
import react.dom.p
import react.useEffect
import react.useLayoutEffect
import reactutils.functionalComponentEx
import shared.Offsets
import shared.PlayerInfo
import shared.PlayerInfoList
import shared.RoundAction
import shared.SessionId
import shared.applyStyle
import shared.attrsApplyStyle
import shared.mirrored
import vendor.useRef

fun RBuilder.otherPlayers(
    otherPlayersList: PlayerInfoList,
    winners: List<SessionId>,
    activePlayer: SessionId?,
    recentAction: Pair<SessionId, RoundAction>?
) = child(OtherPlayers, OtherPlayersProps(otherPlayersList, winners, activePlayer, recentAction))

private data class OtherPlayersProps(
    val otherPlayersList: PlayerInfoList,
    val winners: List<SessionId>,
    val activePlayer: SessionId?,
    val recentAction: Pair<SessionId, RoundAction>?
) : RProps

private val OtherPlayers = functionalComponentEx<OtherPlayersProps>("OtherPlayers") { props ->
    for ((player, offsets) in props.otherPlayersList.zip(OFFSETS)) {
        child(OtherPlayer, jsObject {
            this.player = player
            position = offsets
            isActive = player.sessionId == props.activePlayer
            isWinner = player.sessionId in props.winners
            recentAction = props.recentAction?.takeIf { it.first == player.sessionId }?.second
        })
    }
}

private val OFFSETS = Offsets(6, 12).mirrored()


external interface OtherPlayerProps : RProps {
    var player: PlayerInfo
    var position: Offsets
    var isActive: Boolean
    var isWinner: Boolean
    var recentAction: RoundAction?
}

private val OtherPlayer = functionalComponentEx<OtherPlayerProps>("OtherPlayer") { props ->

    val containerRef = useRef<HTMLElement?>(null)
    val overlayRef = useRef<HTMLElement?>(null)

    div("uk-position-absolute") {
        ref = containerRef
        attrsApplyStyle {
            props.position.applyStyle(this)
            maxWidth = "18vw"
        }

        div("uk-card uk-card-small uk-card-body uk-card-default uk-width-auto") {
            if (props.isWinner) {
                attrs.classes += "poker-other-winner"
            }

            h3("uk-card-title") {
                +props.player.profile.name
            }
        }

        div {
            attrsApplyStyle { marginTop = "14px" }
            chipsDisplay(props.player.money, MONEY_RENDER_SIZE_MOD)
        }

        div { attrsApplyStyle { height = "14px" } }

        div {
            props.player.hand?.also {
                handDisplay(it, renderCommunityCards = false, requestHandReorderFn = { TODO() })
            }
        }

    }

    div("uk-position-absolute") {
        ref = overlayRef
        attrsApplyStyle {
            props.position.applyStyle(this)
            zIndex = 100
        }

        when (val recentAction = props.recentAction) {
            RoundAction.Fold -> img(src = "img/cross_out.svg") {}
            RoundAction.Call -> img(src = "img/fold.png") {}
            is RoundAction.Raise -> {
                div("uk-width-1-1") {
                    attrsApplyStyle { height = "100%" }
                    p("uk-text-center uk-text-middle") {
                        attrsApplyStyle {
                            fontWeight = 1000
                            fontSize = "8rem"
                            backgroundColor = "limegreen"
                            color = "white"
                        }
                        +"+ ${recentAction.raiseAmount}"
                    }
                }
            }
        }
    }

    useLayoutEffect {
        val container = containerRef.current!!
        val overlay = overlayRef.current!!
        overlay.style.width = "${container.offsetWidth}px"
        overlay.style.height = "${container.offsetHeight}px"
    }

    useEffect(listOf(props.isActive)) {
        val container = containerRef.current!!
        val overlay = overlayRef.current!!

        if (props.isActive) {
            // Transition to scale up the entire other-player container
            for (it in listOf(container, overlay)) {
                it.style.transition = "transform"
                it.style.transitionDuration = "0.2s"
                it.style.transform = "scale(1.3)"
            }
        } else {
            container.style.transform = "scale(1)"
            overlay.style.transform = "scale(1)"
        }
    }
}

private const val MONEY_RENDER_SIZE_MOD = 0.7
