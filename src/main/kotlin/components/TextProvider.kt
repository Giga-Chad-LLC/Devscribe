package components

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.input.key.Key
import java.util.stream.Collectors

class TextProvider {
    private val textLines = mutableListOf("")

    val text: String
        get() {
            // joining lines with newline symbol
            return textLines.toList().stream().collect(Collectors.joining(System.lineSeparator()))
        }

    var cursor by mutableStateOf(Cursor(0, 0, 0))
        private set


    private data class CurrentCursorLineChunks(val beforeCursor: String, val afterCursor: String)

    private fun splitCurrentCursorLine(): CurrentCursorLineChunks {
        val currentLine = textLines[cursor.lineNumber]

        val currentLineBeforeCursor = if (currentLine.isEmpty())
            "" else currentLine.substring(IntRange(0, cursor.currentLineOffset - 1))

        val currentLineAfterCursor = if (currentLine.isEmpty())
            "" else currentLine.substring(cursor.currentLineOffset)

        return CurrentCursorLineChunks(currentLineBeforeCursor, currentLineAfterCursor)
    }

    fun backspaceCursoredSymbol() {
        val currentCursorLineChunks = splitCurrentCursorLine()

        if (currentCursorLineChunks.beforeCursor.isEmpty() && cursor.lineNumber > 0) {
            // move the current line after cursor to the previous line
            textLines.removeAt(cursor.lineNumber)

            val prevLineLength = textLines[cursor.lineNumber - 1].length
            textLines[cursor.lineNumber - 1] += currentCursorLineChunks.afterCursor

            cursor = cursor.run {
                val newOffset = offset - System.lineSeparator().length
                val newlineNumber = lineNumber - 1
                val newCurrentLineOffset = prevLineLength

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

    fun moveToNewline() {
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

    private fun isPrintable(ch: Char): Boolean {
        return !ch.isISOControl() && !ch.isIdentifierIgnorable() && ch.isDefined()
    }

    fun insertCharacter(ch: Char) {
        if (isPrintable(ch)) {
            println(textLines.stream().map { s -> "'$s'" }.collect(Collectors.joining(",", "[", "]")))
            println(cursor)
            val currentCursorLineChunks = splitCurrentCursorLine()
            textLines[cursor.lineNumber] = currentCursorLineChunks.beforeCursor + ch + currentCursorLineChunks.afterCursor

            cursor = cursor.run {
                val newOffset = offset + 1
                val newCurrentLineOffset = currentLineOffset + 1

                Cursor(newOffset, lineNumber, newCurrentLineOffset)
            }
        }
        else {
            println("Provided character '${ch.code}' is non-printable")
        }
    }

    @OptIn(ExperimentalComposeUiApi::class)
    fun changeCursorPosition(pressedArrow: Key) {
        assert(pressedArrow == Key.DirectionUp ||
                pressedArrow == Key.DirectionRight ||
                pressedArrow == Key.DirectionDown ||
                pressedArrow == Key.DirectionLeft)

        when (pressedArrow) {
            Key.DirectionUp -> changeCursorPositionDirectionUp()
            Key.DirectionRight -> changeCursorPositionDirectionRight()
            Key.DirectionDown -> changeCursorPositionDirectionDown()
            Key.DirectionLeft -> changeCursorPositionDirectionLeft()
        }
    }

    private fun changeCursorPositionDirectionLeft() {
        cursor = cursor.run {
            val newOffset: Int
            val newLineNumber: Int
            val newCurrentLineOffset: Int

            if (currentLineOffset == 0 && lineNumber > 0) {
                // moving up to the beginning of the above line
                newOffset = offset - System.lineSeparator().length
                newLineNumber = lineNumber - 1
                newCurrentLineOffset = textLines[lineNumber - 1].length
            }
            else if (currentLineOffset > 0) {
                newOffset = offset - 1
                newLineNumber = lineNumber
                newCurrentLineOffset = currentLineOffset - 1
                // moving one symbol left
            }
            else {
                // staying at offset 0
                newOffset = offset
                newLineNumber = lineNumber
                newCurrentLineOffset = currentLineOffset
            }

            Cursor(newOffset, newLineNumber, newCurrentLineOffset)
        }
    }

    private fun changeCursorPositionDirectionRight() {
        cursor = cursor.run {
            val newOffset: Int
            val newLineNumber: Int
            val newCurrentLineOffset: Int

            if (currentLineOffset == textLines[lineNumber].length && lineNumber + 1 < textLines.size) {
                // if cursor is at the end of current line and there exist next line
                // then move to the next line start
                newOffset = offset + System.lineSeparator().length
                newLineNumber = lineNumber + 1
                newCurrentLineOffset = 0
            }
            else if (currentLineOffset < textLines[lineNumber].length) {
                // moving cursor to the next char
                newOffset = offset + 1
                newLineNumber = lineNumber
                newCurrentLineOffset = currentLineOffset + 1
            }
            else {
                // standing on the last position of the last line
                newOffset = offset
                newLineNumber = lineNumber
                newCurrentLineOffset = currentLineOffset
            }

            Cursor(newOffset, newLineNumber, newCurrentLineOffset)
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

    private fun changeCursorPositionDirectionDown() {
        cursor = cursor.run {
            val newOffset: Int
            val newLineNumber: Int
            val newCurrentLineOffset: Int

            if (lineNumber + 1 < textLines.size) {
                newLineNumber = lineNumber + 1
                val newLineLength = textLines[lineNumber + 1].length

                newCurrentLineOffset = if (currentLineOffset > newLineLength) {
                    // move to the end of new line
                    newLineLength
                } else {
                    // preserve current line offset
                    currentLineOffset
                }

                newOffset = offset + (textLines[lineNumber].length - currentLineOffset) +
                        newCurrentLineOffset + System.lineSeparator().length
            }
            else {
                // staying at the last line and placing cursor at the end position
                newOffset = offset + (textLines[lineNumber].length - currentLineOffset);
                newLineNumber = lineNumber
                newCurrentLineOffset = textLines[lineNumber].length
            }

            Cursor(newOffset, newLineNumber, newCurrentLineOffset)
        }
    }
}