import androidx.compose.runtime.*
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.input.key.*
import components.Cursor
import java.util.stream.Collectors

class TextViewModel {
    private var textLines = mutableStateListOf("")
    val text: String
        get() {
            // joining lines with newline symbol
            return textLines.toList().stream().collect(Collectors.joining(System.lineSeparator()))
        }

    var cursor by mutableStateOf(Cursor(0, 0, 0))
        private set

    @OptIn(ExperimentalComposeUiApi::class)
    fun processKeyEvent(event: KeyEvent): Boolean {
        println(textLines.toList())

        if (event.type == KeyEventType.KeyDown) {
            when (event.key) {
                Key.Backspace -> removeLastSymbol()
                Key.Enter -> moveToNewline()
                Key.DirectionUp,
                Key.DirectionRight,
                Key.DirectionDown,
                Key.DirectionLeft -> changeCursorPosition(event.key)
                else -> insertCharacter(event.utf16CodePoint.toChar())
            }
        }

        return true
    }

    private fun removeLastSymbol() {
        if (textLines.last().isEmpty() && textLines.size > 1) {
            // removing last line -> remove newline
            textLines.removeLast()

            cursor.apply {
                offset -= System.lineSeparator().length
                lineNumber -= 1
                currentLineOffset = textLines[lineNumber].length - 1
            }
        }
        else if (textLines.last().isNotEmpty()) {
            // removing general symbol
            textLines[textLines.size - 1] = textLines.last().dropLast(1)

            cursor.apply {
                offset -= 1
                currentLineOffset -= 1
            }
        }
    }

    private fun moveToNewline() {
        // adding newline
        textLines.add("")

        cursor.apply {
            offset += System.lineSeparator().length
            lineNumber += 1
            currentLineOffset = 0
        }
    }

    private fun insertCharacter(ch: Char) {
        textLines[textLines.size - 1] = textLines.last() + ch

        cursor.apply {
            offset += 1
            currentLineOffset += 1
        }
    }

    @OptIn(ExperimentalComposeUiApi::class)
    private fun changeCursorPosition(pressedArrow: Key) {
        assert(pressedArrow == Key.DirectionUp ||
               pressedArrow == Key.DirectionRight ||
               pressedArrow == Key.DirectionDown ||
                pressedArrow == Key.DirectionLeft)

        when (pressedArrow) {
            Key.DirectionUp -> {
                // shifting back to the beginning of current line

                cursor.offset

                /*if (caret.lineNumber > 0) {
                    --caret.lineNumber
                }

                assert(caret.lineNumber < textLines.size)

                if (caret.position > textLines[caret.lineNumber].length) {
                    caret.position = textLines[caret.lineNumber].length
                }*/
            }
            Key.DirectionRight -> {

            }
            Key.DirectionDown -> {

            }
            Key.DirectionLeft -> {

            }
        }
    }
}