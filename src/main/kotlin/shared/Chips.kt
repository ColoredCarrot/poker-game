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

package shared

import kotlinx.serialization.Decoder
import kotlinx.serialization.Encoder
import kotlinx.serialization.KSerializer
import kotlinx.serialization.PrimitiveDescriptor
import kotlinx.serialization.PrimitiveKind
import kotlinx.serialization.SerialDescriptor
import kotlinx.serialization.Serializable
import kotlinx.serialization.Serializer

@Serializable(with = Chips.ChipsSerializer::class)
class Chips(
    value: Int,
    private val dispenser: ChipDispenser = ChipDispenser.DEFAULT
) {

    @set:Deprecated("don't use! will be immutable")
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

    fun setValue(v: Int) = Chips(v, dispenser)

    inline fun updateValue(vc: (Int) -> Int) = setValue(vc(value))

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
        internal const val MAX_CHIPS_PER_STACK = 5
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

operator fun Chips.plus(x: Int) = updateValue { it + x }
operator fun Chips.minus(x: Int) = updateValue { it - x }
