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

import org.w3c.dom.CanvasRenderingContext2D
import org.w3c.dom.HTMLCanvasElement
import org.w3c.dom.HTMLImageElement
import org.w3c.dom.Image
import react.RBuilder
import react.RProps
import react.RPureComponent
import react.RReadableRef
import react.RState
import react.dom.canvas
import react.dom.div
import react.dom.img
import shared.Chips
import vendor.createRef
import kotlin.browser.document
import kotlin.math.min

private fun guessViewportSizeMod(): Double {
    val vw = document.documentElement!!.clientWidth
    val vh = document.documentElement!!.clientHeight
    val vmin = min(vw, vh)
    return when {
        vmin >= 1600 -> 1.0
        vmin >= 1200 -> 0.8
        vmin >= 960 -> 0.6
        else -> 0.5
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

private class ChipsDisplay : RPureComponent<ChipsDisplayProps, RState>() {

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

private class ChipStack : RPureComponent<ChipStackProps, RState>() {

    private lateinit var canvasRef: RReadableRef<HTMLCanvasElement>
    private lateinit var imgRef: RReadableRef<HTMLImageElement>

    override fun RState.init() {
        canvasRef = createRef()
        imgRef = createRef()
    }

    override fun RBuilder.render() {

        canvas {
            ref = canvasRef
        }
        img(src = "chips/${props.chip}.png") {
            attrs.attributes["hidden"] = "hidden"
            ref = imgRef
        }

    }

    override fun componentDidMount() {
        paint()
    }

    override fun componentDidUpdate(prevProps: ChipStackProps, prevState: RState, snapshot: Any) {
        paint()
    }

    private fun paint() {
        val canvas = canvasRef.current!!

        canvas.width = (CHIP_STACK_WIDTH * props.actualSizeMod).toInt()
        canvas.height = (CHIP_STACK_HEIGHT * props.actualSizeMod).toInt()

        val ctx = canvas.getContext("2d") as CanvasRenderingContext2D
        val img = imgRef.current!!

        // Use a proxy image so that onload is triggered even if already loaded
        val proxyImg = Image()
        proxyImg.addEventListener("load", {
            ctx.clearRect(0.0, 0.0, canvas.width.toDouble(), canvas.height.toDouble())
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
        })
        proxyImg.src = img.src
    }
}

private const val MAX_CHIPS_PER_STACK = Chips.MAX_CHIPS_PER_STACK

private const val CHIP_STACK_WIDTH = 128
private const val CHIP_STACK_SINGLE_HEIGHT = CHIP_STACK_WIDTH
private const val CHIP_STACK_HEIGHT_DIFF = 20
private const val CHIP_STACK_HEIGHT =
    CHIP_STACK_SINGLE_HEIGHT + (MAX_CHIPS_PER_STACK - 1) * CHIP_STACK_HEIGHT_DIFF
