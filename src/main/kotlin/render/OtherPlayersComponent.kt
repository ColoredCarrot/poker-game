package render

import comm.msg.Messages
import kotlinx.html.dom.append
import kotlinx.html.id
import kotlinx.html.img
import kotlinx.html.js.div
import kotlinx.html.js.h3
import kotlinx.html.js.p
import kotlinx.html.span
import kotlinx.html.style
import org.w3c.dom.HTMLDivElement
import org.w3c.dom.HTMLElement
import org.w3c.dom.HTMLHeadingElement
import shared.Offsets
import shared.PlayerInfo
import shared.PlayerInfoList
import shared.RenderContext
import shared.RenderUtil
import shared.RoundAction
import shared.SessionId
import shared.elementById
import shared.mirrored
import shared.render
import shared.toStyleString
import kotlin.browser.document
import kotlin.browser.window
import kotlin.dom.addClass
import kotlin.dom.clear
import kotlin.dom.removeClass

class OtherPlayersComponent(
    private val players: PlayerInfoList,
    private val winners: List<SessionId>,
    private val getActivePlayerFn: () -> SessionId?
) : Component() {

    private val components =
        players.zip(OFFSETS).mapTo(ArrayList()) { (player, offsets) -> OtherPlayerComponent(player, offsets) }

    override fun RenderContext.addStaticToDOM() {
        div {
            id = ID_CONTAINER
            // We know that the iteration order of players is consistent
            for (component in components) {
                component.addToDOM(this@addStaticToDOM)
            }
        }
    }

    private val containerElement by document.elementById<HTMLDivElement>(ID_CONTAINER)

    override fun update() {
        // Remove disconnected players
        components.retainAll {
            val isGone = it.player !in players
            if (isGone) it.removeFromDOM()
            !isGone
        }

        // Add newly connected players
        players
            .filter { pl -> components.none { it.player == pl } }
            .zip(OFFSETS - components.map { it.offsets })
            .mapTo(components) { (newPlayer, offsets) ->
                OtherPlayerComponent(newPlayer, offsets)
                    .also { it.addToDOM(containerElement.append) }
            }

        // Update all players
        for (component in components) {
            component.isWinnerFlag = component.player.sessionId in winners
            component.isActiveFlag = component.player.sessionId == getActivePlayerFn()
            component.update()
        }
    }

    fun renderCurrentAction(player: SessionId, action: RoundAction, time: Int = 2500) {
        components.first { it.player.sessionId == player }.also {
            it.currentAction = action
            it.update()
        }
        window.setTimeout({
            components.first { it.player.sessionId == player }.also {
                it.currentAction = null
                it.update()
            }
        }, time)
    }

    companion object {
        private val OFFSETS = Offsets(6, 12).mirrored()

        private const val ID_CONTAINER = "other-players"
    }
}

@Suppress("PrivatePropertyName")
private class OtherPlayerComponent(val player: PlayerInfo, val offsets: Offsets) : Component() {

    var isWinnerFlag = false
    var isActiveFlag = false
    var currentAction: RoundAction? = null

    private val ID_CONTAINER = "other-player--" + player.sessionId
    private val ID_TITLE = "other-player-title--" + player.sessionId
    private val ID_MONEY = "other-player-money--" + player.sessionId
    private val ID_HAND = "other-player-hand--" + player.sessionId
    private val ID_OVERLAY = "other-player-overlay--" + player.sessionId
    private val ID_OVERLAY_FOLD = "other-player-overlay-fold--" + player.sessionId
    private val ID_OVERLAY_CALL = "other-player-overlay-call--" + player.sessionId
    private val ID_OVERLAY_RAISE = "other-player-overlay-raise--" + player.sessionId
    private val ID_OVERLAY_RAISE_AMT = "other-player-overlay-raise-amt--" + player.sessionId

