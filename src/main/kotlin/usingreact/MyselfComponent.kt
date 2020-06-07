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
import react.child
import react.dom.div
import reactutils.functionalComponentEx
import shared.PrivateGameState

fun RBuilder.myselfComponent(
    myself: PrivateGameState,
    onHandReorder: (newOrder: List<Int>) -> Unit
) = child(MyselfComponent) {
    attrs {
        this.myself = myself
        this.onHandReorder = onHandReorder
    }
}

private external interface MyselfComponentProps : RProps {
    var myself: PrivateGameState
    var onHandReorder: (newOrder: List<Int>) -> Unit
}

private val MyselfComponent = functionalComponentEx<MyselfComponentProps>("Myself") { props ->

    div("uk-position-bottom-center poker-my-hand-container") {
        chipsDisplay(props.myself.playerInfo.money, MY_MONEY_RENDER_SIZE_MOD)
        handDisplay(props.myself.playerInfo.hand!!, true, props.onHandReorder)
        //TODO onDblClick, explain hand in modal
    }

}

private const val MY_MONEY_RENDER_SIZE_MOD = 0.7
