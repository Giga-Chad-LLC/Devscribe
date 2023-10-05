import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.input.key.*
import components.Cursor
import components.TextProvider

class TextViewModel {
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

    @OptIn(ExperimentalComposeUiApi::class)
    fun processKeyEvent(event: KeyEvent): Boolean {
        if (event.type == KeyEventType.KeyDown) {
            when (event.key) {
                Key.Backspace -> textProvider.backspaceCursoredSymbol()
                Key.Enter -> textProvider.moveToNewline()
                Key.DirectionUp,
                Key.DirectionRight,
                Key.DirectionDown,
                Key.DirectionLeft -> textProvider.changeCursorPosition(event.key)
                Key.Spacebar -> {
                    /**
                     * Consecutive whitespaces are not rendered if there is no basic letter after them.
                     * Instead, a special non-breaking space character '\u00A0' is used
                     */
                    textProvider.insertCharacter(nonBreakingSpaceChar)
                }
                else -> textProvider.insertCharacter(event.utf16CodePoint.toChar())
            }
        }

        return true
    }


}