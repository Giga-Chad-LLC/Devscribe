package models.text

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import common.TextConstants
import java.util.stream.Collectors

class LineArrayTextModel : TextModel {
    private val textLines = mutableStateListOf("")

    override var text: String
        get() {
            // joining lines with newline symbol
            return textLines.toList().stream().map{ it.replace(TextConstants.nonBreakingSpaceChar, ' ') }.collect(Collectors.joining(System.lineSeparator()))
        }
        set(data) {
            cursor = Cursor(0, 0, 0)
            textLines.clear()
            textLines.add("")
            insert(data.replace(' ', TextConstants.nonBreakingSpaceChar))
        }

    override var cursor by mutableStateOf(Cursor(0, 0, 0))

    private data class CurrentCursorLineChunks(val beforeCursor: String, val afterCursor: String)

    private fun splitCurrentCursorLine(): CurrentCursorLineChunks {
        val currentLine = textLines[cursor.lineNumber]

        val currentLineBeforeCursor = if (currentLine.isEmpty())
            "" else currentLine.substring(IntRange(0, cursor.currentLineOffset - 1))

        val currentLineAfterCursor = if (currentLine.isEmpty())
            "" else currentLine.substring(cursor.currentLineOffset)

        return CurrentCursorLineChunks(currentLineBeforeCursor, currentLineAfterCursor)
    }

    override fun backspace() {
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
            // removing symbol at cursor position (i.e. last symbol)
            textLines[cursor.lineNumber] =
                currentCursorLineChunks.beforeCursor.dropLast(1) + currentCursorLineChunks.afterCursor

            cursor = cursor.run {
                val newOffset = offset - 1
                val newCurrentLineOffset = currentLineOffset - 1

                Cursor(newOffset, lineNumber, newCurrentLineOffset)
            }
        }
    }

    override fun delete() {
        val currentCursorLineChunks = splitCurrentCursorLine()

        if (currentCursorLineChunks.afterCursor.isEmpty() && cursor.lineNumber + 1 < textLines.size) {
            // staying on the end of current line and current line is not the last one
            // move the join the next line with the current line (i.e. delete newline)
            val nextLine: String = textLines.removeAt(cursor.lineNumber + 1)

            textLines[cursor.lineNumber] = textLines[cursor.lineNumber] + nextLine
        }
        else if (currentCursorLineChunks.afterCursor.isNotEmpty()) {
            // staying on the non-ending position of current line
            // delete 1st symbol after the cursor
            textLines[cursor.lineNumber] =
                currentCursorLineChunks.beforeCursor + currentCursorLineChunks.afterCursor.substring(1)
        }

//        println(textLines.stream()
//            .map { s -> "'${s.replace(TextConstants.nonBreakingSpaceChar, ' ')}'" }
//            .collect(Collectors.joining(", ", "[", "]")))
//        println(cursor)
    }

    override fun newline() {
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

    override fun insert(text: String) {
        val chunks = text.split(System.lineSeparator())
        val textEndsWithLineSeparator = text.endsWith(System.lineSeparator())

        for (index in chunks.indices) {
            val line = chunks[index]

            val currentCursorLineChunks = splitCurrentCursorLine()
            textLines[cursor.lineNumber] = currentCursorLineChunks.beforeCursor + line + currentCursorLineChunks.afterCursor

            cursor = cursor.run {
                val newOffset = offset + line.length
                val newCurrentLineOffset = currentLineOffset + line.length

                Cursor(newOffset, lineNumber, newCurrentLineOffset)
            }

//            println(textLines.stream()
//                .map { s -> "'${s.replace(TextConstants.nonBreakingSpaceChar, ' ')}'" }
//                .collect(Collectors.joining(", ", "[", "]")))
//            println(cursor)

            /**
             * If current line is the last one, and it does not end with line separator
             * then it does not need the line separator to be inserted.
             *
             * Otherwise, insert line separator
             */
            if (!(index + 1 == chunks.size && !textEndsWithLineSeparator)) {
                newline()
            }
        }
    }

    override fun changeCursorPositionDirectionLeft() {
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

    override fun changeCursorPositionDirectionRight() {
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

    override fun changeCursorPositionDirectionUp() {
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

    override fun changeCursorPositionDirectionDown() {
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