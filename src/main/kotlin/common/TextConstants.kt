package common

import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.type
import androidx.compose.ui.input.key.utf16CodePoint

object TextConstants {
    /**
     * Consecutive whitespaces are not rendered if there is no basic letter after them.
     * Instead, a special non-breaking space character '\u00A0' is used
     */
    const val nonBreakingSpaceChar = 0xA0.toChar()
}

fun isPrintableSymbolAction(event: KeyEvent): Boolean {
    val ch = event.utf16CodePoint.toChar()
    return event.type == KeyEventType.KeyDown && !ch.isISOControl() && !ch.isIdentifierIgnorable() && ch.isDefined()
}