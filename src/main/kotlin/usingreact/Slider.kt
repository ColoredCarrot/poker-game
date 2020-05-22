package usingreact

import kotlinx.html.InputType
import kotlinx.html.js.onInputFunction
import org.w3c.dom.HTMLInputElement
import react.RBuilder
import react.RProps
import react.RPureComponent
import react.RState
import react.dom.input
import react.dom.jsStyle
import react.dom.output

fun RBuilder.slider(
    min: Int, max: Int,
    value: Int? = null,
    onChange: (newValue: Int) -> Unit
) = child(Slider::class) {
    attrs {
        this.min = min; this.max = max
        this.value = value ?: min + (max - min) / 2
        this.onChange = onChange
    }
}

external interface SliderProps : RProps {
    var min: Int
    var max: Int
    var value: Int
    var onChange: (Int) -> Unit
}

class Slider(props: SliderProps) : RPureComponent<SliderProps, RState>(props) {
    override fun RBuilder.render() {
        input(InputType.range, classes = "uk-range") {
            attrs {
                min = props.min.toString()
                max = props.max.toString()
                onInputFunction = { evt ->
                    val newValue = (evt.currentTarget as HTMLInputElement).value.toInt()
                    props.onChange(newValue)
                }
            }
        }
        output(classes = "uk-badge range-bubble") {
            val value = props.value
            +"$value"

            val min = props.min
            val max = props.max
            val percent = ((value - min) * 100.0) / (max - min) * 0.85
            attrs.jsStyle.left = "calc($percent% + 30px + (${8 + percent * 0.175}px))"
        }
    }
}
