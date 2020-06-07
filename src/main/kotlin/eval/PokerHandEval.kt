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

package eval

import kotlinx.html.currentTimeMillis

object RankPokerHandPublic {
    private val hands = intArrayOf(
        Combination.FOUR_OF_A_KIND.rank(), Combination.STRAIGHT_FLUSH.rank(), Combination.STRAIGHT.rank(),
        Combination.FLUSH.rank(), Combination.HIGH_CARD.rank(), Combination.PAIR.rank(), Combination.TWO_PAIR.rank(),
        Combination.ROYAL_FLUSH.rank(), Combination.THREE_OF_A_KIND.rank(), Combination.FULL_HOUSE.rank()
    )
    const val A = 14
    const val K = 13
    const val Q = 12
    const val J = 11
    val suits5 = intArrayOf(1, 2, 4, 8)

    /*
     * Get an int that is bigger as the hand is bigger, breaking ties and keeping actual (real) ties.
     * Apply >>26 to the returned value afterwards, to get the rank of the Combination (0..11)
     *
     * Based and improved on:
     * http://www.codeproject.com/Articles/569271/A-Poker-hand-analyzer-in-JavaScript-using-bit-math
     *
     * nr is an array of length 5, where each number should be 2..14.
     * suit is an array of length 5, where each number should be 1,2,4 or 8.
     */
    @OptIn(ExperimentalStdlibApi::class)
    fun rankPokerHand5(nr: IntArray?, suit: IntArray): Int {
        var v = 0L
        var set = 0
        for (i in 0..4) {
            v += (v and (15L shl nr!![i] * 4)) + (1L shl nr[i] * 4)
            set = set or (1 shl nr[i] - 2)
        }
        var value =
            (v % 15L - if (hasStraight(set) || set == 0x403c / 4) 3L else 1L).toInt() // keep the v value at this point
        value -= (if (suit[0] == suit[1] or suit[2] or suit[3] or suit[4]) 1 else 0) * if (set == 0x7c00 / 4) -5 else 1
        value = hands[value]

        // break ties
        value = value shl 26
        value =
            value or when {
                value == Combination.FULL_HOUSE.rank() shl 26 -> 64 - (v and (v shl 1) and (v shl 2)).countLeadingZeroBits() shl 20
                set == 0x403c / 4 -> 0
                else -> 64 - (v and (v shl 1)).countLeadingZeroBits() shl 20 or ((v and (v shl 1)).countTrailingZeroBits() shl 14)
            }
        value = value or set
        return value

    }

    @OptIn(ExperimentalStdlibApi::class)
    fun initRankPokerHand7() {
        for (i in 0..32767) {
            straightFlush[i] = getStraight(i)
            if (i and 0x403c / 4 == 0x403c / 4) {
                straightFlush[i] = straightFlush[i] or (1 shl 3)
            }
            flush[i] = if (i.countOneBits() >= 5) getUpperFive(i) else 0
        }
        var max = 0
        var count = 0
        val nrs = intArrayOf(1, 2, 4, 8, 1)
        for (nr1 in 2..14) {
            for (nr2 in nr1..14) {
                for (nr3 in nr2..14) {
                    for (nr4 in nr3..14) {
                        for (nr5 in nr4..14) {
                            if (nr1 == nr5) {
                                continue
                            }
                            for (nr6 in nr5..14) {
                                if (nr2 == nr6) {
                                    continue
                                }
                                for (nr7 in nr6..14) {
                                    if (nr3 == nr7) {
                                        continue
                                    }
                                    val hands = get5HandsFrom7(
                                        intArrayOf(
                                            nr1,
                                            nr2,
                                            nr3,
                                            nr4,
                                            nr5,
                                            nr6,
                                            nr7
                                        )
                                    )
                                    var maxValue = 0
                                    var straight = 0
                                    for (hand in hands) {
                                        val value = rankPokerHand5(hand, nrs)
                                        val set = value and (1 shl 14) - 1
                                        straight =
                                            straight or if (hasStraight(set) || set == 0x403c / 4) set else 0
                                        maxValue = if (value > maxValue) value else maxValue
                                    }
                                    val sum =
                                        recurrence[nr1 - 2] + recurrence[nr2 - 2] + recurrence[nr3 - 2] + recurrence[nr4 - 2] + recurrence[nr5 - 2] + recurrence[nr6 - 2] + recurrence[nr7 - 2]
                                    count++
                                    lookup[sum % 4731770] = maxValue
                                    if (sum % 4731770 > max) {
                                        max = sum % 4731770 //7561824
                                    }
                                    RankPokerHandPublic.straight[sum % 4731770] = straight
                                }
                            }
                        }
                    }
                }
            }
        }
        println("count (lower bound):$count")
        println("MAX:$max")
    }

