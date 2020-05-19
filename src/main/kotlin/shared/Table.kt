package shared

import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
class Table(
    val pot: Chips,
    val otherPlayers: PlayerInfoList,
    val myself: PrivateGameState
) {

    @Transient
    val winners = ArrayList<SessionId>()

    constructor(
        pot: Chips, otherPlayers: PlayerInfoList, myself: PrivateGameState,
        winners: List<SessionId>
    ) : this(pot, otherPlayers, myself) {
        this.winners += winners
    }

    val allPlayers get() = PlayerInfoList(LinkedHashSet(otherPlayers.all).also { it += myself.playerInfo })

    val mySessionId get() = myself.playerInfo.sessionId

    val allSessionIds get() = listOf(mySessionId) + otherPlayers.all.map { it.sessionId }

}

inline fun Table.setPot(pot: (Chips) -> Chips) = Table(
    pot(this.pot),
    otherPlayers, myself, winners
)

inline fun Table.setMyself(myself: (PrivateGameState) -> PrivateGameState) = Table(
    pot, otherPlayers, myself(this.myself), winners
)

inline fun Table.setWinners(winners: (List<SessionId>) -> List<SessionId>) = Table(
    pot, otherPlayers, myself, winners(this.winners)
)

inline fun Table.setMyselfPlayerInfo(myselfPlayerInfo: (PlayerInfo) -> PlayerInfo) =
    setMyself { it.setPlayerInfo(myselfPlayerInfo) }

fun Table.reorderMyHand(newOrder: List<Int>) = Table(
    pot, otherPlayers,
    myself.setPlayerInfo { it.setHand { it!!.reorderManually(newOrder) } },
    winners
)

inline fun Table.mapAllPlayers(map: (PlayerInfo) -> PlayerInfo) =
    Table(
        pot,
        PlayerInfoList(otherPlayers.mapTo(LinkedHashSet(), map)),
        myself.setPlayerInfo(map),
        winners
    )

inline fun Table.mapPlayer(player: SessionId, map: (PlayerInfo) -> PlayerInfo) =
    mapAllPlayers { if (it.sessionId == player) map(it) else it }

