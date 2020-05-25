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
