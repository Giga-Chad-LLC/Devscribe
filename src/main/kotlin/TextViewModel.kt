import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.input.key.*
import components.text.Cursor
import components.providers.TextProvider

class TextViewModel {
    /**
     * Consecutive whitespaces are not rendered if there is no basic letter after them.
     * Instead, a special non-breaking space character '\u00A0' is used
     */
    private val nonBreakingSpaceChar = 0xA0.toChar()
    private val textProvider = TextProvider()

    val text: String
        get() {
            return textProvider.text
        }

    val cursor: Cursor
        get() {
            return textProvider.cursor
        }

    private fun isPrintable(ch: Char): Boolean {
        return !ch.isISOControl() && !ch.isIdentifierIgnorable() && ch.isDefined()
    }

    @OptIn(ExperimentalComposeUiApi::class)
    fun processKeyEvent(event: KeyEvent): Boolean {
        if (event.type == KeyEventType.KeyDown) {
            when (event.key) {
                Key.Backspace -> textProvider.backspace()
                Key.Enter -> textProvider.newline()
                Key.DirectionUp -> textProvider.changeCursorPositionDirectionUp()
                Key.DirectionRight -> textProvider.changeCursorPositionDirectionRight()
                Key.DirectionDown -> textProvider.changeCursorPositionDirectionDown()
                Key.DirectionLeft -> textProvider.changeCursorPositionDirectionLeft()
                Key.Spacebar -> textProvider.insert(nonBreakingSpaceChar)
                Key.Delete -> textProvider.delete()
                else -> {
                    val ch: Char = event.utf16CodePoint.toChar()
                    if (isPrintable(ch)) {
                        textProvider.insert(ch)
                    }
                    else {
                        println("Provided unsupported character '${ch.code}' is non-printable")
                    }
                }
            }
        }

        return true
    }


}