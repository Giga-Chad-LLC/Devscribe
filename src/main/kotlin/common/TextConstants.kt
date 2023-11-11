package common

object TextConstants {
    /**
     * Consecutive whitespaces are not rendered if there is no basic letter after them.
     * Instead, a special non-breaking space character '\u00A0' is used
     */
    const val nonBreakingSpaceChar = 0xA0.toChar()
}