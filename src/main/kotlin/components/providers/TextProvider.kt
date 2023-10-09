package components.providers

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import components.text.Cursor
import java.util.stream.Collectors

class TextProvider {
    private val textLines = mutableStateListOf("")

    val text: String
        get() {
            // joining lines with newline symbol
            return textLines.toList().stream().collect(Collectors.joining(System.lineSeparator()))
        }

    var cursor by mutableStateOf(Cursor(0, 0, 0))
        private set


    private data class CurrentCursorLineChunks(val beforeCursor: String, val afterCursor: String)

    private fun splitCurrentCursorLine(): CurrentCursorLineChunks {
        val currentLine = textLines[cursor.lineIndex]

        val currentLineBeforeCursor = if (currentLine.isEmpty())
            "" else currentLine.substring(IntRange(0, cursor.currentLineOffset - 1))

        val currentLineAfterCursor = if (currentLine.isEmpty())
            "" else currentLine.substring(cursor.currentLineOffset)

        return CurrentCursorLineChunks(currentLineBeforeCursor, currentLineAfterCursor)
    }

    fun backspace() {
        val currentCursorLineChunks = splitCurrentCursorLine()

        if (currentCursorLineChunks.beforeCursor.isEmpty() && cursor.lineIndex > 0) {
            // move the current line after cursor to the previous line
            textLines.removeAt(cursor.lineIndex)

            val prevLineLength = textLines[cursor.lineIndex - 1].length
            textLines[cursor.lineIndex - 1] += currentCursorLineChunks.afterCursor

            cursor = cursor.run {
                val newOffset = offset - System.lineSeparator().length
                val newlineIndex = lineIndex - 1
                val newCurrentLineOffset = prevLineLength

                Cursor(newOffset, newlineIndex, newCurrentLineOffset)
            }
        }
        else if (currentCursorLineChunks.beforeCursor.isNotEmpty()) {
            // removing symbol at cursor position (i.e. last symbol)
            textLines[cursor.lineIndex] =
                currentCursorLineChunks.beforeCursor.dropLast(1) + currentCursorLineChunks.afterCursor

            cursor = cursor.run {
                val newOffset = offset - 1
                val newCurrentLineOffset = currentLineOffset - 1

                Cursor(newOffset, lineIndex, newCurrentLineOffset)
            }
        }
    }

    fun delete() {
        val currentCursorLineChunks = splitCurrentCursorLine()

        if (currentCursorLineChunks.afterCursor.isEmpty() && cursor.lineIndex + 1 < textLines.size) {
            // staying on the end of current line and current line is not the last one
            // move the join the next line with the current line (i.e. delete newline)
            val nextLine: String = textLines.removeAt(cursor.lineIndex + 1)

            textLines[cursor.lineIndex] = textLines[cursor.lineIndex] + nextLine
        }
        else if (currentCursorLineChunks.afterCursor.isNotEmpty()) {
            // staying on the non-ending position of current line
            // delete 1st symbol after the cursor
            textLines[cursor.lineIndex] =
                currentCursorLineChunks.beforeCursor + currentCursorLineChunks.afterCursor.substring(1)
        }

        println(textLines.stream().map { s -> "'$s'" }.collect(Collectors.joining(",", "[", "]")))
        println(cursor)
    }

    fun newline() {
        val currentCursorLineChunks = splitCurrentCursorLine()
        textLines[cursor.lineIndex] = currentCursorLineChunks.beforeCursor
        textLines.add(cursor.lineIndex + 1, currentCursorLineChunks.afterCursor)

        cursor = cursor.run {
            val newOffset = offset + System.lineSeparator().length
            val newLineIndex = lineIndex + 1
            val newCurrentLineOffset = 0

            Cursor(newOffset, newLineIndex, newCurrentLineOffset)
        }
    }

    fun insert(ch: Char) {
        insert(ch.toString())
    }

