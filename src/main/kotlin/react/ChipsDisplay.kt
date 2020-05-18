package react

import org.w3c.dom.CanvasRenderingContext2D
import org.w3c.dom.HTMLCanvasElement
import org.w3c.dom.HTMLImageElement
import react.dom.canvas
import react.dom.div
import react.dom.img
import shared.Chips
import vendor.findDOMNode
import kotlin.browser.document

private fun guessViewportSizeMod(): Double {
    val vw = document.documentElement!!.clientWidth
    return when {
        vw >= 1600 -> 1.0
        vw >= 1200 -> 0.8
        vw >= 960 -> 0.5
        else -> 0.3
    }
}

fun RBuilder.chipsDisplay(chips: Chips, sizeMod: Double) = child(ChipsDisplay::class) {
    attrs.chips = chips
    attrs.actualSizeMod = sizeMod * guessViewportSizeMod()
}

private external interface ChipsDisplayProps : RProps {
    var chips: Chips
    var actualSizeMod: Double
}

private class ChipsDisplay : RComponent<ChipsDisplayProps, RState>() {

    override fun RBuilder.render() {
        div {
            attrs["uk-grid"] = ""

            val chipDist = props.chips.calculateDistribution()
            for ((chip, numOfChip) in chipDist) {
                child(ChipStack::class) {
                    attrs {
                        this.chip = chip
                        this.numOfChip = numOfChip
                        this.actualSizeMod = props.actualSizeMod
                    }
                }
            }
        }
    }

}

private external interface ChipStackProps : RProps {
    var chip: Int
    var numOfChip: Int
    var actualSizeMod: Double
}

private external interface ChipStackState : RState {
    var canvasRef: HTMLCanvasElement
    var imgRef: HTMLImageElement
}

private class ChipStack : RComponent<ChipStackProps, ChipStackState>() {

    override fun RBuilder.render() {

        canvas {
            ref {
                state.canvasRef = findDOMNode(it) as HTMLCanvasElement
            }
        }
        img(src = "chips/${props.chip}.png") {
            attrs.attributes["hidden"] = "hidden"

            ref {
                state.imgRef = findDOMNode(it) as HTMLImageElement
            }
        }

    }

    override fun componentDidMount() {
        val canvas = state.canvasRef

        canvas.width = (CHIP_STACK_WIDTH * props.actualSizeMod).toInt()
        canvas.height = (CHIP_STACK_HEIGHT * props.actualSizeMod).toInt()

        val ctx = canvas.getContext("2d") as CanvasRenderingContext2D
        val img = state.imgRef

        img.onload = {
            val missingChipsInStack = MAX_CHIPS_PER_STACK - props.numOfChip
            for (i in (props.numOfChip - 1) downTo 0) {
                ctx.drawImage(
                    img,
                    0.0,
                    (CHIP_STACK_HEIGHT_DIFF * (i + missingChipsInStack)).toDouble() * props.actualSizeMod,
                    CHIP_STACK_WIDTH.toDouble() * props.actualSizeMod,
                    CHIP_STACK_SINGLE_HEIGHT.toDouble() * props.actualSizeMod
                )
            }
        }
    }
}

private const val MAX_CHIPS_PER_STACK = 5

private const val CHIP_STACK_WIDTH = 128
private const val CHIP_STACK_SINGLE_HEIGHT = CHIP_STACK_WIDTH
private const val CHIP_STACK_HEIGHT_DIFF = 20
private const val CHIP_STACK_HEIGHT =
    CHIP_STACK_SINGLE_HEIGHT + (MAX_CHIPS_PER_STACK - 1) * CHIP_STACK_HEIGHT_DIFF