    override fun RenderContext.addStaticToDOM() {
        div("uk-position-absolute") {
            style = "${offsets.toStyleString()} max-width: 18vw;"
            id = ID_CONTAINER

            div("uk-card uk-card-small uk-card-body uk-card-default uk-width-auto") {
                h3("uk-card-title") {
                    id = ID_TITLE
                    +player.profile.name
                }
            }

            div {
                id = ID_MONEY
                style = "margin-top: 14px;"
                player.money.render(this@addStaticToDOM, sizeMod = MONEY_RENDER_SIZE_MOD)
            }

            div { style = "height: 14px;" }

            div {
                id = ID_HAND
                player.hand?.render(this@addStaticToDOM, skipCommunityCards = true)
            }

        }

        div("uk-position-absolute") {
            id = ID_OVERLAY
            style = offsets.toStyleString() + " z-index: 100;"

            window.setTimeout({
                val normalContainer = document.getElementById(ID_CONTAINER) as HTMLElement
                val overlay = document.getElementById(ID_OVERLAY) as HTMLElement
                overlay.style.width = "${normalContainer.offsetWidth}px"
                overlay.style.height = "${normalContainer.offsetHeight}px"
            }, 1)

            img(src = "img/cross_out.svg") {
                id = ID_OVERLAY_FOLD
            }
            img(src = "img/fold.png") {
                id = ID_OVERLAY_CALL
            }
            div("uk-width-1-1") {
                id = ID_OVERLAY_RAISE
                style = "height: 100%"
                p("uk-text-center uk-text-middle") {
                    style = "font-weight: 1000; font-size: 8rem; background-color: limegreen; color: white;"
                    +"+ "
                    span { id = ID_OVERLAY_RAISE_AMT }
                }
            }
        }
    }

    private fun findTitle() = document.getElementById(ID_TITLE) as HTMLHeadingElement

    private val containerElement: HTMLElement by document.elementById(ID_CONTAINER)
    private val moneyElement: HTMLElement by document.elementById(ID_MONEY)
    private val handElement: HTMLElement by document.elementById(ID_HAND)
    private val overlayElement: HTMLElement by document.elementById(ID_OVERLAY)

    override fun update() {
        moneyElement.also {
            it.clear()
            player.money.render(it.append, sizeMod = MONEY_RENDER_SIZE_MOD)
        }
        handElement.also {
            it.clear()
            player.hand?.render(it.append, skipCommunityCards = true)
        }

        if (isWinnerFlag) {
            findTitle().parentElement!!.addClass("poker-other-winner")
        } else {
            findTitle().parentElement!!.removeClass("poker-other-winner")
        }

        if (isActiveFlag) {
            // Transition to scale up the entire other-player container
            containerElement.also {
                it.style.transition = "transform"
                it.style.transitionDuration = "0.2s"
                it.style.transform = "scale(1.3)"
            }
            overlayElement.also {
                it.style.transition = "transform"
                it.style.transitionDuration = "0.2s"
                it.style.transform = "scale(1.3)"
            }
        } else {
            containerElement.style.transform = "scale(1)"
            overlayElement.style.transform = "scale(1)"
        }

        when (val currentAction = currentAction) {
            RoundAction.Fold -> {
                RenderUtil.fadeIn(document.getElementById(ID_OVERLAY_FOLD) as HTMLElement)
            }
            RoundAction.Call -> {
                RenderUtil.fadeIn(document.getElementById(ID_OVERLAY_CALL) as HTMLElement)
            }
            is RoundAction.Raise -> {
                (document.getElementById(ID_OVERLAY_RAISE_AMT) as HTMLElement).innerHTML = "${currentAction.raiseAmount}"
                RenderUtil.fadeIn(document.getElementById(ID_OVERLAY_RAISE) as HTMLElement)
            }
            null -> {
                RenderUtil.fadeOut(document.getElementById(ID_OVERLAY_FOLD) as HTMLElement)
                RenderUtil.fadeOut(document.getElementById(ID_OVERLAY_CALL) as HTMLElement)
                RenderUtil.fadeOut(document.getElementById(ID_OVERLAY_RAISE) as HTMLElement)
            }
        }
    }

    fun removeFromDOM() {
        containerElement.remove()
    }

    companion object {
        private const val MONEY_RENDER_SIZE_MOD = 0.7
    }
}
