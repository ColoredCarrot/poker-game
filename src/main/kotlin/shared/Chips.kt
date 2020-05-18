package shared

import kotlinx.html.TagConsumer
import kotlinx.html.js.div
import kotlinx.serialization.Decoder
import kotlinx.serialization.Encoder
import kotlinx.serialization.KSerializer
import kotlinx.serialization.PrimitiveDescriptor
import kotlinx.serialization.PrimitiveKind
import kotlinx.serialization.SerialDescriptor
import kotlinx.serialization.Serializable
import kotlinx.serialization.Serializer
import org.w3c.dom.HTMLElement
import usingreact.chipsDisplay

@Serializable(with = Chips.ChipsSerializer::class)
class Chips(
    value: Int,
    private val dispenser: ChipDispenser = ChipDispenser.DEFAULT
) {

    var value = value
        set(value) {
            if (value < 0) throw IllegalArgumentException("value $value should be non-negative")
            field = value
        }

    init {
        if (value < 0) {
            throw IllegalArgumentException("value $value should be non-negative")
        }
    }

    fun render(
        context: TagConsumer<HTMLElement>,
        sizeMod: Double = 1.0,
        addedToDOM: Notify? = null
    ) {
        react.dom.render(context.div()) {
            chipsDisplay(this@Chips, sizeMod)
        }
    }

    //TODO remove
/*
    fun legacyrender(
        context: TagConsumer<HTMLElement>,
        sizeMod: Double = 1.0,
        addedToDOM: Notify? = null
    ) = with(context) {
        val actualSizeMod = sizeMod * guessViewportSizeMod()

        div {
            attributes["uk-grid"] = ""
            attributes["data-poker-chips-value"] = "$value"

            val chipDist = calculateDistribution()
            for ((chip, numOfChip) in chipDist) {
                val canvasId = Random.nextHtmlId("poker-chip-stack-canvas-")
                canvas("padding-left-small") {
                    id = canvasId
                }
                // Note: for some reason, just storing the result of canvas{} does NOT work.

                val img = Image()
                img.onload = {

                    fun paint() {
                        val canvas = document.getElementById(canvasId) as? HTMLCanvasElement
                        if (canvas == null) {
                            console.error("Chips.render cannot find canvas element #$canvasId")
                            if (addedToDOM != null) console.error("EVEN THOUGH addedToDOM != null (this should never happen)")
                            js("if(console.trace)console.trace();")
                            return
                        }

                        val ctx = canvas.getContext("2d") as CanvasRenderingContext2D

                        canvas.width = (CHIP_STACK_WIDTH * actualSizeMod).toInt()
                        canvas.height = (CHIP_STACK_HEIGHT * actualSizeMod).toInt()

                        val missingChipsInStack = MAX_CHIPS_PER_STACK - numOfChip
                        for (i in (numOfChip - 1) downTo 0) {
                            ctx.drawImage(
                                img,
                                0.0,
                                (CHIP_STACK_HEIGHT_DIFF * (i + missingChipsInStack)).toDouble() * actualSizeMod,
                                CHIP_STACK_WIDTH.toDouble() * actualSizeMod,
                                CHIP_STACK_SINGLE_HEIGHT.toDouble() * actualSizeMod
                            )
                        }
                    }

                    if (addedToDOM != null) {
                        addedToDOM.handle {
                            paint()
                        }
                    } else {
                        //TO DO do this in onFinalize somehow
                        window.setTimeout({

                            // TO DO: this is in a timeout because the below line sometimes throws ClassCastException
                            paint()

                        }, 1)
                    } // painter == null

                } // img.onload
                img.src = "chips/$chip.png"
            }

        }
    }
*/

    /**
     * Calculates the chip distribution for [value],
     * mapping each used chip to its use count.
     * The returned map is sorted by key in natural ascending order.
     */
    internal fun calculateDistribution(): List<Pair<Int, Int>> {
        val dist = ArrayList<Pair<Int, Int>>()

        var left = value
        var chip = dispenser.largestChip
        var numOfChip = 0 // count of chips valued [chip]

        fun processChips() {
            if (numOfChip > 0) {
                // Divide into multiple stacks if more than X chips
                while (numOfChip > MAX_CHIPS_PER_STACK) {
                    dist += chip to MAX_CHIPS_PER_STACK
                    numOfChip -= MAX_CHIPS_PER_STACK
                }
                dist += chip to numOfChip
                numOfChip = 0
            }
        }

        while (left > 0) {
            while (left < chip) {
                processChips()
                chip = dispenser.getNextSmallerChip(chip)
            }

            ++numOfChip
            left -= chip
        }
        processChips()

        dist.reverse()
        return dist
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Chips) return false

        if (value != other.value) return false

        return true
    }

    override fun hashCode(): Int {
        return value
    }

    companion object {
        private const val MAX_CHIPS_PER_STACK = 5

        private const val CHIP_STACK_WIDTH = 128
        private const val CHIP_STACK_SINGLE_HEIGHT = CHIP_STACK_WIDTH
        private const val CHIP_STACK_HEIGHT_DIFF = 20
        private const val CHIP_STACK_HEIGHT =
            CHIP_STACK_SINGLE_HEIGHT + (MAX_CHIPS_PER_STACK - 1) * CHIP_STACK_HEIGHT_DIFF
    }

    @Serializer(forClass = Chips::class)
    object ChipsSerializer : KSerializer<Chips> {
        override val descriptor: SerialDescriptor = PrimitiveDescriptor("Chips", PrimitiveKind.INT)

        override fun serialize(encoder: Encoder, value: Chips) {
            encoder.encodeInt(value.value)
        }

        override fun deserialize(decoder: Decoder): Chips {
            return Chips(decoder.decodeInt())
        }
    }

}
