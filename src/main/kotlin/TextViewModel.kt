import androidx.compose.runtime.*
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.input.key.*
import components.Cursor
import components.TextProvider
import java.util.stream.Collectors

class TextViewModel {
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
                else -> textProvider.insertCharacter(event.utf16CodePoint.toChar())
            }
        }

        return true
    }


}