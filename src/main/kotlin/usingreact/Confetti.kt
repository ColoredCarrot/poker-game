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

import org.w3c.dom.HTMLElement
import react.RBuilder
import react.RComponent
import react.RProps
import react.RState
import react.dom.div
import shared.attrsApplyStyle
import vendor.createRef

fun RBuilder.confetti(trigger: Boolean, cfg: dynamic, additionalCallback: () -> Unit) = child(Confetti::class) {
    attrs.trigger = trigger
    attrs.cfg = cfg
    attrs.additionalCallback = additionalCallback
}

private external interface ConfettiProps : RProps {
    var trigger: Boolean
    var cfg: dynamic
    var additionalCallback: () -> Unit
}

private class Confetti : RComponent<ConfettiProps, RState>() {

    private val ref = createRef<HTMLElement>()

    override fun RBuilder.render() {
        div {
            ref = this@Confetti.ref
            attrsApplyStyle {
                position = "absolute"
                width = 1
                height = 1
                top = "50%"
                left = "50%"
                transform = "translate(-50%, -50%)"
            }
        }
    }

    override fun shouldComponentUpdate(nextProps: ConfettiProps, nextState: RState): Boolean {
        return nextProps.trigger && !props.trigger
    }

    override fun componentDidUpdate(prevProps: ConfettiProps, prevState: RState, snapshot: Any) {
        if (props.trigger && !prevProps.trigger) {

            @Suppress("UNUSED_VARIABLE")
            val container = ref.current!!

            @Suppress("UNUSED_VARIABLE")
            val cfg = props.cfg

            js("confetti(container, cfg);")

            props.additionalCallback()
        }
    }

}
