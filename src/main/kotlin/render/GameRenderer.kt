package render

import kotlinx.html.js.div
import kotlinx.html.style
import shared.Chips
import shared.RenderUtil
import shared.Round
import shared.RoundAction
import shared.SessionId
import shared.Table
import vendor.Swal

class GameRenderer(
    private val table: Table,
    private val getAmountToCallFn: () -> Int,
    private val getActivePlayerFn: () -> SessionId?,
    callbacks: Callbacks
) : Rerenderable {

    private val actionCenter = ActionCenterComponent(
        { renderMyselfFold(); callbacks.foldFn() },
        { renderMyselfCall(); callbacks.callFn() },
        { renderMyselfRaise(it); callbacks.raiseFn(it) },
        getAmountToCallFn = { Chips(getAmountToCallFn()) },
        getMyFundsFn = { table.myself.playerInfo.money }
    )
    private val pot = PotComponent(table.pot)
    private val myself = MyselfComponent(table.myself)
    private val otherPlayers = OtherPlayersComponent(table.otherPlayers, table.winners, getActivePlayerFn)

    private val mySessionId get() = table.mySessionId

    constructor(table: Table, round: Round, callbacks: Callbacks) : this(
        table,
        { round.amountToCall },
        { round.activePlayer.get() },
        callbacks
    )

    override fun render() {
        RenderUtil.renderToBody {
            div("uk-background-cover uk-background-center-center") {
                style = "background-image: url(table.svg)"
                attributes["uk-height-viewport"] = ""

                actionCenter.addToDOM(this@renderToBody)
                pot.addToDOM(this@renderToBody)
                myself.addToDOM(this@renderToBody)
                otherPlayers.addToDOM(this@renderToBody)

            }
        }
        rerender()
    }

    override fun rerender() {
        if (getActivePlayerFn() == mySessionId) {
            actionCenter.enableActivePlayerControls()
        } else {
            actionCenter.disableActivePlayerControls()
        }

        actionCenter.update()
        pot.update()
        myself.update()
        otherPlayers.update()
    }

    //<editor-fold desc="renderMyself:Fold/Call/Raise">
    private fun renderMyselfFold() {
    }

    private fun renderMyselfCall() {
    }

    private fun renderMyselfRaise(amount: Int) {
    }
    //</editor-fold>

    //<editor-fold desc="renderOther:Fold/Call/Raise">
    fun renderOtherFold(actor: SessionId) {
        otherPlayers.renderCurrentAction(actor, RoundAction.Fold)
    }

    fun renderOtherCall(actor: SessionId) {
        otherPlayers.renderCurrentAction(actor, RoundAction.Call)
    }

    fun renderOtherRaise(actor: SessionId, raiseAmount: Int) {
        otherPlayers.renderCurrentAction(actor, RoundAction.Raise(raiseAmount))
    }
    //</editor-fold>

    fun renderNextRound() {
        Swal.Options(
            title = "Next Round",
            showConfirmButton = false,
            timer = 2400
        ).fire()
    }

    private fun getName(sid: SessionId) = table.allPlayers[sid].profile.name

    data class Callbacks(
        val foldFn: () -> Unit,
        val callFn: () -> Unit,
        val raiseFn: (raiseAmount: Int) -> Unit
    )
}
