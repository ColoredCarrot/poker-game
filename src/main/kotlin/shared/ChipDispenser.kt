package shared

class ChipDispenser private constructor(private val sortedAvailableChips: List<Int>) {

    val largestChip = sortedAvailableChips.last()
    val smallestChip = sortedAvailableChips.first()

    fun getNextSmallerChip(smallerThan: Int): Int {
        // Binary search for smallerThan
        val idx = modifiedBinarySearch(sortedAvailableChips, smallerThan - 1)
        if (idx < 0) error("No chip smaller than $smallerThan")
        return sortedAvailableChips[idx]
    }

    companion object {
        val DEFAULT = ChipDispenser(listOf(1, 5, 20, 50, 250, 1000, 5000))

        private fun modifiedBinarySearch(a: List<Int>, x: Int, l: Int = 0, h: Int = a.size): Int {
            // Invalid range
            if (l > h) {
                return -1
            }

            // Empty range
            if (l == h) {
                return if (l > 0) l - 1 else -1
            }

            // Calculate middle index
            val m = l + (h - 1 - l) / 2

            return when {
                x > a[m] -> modifiedBinarySearch(a, x, m + 1, h)
                x < a[m] -> modifiedBinarySearch(a, x, l, m)
                else -> m
            }
        }
    }

}