    private const val totalLength = 0
    private val COUNTS = intArrayOf(1, 13, 78, 286, 715, 1287, 1716, 1716)

    @ExperimentalStdlibApi
    private fun getUpperFive(cards: Int): Int {
        var res = cards
        res = if (res.countOneBits() > 5) res and ((1 shl res.countTrailingZeroBits() + 1) - 1).inv() else res
        res = if (res.countOneBits() > 5) res and ((1 shl res.countTrailingZeroBits() + 1) - 1).inv() else res
        return res
    }

    private val recurrence = intArrayOf(
        0, 1, 5, 24, 106, 472, 2058, 8768, 29048, 70347, 233028, 583164, 1472911
    )
    private val lookup = IntArray(4731770)
    private val straight = IntArray(4731770)
    private val flush = IntArray(32768)
    private val straightFlush = IntArray(32768)
    private val cards = IntArray(4)

    // suit must be 0..3 here!!
    @OptIn(ExperimentalStdlibApi::class)
    fun rankPokerHand7(nr: IntArray, suit: IntArray): Int {
        var index = 0
        for (i in 0..3) {
            cards[i] = 0
        }
        for (i in 0..6) {
            cards[suit[i]] =
                cards[suit[i]] or (1L shl nr[i]).toInt()
            index += recurrence[nr[i]]
        }
        index = index % 4731770
        val value = lookup[index]
        var fl = 0
        for (i in 0..3) {
            fl = fl or flush[cards[i]]
        }
        val str = straight[index]
        var straightFl =
            if (fl == 0) 0 else straightFlush[str and cards[0]] or straightFlush[str and cards[1]] or straightFlush[str and cards[2]] or straightFlush[str and cards[3]]
        straightFl = straightFl.takeHighestOneBit()
        return if (straightFl == 1 shl 12) Combination.ROYAL_FLUSH.rank() shl 26 else if (straightFl != 0) Combination.STRAIGHT_FLUSH.rank() shl 26 or straightFl else if (fl != 0) Combination.FLUSH.rank() shl 26 or fl else value
    }

    private fun hasStraight(set: Int): Boolean {
        return 0 != set and (set shr 1) and (set shr 2) and (set shr 3) and (set shr 4)
    }

    private fun getStraight(set: Int): Int {
        return set and (set shl 1) and (set shl 2) and (set shl 3) and (set shl 4)
    }


