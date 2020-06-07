/*
 * Copyright 2020 Julian Koch and contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package usingreact

import react.RBuilder
import react.RProps
import react.dom.div
import react.getValue
import react.setValue
import react.useEffectWithCleanup
import react.useState
import reactutils.functionalComponentEx
import shared.RoundAction
import shared.SessionId
import shared.Table
import shared.attrsApplyStyle
import shared.htmlAttrs
import shared.swap
import kotlin.browser.window

data class GameProps(
    val table: Table,
    val activePlayer: SessionId?,
    val amountToCall: Int,
    val actionCenterCallbacks: ActionCenterCallbacks,
    val onHandReorder: (newOrder: List<Int>) -> Unit,
    val lastAction: Pair<RoundAction, SessionId>?
) : RProps

val GameProps.myself get() = table.myself.playerInfo
val GameProps.mySessionId get() = myself.sessionId

fun RBuilder.game(props: GameProps) = child(Game, props) {}

private val Game = functionalComponentEx<GameProps>("Game") { props ->

    var showLastAction by useState(false)

    useEffectWithCleanup(listOf(props.lastAction)) {
        showLastAction = true

        var clearShowLastActionHandle: Int?
        clearShowLastActionHandle = window.setTimeout({
            showLastAction = false
            clearShowLastActionHandle = null
        }, SHOW_RECENT_ACTION_FOR_MS)

        return@useEffectWithCleanup {
            clearShowLastActionHandle?.also { window.clearTimeout(it); showLastAction = false }
        }
    }

    div("uk-background-cover uk-background-center-center") {
        attrsApplyStyle { backgroundImage = "url(table.svg)" }
        htmlAttrs["uk-height-viewport"] = ""

        actionCenter(
            props.myself.money.value,
            props.amountToCall,
            enableActivePlayerControls = props.mySessionId == props.activePlayer,
            youWin = props.mySessionId in props.table.winners,
            callbacks = props.actionCenterCallbacks
        )

        div("uk-position-center poker-pot-container") {
            chipsDisplay(props.table.pot, 1.0)
        }

        myselfComponent(props.table.myself, props.onHandReorder)

        otherPlayers(
            props.table.otherPlayers, props.table.winners, props.activePlayer,
            props.lastAction?.takeIf { showLastAction }?.swap()
        )

    }

}

private const val SHOW_RECENT_ACTION_FOR_MS = 2000
