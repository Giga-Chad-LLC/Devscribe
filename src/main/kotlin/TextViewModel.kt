import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.input.key.*
import java.util.stream.Collectors

class TextViewModel {
    private var textLines = mutableStateListOf("")

    val text: String
        get() {
            // joining lines with newline symbol
            return textLines.toList().stream().collect(Collectors.joining(System.lineSeparator()))
        }

    @OptIn(ExperimentalComposeUiApi::class)
    fun processKeyEvent(event: KeyEvent): Boolean {
        println(textLines.toList())

        if (event.type == KeyEventType.KeyDown) {
            when (event.key) {
                Key.Backspace -> removeLastSymbol()
                Key.Enter -> moveToNewline()
                else -> insertCharacter(event.utf16CodePoint.toChar())
            }
        }

        return true
    }

    private fun removeLastSymbol() {
        if (textLines.last().isEmpty() && textLines.size > 1) {
            textLines.removeLast()
        }
        else if (textLines.last().isNotEmpty()) {
            textLines[textLines.size - 1] = textLines.last().dropLast(1)
        }
    }

    private fun moveToNewline() {
        textLines.add("")
    }

    private fun insertCharacter(ch: Char) {
        textLines[textLines.size - 1] = textLines.last() + ch
    }
}