    @OptIn(ExperimentalStdlibApi::class)
    fun maintest() {
        var count = 0
        for (i in 0..127) {
            if (i.countOneBits() == 3) {
                count++
            }
        }
        println(count)
        println(14 * 14 * 14 * 14 * 13 * 13 * 13 / count)
        test(
            rankPokerHand5(
                intArrayOf(
                    10,
                    J,
                    A,
                    K,
                    Q
                ), suits(intArrayOf(1, 1, 1, 1, 1))
            )
        ) // Royal Flush
        test(
            rankPokerHand5(
                intArrayOf(4, 5, 6, 7, 8),
                suits(intArrayOf(1, 1, 1, 1, 1))
            )
        ) // Straight Flush
        test(
            rankPokerHand5(
                intArrayOf(4, 5, 6, 7, 3),
                suits(intArrayOf(1, 1, 1, 1, 1))
            )
        ) // Straight Flush
        test(
            rankPokerHand5(
                intArrayOf(4, 5, 6, 3, 2),
                suits(intArrayOf(1, 1, 1, 1, 1))
            )
        ) // Straight Flush
        test(
            rankPokerHand5(
                intArrayOf(2, 3, 4, 5, A),
                suits(intArrayOf(1, 1, 1, 1, 1))
            )
        ) // Straight Flush (ace low)
        test(
            rankPokerHand5(
                intArrayOf(8, 8, 8, 8, 9),
                suits(intArrayOf(1, 2, 3, 0, 1))
            )
        ) // 4 of a Kind
        test(
            rankPokerHand5(
                intArrayOf(8, 8, 8, 8, 7),
                suits(intArrayOf(1, 2, 3, 0, 1))
            )
        ) // 4 of a Kind
        test(
            rankPokerHand5(
                intArrayOf(7, 7, 7, A, 7),
                suits(intArrayOf(1, 2, 3, 0, 1))
            )
        ) // 4 of a Kind
        test(
            rankPokerHand5(
                intArrayOf(7, 7, 7, 9, 9),
                suits(intArrayOf(1, 2, 3, 1, 2))
            )
        ) // Full house
        test(
            rankPokerHand5(
                intArrayOf(7, 7, 7, 8, 8),
                suits(intArrayOf(1, 2, 3, 1, 2))
            )
        ) // Full house
        test(
            rankPokerHand5(
                intArrayOf(
                    6,
                    6,
                    6,
                    A,
                    A
                ), suits(intArrayOf(1, 2, 3, 1, 2))
            )
        ) // Full house
        test(
            rankPokerHand5(
                intArrayOf(
                    10,
                    J,
                    6,
                    K,
                    9
                ), suits(intArrayOf(2, 2, 2, 2, 2))
            )
        ) // Flush
        test(
            rankPokerHand5(
                intArrayOf(10, J, 6, 3, 9),
                suits(intArrayOf(2, 2, 2, 2, 2))
            )
        ) // Flush
        test(
            rankPokerHand5(
                intArrayOf(10, J, 5, 3, 9),
                suits(intArrayOf(2, 2, 2, 2, 2))
            )
        ) // Flush
        test(
            rankPokerHand5(
                intArrayOf(
                    10,
                    J,
                    Q,
                    K,
                    A
                ), suits(intArrayOf(1, 2, 3, 2, 0))
            )
        ) // Straight
        test(
            rankPokerHand5(
                intArrayOf(
                    10,
                    J,
                    Q,
                    K,
                    9
                ), suits(intArrayOf(1, 2, 3, 2, 0))
            )
        ) // Straight
        test(
            rankPokerHand5(
                intArrayOf(
                    10,
                    J,
                    Q,
                    8,
                    9
                ), suits(intArrayOf(1, 2, 3, 2, 0))
            )
        ) // Straight
        test(
            rankPokerHand5(
                intArrayOf(2, 3, 4, 5, A),
                suits(intArrayOf(1, 2, 3, 2, 0))
            )
        ) // Straight (ace low)
        test(
            rankPokerHand5(
                intArrayOf(4, 4, 4, 8, 9),
                suits(intArrayOf(1, 2, 3, 1, 2))
            )
        ) // 3 of a Kind
        test(
            rankPokerHand5(
                intArrayOf(4, 4, 4, 8, 7),
                suits(intArrayOf(1, 2, 3, 1, 2))
            )
        ) // 3 of a Kind
        test(
            rankPokerHand5(
                intArrayOf(
                    3,
                    3,
                    3,
                    K,
                    A
                ), suits(intArrayOf(1, 2, 3, 1, 2))
            )
        ) // 3 of a Kind
        test(
            rankPokerHand5(
                intArrayOf(3, 3, 3, 6, 5),
                suits(intArrayOf(1, 2, 3, 1, 2))
            )
        ) // 3 of a Kind
        test(
            rankPokerHand5(
                intArrayOf(8, 8, J, 9, 9),
                suits(intArrayOf(1, 2, 3, 1, 2))
            )
        ) // 2 Pair
        test(
            rankPokerHand5(
                intArrayOf(8, 8, 10, 9, 9),
                suits(intArrayOf(1, 2, 3, 1, 2))
            )
        ) // 2 Pair
        test(
            rankPokerHand5(
                intArrayOf(7, 7, Q, 9, 9),
                suits(intArrayOf(1, 2, 3, 1, 2))
            )
        ) // 2 Pair
        test(
            rankPokerHand5(
                intArrayOf(7, 7, 6, 9, 9),
                suits(intArrayOf(1, 2, 3, 1, 2))
            )
        ) // 2 Pair
        test(
            rankPokerHand5(
                intArrayOf(6, 6, A, 8, 8),
                suits(intArrayOf(1, 2, 3, 1, 2))
            )
        ) // 2 Pair
        test(
            rankPokerHand5(
                intArrayOf(8, 8, 3, 5, 9),
                suits(intArrayOf(1, 2, 3, 1, 2))
            )
        ) // 1 Pair
        test(
            rankPokerHand5(
                intArrayOf(7, 7, 3, 5, 9),
                suits(intArrayOf(1, 2, 3, 1, 2))
            )
        ) // 1 Pair
        test(
            rankPokerHand5(
                intArrayOf(7, 7, 3, 2, 9),
                suits(intArrayOf(1, 2, 3, 1, 2))
            )
        ) // 1 Pair
        test(
            rankPokerHand5(
                intArrayOf(10, 5, 4, 7, 9),
                suits(intArrayOf(1, 2, 3, 1, 2))
            )
        ) // High Card
        test(
            rankPokerHand5(
                intArrayOf(10, 5, 3, 7, 9),
                suits(intArrayOf(1, 2, 3, 1, 2))
            )
        ) // High Card
        println("Wrongs: $testWrongs")
        print("Setting up lookup tables... ")
        var startTime: Long = currentTimeMillis()
        initRankPokerHand7()
        var runTime: Long = currentTimeMillis() - startTime
        println("$runTime ms")
        println("totalLength = $totalLength")
        startTime = currentTimeMillis()
        count7()
        runTime = currentTimeMillis() - startTime
        printHandToCount()
        println("$runTime ms")
        println("AK vs QQ preflop:")
        println(
            rangeVsRange(
                intArrayOf(12, 11, -1, -1, -1, -1, -1, 10, 10),
                intArrayOf(0, 1, -1, -1, -1, -1, -1, 0, 2)
            )
        )
        println("AK vs QQ with flop: AKQ:")
        println(
            rangeVsRange(
                intArrayOf(12, 11, 12, 11, 10, -1, -1, 10, 10),
                intArrayOf(0, 1, 1, 0, 1, -1, -1, 0, 2)
            )
        )
        println("Pair of A on the flow vs random opponent")
        println(
            rangeVsRange(
                intArrayOf(5, 12, 12, 1, 3, -1, -1, -1, -1),
                intArrayOf(0, 0, 1, 1, 2, -1, -1, -1, -1)
            )
        )
        //System.out.println("Pair of 10's preflop against one player with random cards:"); // takes too long
        //System.out.println(rangeVsRange(new int[] {8,8,-1,-1,-1,-1,-1,-1,-1}, new int[] {0,1,-1,-1,-1,-1,-1,-1,-1}));
        //System.out.println("All two-player combinations"); // takes almost forever
        //System.out.println(rangeVsRange(new int[] {-1,-1,-1,-1,-1,-1,-1,-1,-1}, new int[] {-1,-1,-1,-1,-1,-1,-1,-1,-1}));
    }

