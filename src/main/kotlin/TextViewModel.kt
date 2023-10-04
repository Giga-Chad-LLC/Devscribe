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
                Key.Backspace -> backspaceCursoredSymbol()
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

    private data class CurrentCursorLineChunks(val beforeCursor: String, val afterCursor: String)
    private fun splitCurrentCursorLine(): CurrentCursorLineChunks {
        val currentLine = textLines[cursor.lineNumber]

        val currentLineBeforeCursor = if (currentLine.isEmpty())
            "" else currentLine.substring(IntRange(0, cursor.currentLineOffset - 1))

        val currentLineAfterCursor = if (currentLine.isEmpty())
            "" else currentLine.substring(cursor.currentLineOffset)

        return CurrentCursorLineChunks(currentLineBeforeCursor, currentLineAfterCursor)
    }

    private fun backspaceCursoredSymbol() {
        val currentCursorLineChunks = splitCurrentCursorLine()

        if (currentCursorLineChunks.beforeCursor.isEmpty() && cursor.lineNumber > 0) {
            // move the current line after cursor to the previous line
            textLines.removeAt(cursor.lineNumber)
            textLines[cursor.lineNumber - 1] += currentCursorLineChunks.afterCursor

            cursor = cursor.run {
                val newOffset = offset - System.lineSeparator().length
                val newlineNumber = lineNumber - 1
                val newCurrentLineOffset = textLines[lineNumber - 1].length

                Cursor(newOffset, newlineNumber, newCurrentLineOffset)
            }
        }
        else if (currentCursorLineChunks.beforeCursor.isNotEmpty()) {
            // removing symbol at cursor position (i.e. last symbol )
            textLines[cursor.lineNumber] =
                currentCursorLineChunks.beforeCursor.dropLast(1) + currentCursorLineChunks.afterCursor

            cursor = cursor.run {
                val newOffset = offset - 1
                val newCurrentLineOffset = currentLineOffset - 1

                Cursor(newOffset, lineNumber, newCurrentLineOffset)
            }
        }
    }

    private fun moveToNewline() {
        val currentCursorLineChunks = splitCurrentCursorLine()
        textLines[cursor.lineNumber] = currentCursorLineChunks.beforeCursor
        textLines.add(cursor.lineNumber + 1, currentCursorLineChunks.afterCursor)

        cursor = cursor.run {
            val newOffset = offset + System.lineSeparator().length
            val newLineNumber = lineNumber + 1
            val newCurrentLineOffset = 0

            Cursor(newOffset, newLineNumber, newCurrentLineOffset)
        }
    }

    private fun insertCharacter(ch: Char) {
        println(cursor)
        val currentCursorLineChunks = splitCurrentCursorLine()
        textLines[cursor.lineNumber] = currentCursorLineChunks.beforeCursor + ch + currentCursorLineChunks.afterCursor

        cursor = cursor.run {
            val newOffset = offset + 1
            val newCurrentLineOffset = currentLineOffset + 1

            Cursor(newOffset, lineNumber, newCurrentLineOffset)
        }
    }

    @OptIn(ExperimentalComposeUiApi::class)
    private fun changeCursorPosition(pressedArrow: Key) {
        assert(pressedArrow == Key.DirectionUp ||
               pressedArrow == Key.DirectionRight ||
               pressedArrow == Key.DirectionDown ||
                pressedArrow == Key.DirectionLeft)

        when (pressedArrow) {
            Key.DirectionUp -> changeCursorPositionDirectionUp()
            Key.DirectionRight -> {

            }
            Key.DirectionDown -> {

            }
            Key.DirectionLeft -> {

            }
        }
    }


    private fun changeCursorPositionDirectionUp() {
        cursor = cursor.run {
            val newOffset: Int
            val newLineNumber: Int
            var newCurrentLineOffset = currentLineOffset

            if (lineNumber > 0) {
                newLineNumber = lineNumber - 1
                val newLineLength = textLines[lineNumber - 1].length

                if (currentLineOffset > newLineLength) {
                    // current line offset exceeds the length of a new line -> move to the end of new line
                    newOffset = offset - (currentLineOffset + System.lineSeparator().length)
                    newCurrentLineOffset = newLineLength
                }
                else {
                    // preserving current line offset
                    // and reducing total offset by the line length and line separator
                    newOffset = offset - (newLineLength + System.lineSeparator().length)
                }
            }
            else {
                // placing cursor to the start position
                newOffset = 0
                newLineNumber = 0
                newCurrentLineOffset = 0
            }

            Cursor(newOffset, newLineNumber, newCurrentLineOffset)
        }
    }
}