//<editor-fold desc="Legacy rendering">
/*fun Table.render(
    context: RenderContext,
    placeBetFn: (Int) -> Unit,
    revealHandFn: (Hand) -> Unit,
    extendActionCenter: RenderContext.() -> Unit = {}
) = with(context) {
    val iAmWinner = mySessionId in winners

    div("uk-background-cover uk-background-center-center") {
        style = "background-image: url(table.svg)"
        attributes["uk-height-viewport"] = ""

        // The action center
        val actionCenter = div("uk-position-top-center uk-width-1-6 uk-padding-small") {
            id = ID_ACTION_CENTER

            if (iAmWinner) {
                classes += "poker-action-center-win"
                attributes[ATTR_WIN] = ""
            }

            h3 {
                id = ID_ACTION_CENTER_TITLE
                if (!iAmWinner) +"Action Center"
                else +"You Win!"
            }
            button(classes = "uk-button uk-button-danger uk-width-1-1") { +"Reveal Hand" }
                .onclick = { evt ->
                evt.preventDefault()
                revealHandFn(myself.playerInfo.hand!!)
            }
            extendActionCenter()
        }

        // The pot
        div("uk-position-center poker-pot-container") {
            id = ID_POT_CONTAINER
            pot.render(this@with)
        }

        // My hand
        div("uk-position-bottom-center poker-my-hand-container") {
            div {
                id = ID_MY_MONEY_CONTAINER
                myself.playerInfo.money.render(this@with, sizeMod = MY_MONEY_RENDER_SIZE_MOD)
                    .onclick = { clickToBet(); Unit }
            }
            renderClickToBetModal(myself.playerInfo.money, placeBetFn)
            div {
                id = ID_MY_HAND_DIRECT_CONTAINER
                myself.playerInfo.hand!!.render(this@with)
                    .ondblclick = { evt ->
                    evt.preventDefault()
                    // On double click, display help to explain the hand
                    //language=HTML
                    UIkit.modal.dialog(
                        """<p class='uk-modal-body'>
                    Your hand is a: <span class='uk-text-bold uk-text-capitalize'>${myself.playerInfo.hand!!.evaluateRank().displayName}</span>
                    </p>""".trimIndent()
                    )
                    Unit
                }
            }
        }

        // Other players
        // We know that the iteration order of players is consistent
        // TO DO: dynamically position all elements
        if (otherPlayers.all.isNotEmpty()) {
            val player = otherPlayers.all.first()
            player.render(this@with, 6, 40, player.sessionId in winners)
        }

        // Listen to mutations to the action center's attributes to display a "You Win" celebration only once
        MutationObserver { muts, _ ->
            for (mut in muts) {
                if (mut.type == "attributes" && mut.attributeName == ATTR_WIN) {
                    val mutTarget = mut.target as HTMLElement
                    val oldWasWin = mut.oldValue == ""
                    val newIsWin = mutTarget.hasAttribute(ATTR_WIN)
                    if (newIsWin && !oldWasWin) {
                        mutTarget.addClass("poker-action-center-win")
                        renderYouWinCelebration()
                    } else if (!newIsWin && oldWasWin) {
                        mutTarget.removeClass("poker-action-center-win")
                    }
                }
            }
        }.observe(
            actionCenter, MutationObserverInit(
                attributes = true,
                attributeFilter = arrayOf(ATTR_WIN),
                attributeOldValue = true
            )
        )

        document.onkeyup = { evt ->
            if (evt.ctrlKey && evt.key == "c") {
                renderYouWinCelebration()
            }
            Unit
        }

    }
}

fun Table.rerender() {
    val iAmWinner = mySessionId in winners

    val actionCenter = document.getElementById(ID_ACTION_CENTER)!!
    if (iAmWinner) actionCenter.setAttribute(ATTR_WIN, "")
    else actionCenter.removeAttribute(ATTR_WIN)
    val actionCenterTitle = document.getElementById(ID_ACTION_CENTER_TITLE) as HTMLHeadingElement
    actionCenterTitle.innerHTML = if (iAmWinner) "You Win!" else "Action Center"

    val potContainer = document.getElementById(ID_POT_CONTAINER)!!
    potContainer.clear()
    potContainer.append { pot.render(this) }

    val myMoneyContainer = document.getElementById(ID_MY_MONEY_CONTAINER)!!
    myMoneyContainer.clear()
    myMoneyContainer.append {
        myself.playerInfo.money.render(this, sizeMod = MY_MONEY_RENDER_SIZE_MOD)
            .onclick = { clickToBet(); Unit }
    }

    // Update max value of "increase bet" slider
    document.getElementById(ID_CLICK_TO_BET_MODAL_INPUT)!!.setAttribute("max", "${myself.playerInfo.money.value}")

    // Rerender money and revealed hands of other players
    for (otherPlayer in otherPlayers) {
        val theirMoneyContainer = document.getElementById(PREFIX_ID_OTHER_PLAYER_MONEY_CONTAINER + otherPlayer.sessionId)!!
        theirMoneyContainer.clear()
        theirMoneyContainer.append {
            otherPlayer.money.render(this, sizeMod = OTHER_PLAYER_MONEY_RENDER_SIZE_MOD)
        }

        val theirRevealedHandContainer =
            document.getElementById(PREFIX_ID_OTHER_PLAYER_HAND_CONTAINER + otherPlayer.sessionId)!!
        theirRevealedHandContainer.clear()
        theirRevealedHandContainer.append {
            otherPlayer.hand?.render(this)
        }

        // render winner status
        val theirTitle = document.getElementById(PREFIX_ID_OTHER_PLAYER_TITLE + otherPlayer.sessionId)!!.parentElement!!
        if (otherPlayer.sessionId in winners) theirTitle.addClass("poker-other-winner")
        else theirTitle.removeClass("poker-other-winner")
    }
}

private fun PlayerInfo.render(context: RenderContext, x: Int, y: Int, isWinner: Boolean) = with(context) {
    div("uk-position-absolute") {
        style = "top: ${y}vh; left: ${x}vw; max-width: 18vw;"

        div("uk-card uk-card-small uk-card-body uk-card-default uk-width-auto") {
            h3("uk-card-title") {
                id = PREFIX_ID_OTHER_PLAYER_TITLE + sessionId
                if (isWinner) classes += "poker-other-winner"
                +profile.name
            }
        }

        div {
            id = PREFIX_ID_OTHER_PLAYER_MONEY_CONTAINER + sessionId
            style = "margin-top: 12px;"
            money.render(this@with, sizeMod = OTHER_PLAYER_MONEY_RENDER_SIZE_MOD)
        }

        div {
            id = PREFIX_ID_OTHER_PLAYER_HAND_CONTAINER + sessionId
            hand?.render(this@with)
        }

    }
}

private fun RenderContext.renderClickToBetModal(availableMoney: Chips, placeBetFn: (Int) -> Unit) {
    div {
        id = ID_CLICK_TO_BET_MODAL
        attributes["uk-modal"] = ""
        div("uk-modal-dialog uk-modal-body") {
            button(classes = "uk-modal-close-default", type = ButtonType.button) {
                attributes["uk-close"] = ""
            }
            h2("uk-modal-title") { +"Increase Bet" }
            p { +"Transfer money from your funds to the pot." }

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
                    // TO DO test whether that "constant" changes with different browser sizes and adjust accordingly
                    bubble.style.left = "calc($percent% + 30px + (${8 + percent * 0.175}px))"
                }

                val range = input(InputType.range, classes = "uk-range") {
                    id = ID_CLICK_TO_BET_MODAL_INPUT
                    attributes["min"] = "1"
                    attributes["max"] = "${availableMoney.value}"
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
                submitClickToBet(availableMoney, placeBetFn)
                Unit
            }
        }
    }
}

private fun clickToBet() {
    UIkit.modal(document.getElementById(ID_CLICK_TO_BET_MODAL)).show()
}

private fun submitClickToBet(money: Chips, placeBetFn: (Int) -> Unit) {
    var amount = (document.getElementById(ID_CLICK_TO_BET_MODAL_INPUT) as HTMLInputElement).value.toInt()
    UIkit.modal(document.getElementById(ID_CLICK_TO_BET_MODAL)).hide()
    println("Bet $amount")

    if (amount < 1) return
    if (amount > money.value) amount = money.value

    placeBetFn(amount)
}

private const val ID_CLICK_TO_BET_MODAL = "poker-click-to-bet-modal"
private const val ID_CLICK_TO_BET_MODAL_INPUT = "poker-click-to-bet-input"

private const val ID_ACTION_CENTER = "action-center"
private const val ID_ACTION_CENTER_TITLE = "poker-action-center-title"

private const val ID_POT_CONTAINER = "poker-pot-container"
private const val ID_MY_MONEY_CONTAINER = "poker-my-money-container"
private const val ID_MY_HAND_DIRECT_CONTAINER = "poker-my-hand-direct-container"
private const val PREFIX_ID_OTHER_PLAYER_TITLE = "poker-other-player-title--"
private const val PREFIX_ID_OTHER_PLAYER_MONEY_CONTAINER = "poker-other-player-money-container--"
private const val PREFIX_ID_OTHER_PLAYER_HAND_CONTAINER = "poker-other-player-hand-container--"

private const val MY_MONEY_RENDER_SIZE_MOD = 0.7
private const val OTHER_PLAYER_MONEY_RENDER_SIZE_MOD = 0.7

private const val ATTR_WIN = "data-poker-win"*/
//</editor-fold>