    private fun suits(`is`: IntArray): IntArray {
        val s = IntArray(`is`.size)
        for (i in `is`.indices) {
            s[i] = suits5[`is`[i]]
        }
        return s
    }

    private var previousTestValue = Long.MAX_VALUE
    private var testWrongs = 0
    private fun test(rankPokerHand: Int) {
        println(rankPokerHand.toString() + ": " + Combination.fromRank(rankPokerHand shr 26))
        if (rankPokerHand >= previousTestValue) {
            println("WRONG") // should not happen
            testWrongs++
        } else {
            previousTestValue = rankPokerHand.toLong()
        }
    }

    private fun printHandToCount() {
        var sum = 0f
        for (count in handToCount) {
            sum += count.toFloat()
        }
        println("total hands: $sum")
        for (rank in handToCount.indices.reversed()) {
            println(
                rank.toString() + ": " + Combination.fromRank(rank) + " => " + handToCount[rank] + " (" + handToCount[rank] / sum * 100 + "%)"
            )
        }
    }

    var handToCount = IntArray(12)
    fun get5HandsFrom7(nrs: IntArray): Array<IntArray?> {
        val result = arrayOfNulls<IntArray>(21)
        var index = 0
        for (x1 in 0..6) {
            for (x2 in x1 + 1..6) {
                for (x3 in x2 + 1..6) {
                    for (x4 in x3 + 1..6) {
                        for (x5 in x4 + 1..6) {
                            result[index] =
                                intArrayOf(nrs[x1], nrs[x2], nrs[x3], nrs[x4], nrs[x5])
                            index++
                        }
                    }
                }
            }
        }
        return result
    }

