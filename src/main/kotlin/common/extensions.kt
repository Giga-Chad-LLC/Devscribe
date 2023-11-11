package common


/**
 * Finds an element whose index is cyclically before by k the passed index.
 */
fun <T> List<T>.kthBeforeIndex(index: Int, k: Int): T {
    require(index in indices) { "Invalid index $index" }
    require(0 < k) { "k must be positive, got $k" }
    require(k < size) { "k cannot exceed size of $size, got $k" }

    val nextIndex = (index - k + size) % size
    return this[nextIndex]
}