    fun insert(str: String) {
        val currentCursorLineChunks = splitCurrentCursorLine()
        textLines[cursor.lineIndex] = currentCursorLineChunks.beforeCursor + str + currentCursorLineChunks.afterCursor

        cursor = cursor.run {
            val newOffset = offset + str.length
            val newCurrentLineOffset = currentLineOffset + str.length

            Cursor(newOffset, lineIndex, newCurrentLineOffset)
        }

        println(textLines.stream().map { s -> "'$s'" }.collect(Collectors.joining(",", "[", "]")))
        println(cursor)
    }

    fun changeCursorPositionDirectionLeft() {
        cursor = cursor.run {
            val newOffset: Int
            val newLineIndex: Int
            val newCurrentLineOffset: Int

            if (currentLineOffset == 0 && lineIndex > 0) {
                // moving up to the beginning of the above line
                newOffset = offset - System.lineSeparator().length
                newLineIndex = lineIndex - 1
                newCurrentLineOffset = textLines[lineIndex - 1].length
            }
            else if (currentLineOffset > 0) {
                newOffset = offset - 1
                newLineIndex = lineIndex
                newCurrentLineOffset = currentLineOffset - 1
                // moving one symbol left
            }
            else {
                // staying at offset 0
                newOffset = offset
                newLineIndex = lineIndex
                newCurrentLineOffset = currentLineOffset
            }

            Cursor(newOffset, newLineIndex, newCurrentLineOffset)
        }
    }

    fun changeCursorPositionDirectionRight() {
        cursor = cursor.run {
            val newOffset: Int
            val newLineIndex: Int
            val newCurrentLineOffset: Int

            if (currentLineOffset == textLines[lineIndex].length && lineIndex + 1 < textLines.size) {
                // if cursor is at the end of current line and there exist next line
                // then move to the next line start
                newOffset = offset + System.lineSeparator().length
                newLineIndex = lineIndex + 1
                newCurrentLineOffset = 0
            }
            else if (currentLineOffset < textLines[lineIndex].length) {
                // moving cursor to the next char
                newOffset = offset + 1
                newLineIndex = lineIndex
                newCurrentLineOffset = currentLineOffset + 1
            }
            else {
                // standing on the last position of the last line
                newOffset = offset
                newLineIndex = lineIndex
                newCurrentLineOffset = currentLineOffset
            }

            Cursor(newOffset, newLineIndex, newCurrentLineOffset)
        }
    }

    fun changeCursorPositionDirectionUp() {
        cursor = cursor.run {
            val newOffset: Int
            val newLineIndex: Int
            var newCurrentLineOffset = currentLineOffset

            if (lineIndex > 0) {
                newLineIndex = lineIndex - 1
                val newLineLength = textLines[lineIndex - 1].length

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
                newLineIndex = 0
                newCurrentLineOffset = 0
            }

            Cursor(newOffset, newLineIndex, newCurrentLineOffset)
        }
    }

    fun changeCursorPositionDirectionDown() {
        cursor = cursor.run {
            val newOffset: Int
            val newLineIndex: Int
            val newCurrentLineOffset: Int

            if (lineIndex + 1 < textLines.size) {
                newLineIndex = lineIndex + 1
                val newLineLength = textLines[lineIndex + 1].length

                newCurrentLineOffset = if (currentLineOffset > newLineLength) {
                    // move to the end of new line
                    newLineLength
                } else {
                    // preserve current line offset
                    currentLineOffset
                }

                newOffset = offset + (textLines[lineIndex].length - currentLineOffset) +
                        newCurrentLineOffset + System.lineSeparator().length
            }
            else {
                // staying at the last line and placing cursor at the end position
                newOffset = offset + (textLines[lineIndex].length - currentLineOffset);
                newLineIndex = lineIndex
                newCurrentLineOffset = textLines[lineIndex].length
            }

            Cursor(newOffset, newLineIndex, newCurrentLineOffset)
        }
    }
}