    private fun count7() {
        val nr = IntArray(7)
        val suit = IntArray(7)
        for (card1 in 0..51) {
            suit[0] = card1 % 4
            nr[0] = card1 / 4
            for (card2 in card1 + 1..51) {
                suit[1] = card2 % 4
                nr[1] = card2 / 4
                for (card3 in card2 + 1..51) {
                    suit[2] = card3 % 4
                    nr[2] = card3 / 4
                    for (card4 in card3 + 1..51) {
                        suit[3] = card4 % 4
                        nr[3] = card4 / 4
                        for (card5 in card4 + 1..51) {
                            suit[4] = card5 % 4
                            nr[4] = card5 / 4
                            for (card6 in card5 + 1..51) {
                                suit[5] = card6 % 4
                                nr[5] = card6 / 4
                                for (card7 in card6 + 1..51) {
                                    suit[6] = card7 % 4
                                    nr[6] = card7 / 4
                                    val rank = rankPokerHand7(nr, suit) shr 26
                                    handToCount[rank]++
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * cards[0] = player 1 first hole card (0..12)
     * suits[0] = player 1 first hole suit (0..3)
     * cards[1] = player 1 second hole card (0..12)
     * suits[1] = player 1 second hole suit (0..3)
     * cards[i>=2, i<=6] = card on flop/turn/river (0..12) or -1 if not dealt yet
     * cards[i>=2, i<=6] = suit on flop/turn/river (0..12) or -1 if not dealt yet
     * cards[7] = player 2 first hole card (0..12) or -1 if unknown
     * suits[7] = player 2 first hole suit (0..3) or -1 if unknown
     * cards[8] = player 2 second hole card (0..12) or -1 if unknown
     * suits[8] = player 2 second hole suit (0..3) or -1 if unknown
     *
     * for any i (0<=i<=8): if cards[i]==-1 then it must be that suits[i]==-1
     */
    fun rangeVsRange(cards: IntArray, suits: IntArray): RangeResult {
        val rangeResult = RangeResult()
        val nr = IntArray(7)
        val suit = IntArray(7)
        for (i in 0..6) {
            nr[i] = -1
            suit[i] = -1
        }
        var newNr: Int
        var newSuit: Int
        var card1 = 0
        while (card1 < 52 || cards[0] != -1) {
            newNr = if (cards[0] != -1) cards[0] else card1 / 4
            newSuit = if (suits[0] != -1) suits[0] else card1 % 4
            if (cards[0] == -1 && duplicate(newNr, newSuit, nr, suit)) {
                if (cards[0] != -1) {
                    break
                }
                card1++
                continue
            }
            nr[0] = newNr
            val keepCard1 = newNr
            suit[0] = newSuit
            val keepSuit1 = newSuit
            var card2 = 0
            while (card2 < 52 || cards[1] != -1) {
                if (cards[1] == -1 && cards[0] == -1 && card2 <= card1) {
                    card2++
                    continue
                }
                newNr = if (cards[1] != -1) cards[1] else card2 / 4
                newSuit = if (suits[1] != -1) suits[1] else card2 % 4
                if (duplicate(
                        newNr,
                        newSuit,
                        nr,
                        suit
                    ) || cards[1] == -1 && duplicate(newNr, newSuit, cards, suits)
                ) {
                    if (cards[1] != -1) {
                        break
                    }
                    card2++
                    continue
                }
                nr[1] = newNr
                val keepCard2 = newNr
                suit[1] = newSuit
                val keepSuit2 = newSuit
                var card3 = 0
                while (card3 < 52 || cards[2] != -1) {
                    newNr = if (cards[2] != -1) cards[2] else card3 / 4
                    newSuit = if (suits[2] != -1) suits[2] else card3 % 4
                    if (duplicate(
                            newNr,
                            newSuit,
                            nr,
                            suit
                        ) || cards[2] == -1 && duplicate(newNr, newSuit, cards, suits)
                    ) {
                        if (cards[2] != -1) {
                            break
                        }
                        card3++
                        continue
                    }
                    nr[2] = newNr
                    suit[2] = newSuit
                    var card4 = 0
                    while (card4 < 52 || cards[3] != -1) {
                        if (cards[3] == -1 && cards[2] == -1 && card4 <= card3) {
                            card4++
                            continue
                        }
                        newNr = if (cards[3] != -1) cards[3] else card4 / 4
                        newSuit = if (suits[3] != -1) suits[3] else card4 % 4
                        if (duplicate(
                                newNr,
                                newSuit,
                                nr,
                                suit
                            ) || cards[3] == -1 && duplicate(newNr, newSuit, cards, suits)
                        ) {
                            if (cards[3] != -1) {
                                break
                            }
                            card4++
                            continue
                        }
                        nr[3] = newNr
                        suit[3] = newSuit
                        var card5 = 0
                        while (card5 < 52 || cards[4] != -1) {
                            if (cards[4] == -1 && cards[3] == -1 && card5 <= card4) {
                                card5++
                                continue
                            }
                            newNr = if (cards[4] != -1) cards[4] else card5 / 4
                            newSuit = if (suits[4] != -1) suits[4] else card5 % 4
                            if (duplicate(
                                    newNr,
                                    newSuit,
                                    nr,
                                    suit
                                ) || cards[4] == -1 && duplicate(newNr, newSuit, cards, suits)
                            ) {
                                if (cards[4] != -1) {
                                    break
                                }
                                card5++
                                continue
                            }
                            nr[4] = newNr
                            suit[4] = newSuit
                            var card6 = 0
                            while (card6 < 52 || cards[5] != -1) {
                                if (cards[5] == -1 && cards[4] == -1 && card6 <= card5) {
                                    card6++
                                    continue
                                }
                                newNr = if (cards[5] != -1) cards[5] else card6 / 4
                                newSuit = if (suits[5] != -1) suits[5] else card6 % 4
                                if (duplicate(
                                        newNr,
                                        newSuit,
                                        nr,
                                        suit
                                    ) || cards[5] == -1 && duplicate(
                                        newNr,
                                        newSuit,
                                        cards,
                                        suits
                                    )
                                ) {
                                    if (cards[5] != -1) {
                                        break
                                    }
                                    card6++
                                    continue
                                }
                                nr[5] = newNr
                                suit[5] = newSuit
                                var card7 = 0
                                while (card7 < 52 || cards[6] != -1) {
                                    if (cards[6] == -1 && cards[5] == -1 && card7 <= card6) {
                                        card7++
                                        continue
                                    }
                                    newNr = if (cards[6] != -1) cards[6] else card7 / 4
                                    newSuit = if (suits[6] != -1) suits[6] else card7 % 4
                                    if (duplicate(
                                            newNr,
                                            newSuit,
                                            nr,
                                            suit
                                        ) || cards[6] == -1 && duplicate(
                                            newNr,
                                            newSuit,
                                            cards,
                                            suits
                                        )
                                    ) {
                                        if (cards[6] != -1) {
                                            break
                                        }
                                        card7++
                                        continue
                                    }
                                    nr[6] = newNr
                                    suit[6] = newSuit
                                    val score1 = rankPokerHand7(nr, suit)
                                    var card8 = 0
                                    while (card8 < 52 || cards[7] != -1) {
                                        newNr = if (cards[7] != -1) cards[7] else card8 / 4
                                        newSuit = if (suits[7] != -1) suits[7] else card8 % 4
                                        if (duplicate(
                                                newNr,
                                                newSuit,
                                                nr,
                                                suit
                                            ) || cards[7] == -1 && duplicate(
                                                newNr,
                                                newSuit,
                                                cards,
                                                suits
                                            )
                                        ) {
                                            if (cards[7] != -1) {
                                                break
                                            }
                                            card8++
                                            continue
                                        }
                                        nr[0] = newNr
                                        suit[0] = newSuit
                                        var card9 = 0
                                        while (card9 < 52 || cards[8] != -1) {
                                            if (cards[8] == -1 && cards[7] == -1 && card9 <= card8) {
                                                card9++
                                                continue
                                            }
                                            newNr = if (cards[8] != -1) cards[8] else card9 / 4
                                            newSuit = if (suits[8] != -1) suits[8] else card9 % 4
                                            if (newNr == keepCard1 && newSuit == keepSuit1 || duplicate(
                                                    newNr,
                                                    newSuit,
                                                    nr,
                                                    suit
                                                ) || cards[8] == -1 && duplicate(
                                                    newNr,
                                                    newSuit,
                                                    cards,
                                                    suits
                                                )
                                            ) {
                                                if (cards[8] != -1) {
                                                    break
                                                }
                                                card9++
                                                continue
                                            }
                                            nr[1] = newNr
                                            suit[1] = newSuit
                                            val score2 = rankPokerHand7(nr, suit)
                                            rangeResult.process(score1, score2)
                                            nr[1] = keepCard2
                                            suit[1] = keepSuit2
                                            if (cards[8] != -1) {
                                                break
                                            }
                                            card9++
                                        }
                                        nr[0] = keepCard1
                                        suit[0] = keepSuit1
                                        if (cards[7] != -1) {
                                            break
                                        }
                                        card8++
                                    }
                                    if (cards[6] != -1) {
                                        break
                                    }
                                    card7++
                                }
                                nr[6] = -1
                                suit[6] = -1
                                if (cards[5] != -1) {
                                    break
                                }
                                card6++
                            }
                            nr[5] = -1
                            suit[5] = -1
                            if (cards[4] != -1) {
                                break
                            }
                            card5++
                        }
                        nr[4] = -1
                        suit[4] = -1
                        if (cards[3] != -1) {
                            break
                        }
                        card4++
                    }
                    nr[3] = -1
                    suit[3] = -1
                    if (cards[2] != -1) {
                        break
                    }
                    card3++
                }
                nr[2] = -1
                suit[2] = -1
                if (cards[1] != -1) {
                    break
                }
                card2++
            }
            nr[1] = -1
            suit[1] = -1
            if (cards[0] != -1) {
                break
            }
            card1++
        }
        return rangeResult
    }

    private fun duplicate(card: Int, suit: Int, cards: IntArray, suits: IntArray): Boolean {
        for (i in cards.indices) {
            if (card == cards[i] && suit == suits[i]) {
                return true
            }
        }
        return false
    }

    enum class Combination(private val rank: Int) {
        ROYAL_FLUSH(11),
        STRAIGHT_FLUSH(10),
        SKIP_STRAIGHT_FLUSH_ACE_LOW_TMP(9),
        FOUR_OF_A_KIND(8),
        FULL_HOUSE(7),
        FLUSH(6),
        STRAIGHT(5),
        SKIP_STRAIGHT_ACE_LOW_TMP(4),
        THREE_OF_A_KIND(3),
        TWO_PAIR(2),
        PAIR(1),
        HIGH_CARD(0);

        companion object {
            private val fromRank = HashMap<Int, Combination>()

            init {
                for (combination in values()) {
                    fromRank[combination.rank()] = combination
                }
            }

            fun fromRank(rank: Int): Combination {
                return fromRank[rank]!!
            }
        }

        fun rank(): Int {
            return rank
        }

        val displayName get() = name.toLowerCase().replace('_', ' ')

    }

    class RangeResult {
        var win: Long = 0
        var draw: Long = 0
        var loss: Long = 0
        fun process(score1: Int, score2: Int) {
            when {
                score1 > score2 -> win++
                score1 < score2 -> loss++
                else -> draw++
            }
        }

        override fun toString(): String {
            return "win: $win; loss: $loss; draw: $draw"
        }
    }
}
