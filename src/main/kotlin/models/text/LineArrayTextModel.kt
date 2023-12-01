package models.text

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import common.TextConstants
import java.util.stream.Collectors
import kotlin.math.max

// TODO: keep absolute offset for every line
class LineArrayTextModel : TextModel {
    private var textLines = mutableStateListOf("")

    override val text: String
        get() {
            return linesToText(textLines.toList())
        }

    override var cursor by mutableStateOf(Cursor(0, 0, 0))

    private data class CurrentCursorLineChunks(val beforeCursor: String, val afterCursor: String)

    private fun linesToText(lines: List<String>): String {
        // joining lines with newline symbol
        return lines.stream().map{ it.replace(TextConstants.nonBreakingSpaceChar, ' ') }
            .collect(Collectors.joining(System.lineSeparator()))
    }

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

        /*println(textLines.stream()
            .map { s -> "'${s.replace(TextConstants.nonBreakingSpaceChar, ' ')}'" }
            .collect(Collectors.joining(", ", "[", "]")))*/
        println(cursor)
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
        val chunks = text.replace(' ', TextConstants.nonBreakingSpaceChar).split(System.lineSeparator())
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

            /*println(textLines.stream()
                .map { s -> "'${s.replace(TextConstants.nonBreakingSpaceChar, ' ')}'" }
                .collect(Collectors.joining(", ", "[", "]")))*/
            println(cursor)

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

    override fun install(text: String) {
        val lines = text.replace(' ', TextConstants.nonBreakingSpaceChar).split(System.lineSeparator())
        textLines.clear()

        if (lines.isEmpty()) {
            textLines.add("")
        }
        else {
            for (line in lines) {
                textLines.add(line)
            }
        }

        cursor = Cursor(0, 0, 0)
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
                // moving one symbol left
                newOffset = offset - 1
                newLineNumber = lineNumber
                newCurrentLineOffset = currentLineOffset - 1
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

    private fun calculateShiftToNextWord(chunk: String): Int {
        val delimiters = listOf(
            TextConstants.nonBreakingSpaceChar,
            '.', ',',
            '/', '\\',
            '(', ')',
        )

        val whitespaceCountBeforeFirstWord = chunk
            // dropping letters after a starting sequence of consecutive delimiters
            .dropLast(chunk.length - max(chunk.indexOfFirst { !delimiters.contains(it) }, 0))
            .count()

        val firstWord: String = chunk
            .split(*delimiters.map { ch -> ch.toString() }.toTypedArray())
            .firstOrNull { s -> s.isNotEmpty() } ?: ""

        println("whitespaceCountBeforeFirstWord=${whitespaceCountBeforeFirstWord} " +
                " " +
                "firstWord='${firstWord}'")

        return whitespaceCountBeforeFirstWord + firstWord.length
    }

    override fun forwardToNextWord() {
        val chunk = splitCurrentCursorLine().afterCursor

        cursor = cursor.run {
            val newOffset: Int
            val newLineNumber: Int
            val newCurrentLineOffset: Int

            val shift = calculateShiftToNextWord(chunk)

            if (shift > 0) {
                // moving to the end of first word
                newOffset = offset + shift
                newLineNumber = lineNumber
                newCurrentLineOffset = currentLineOffset + shift
            }
            else if (lineNumber + 1 < textLines.size) {
                // move of the next line
                newOffset = offset + System.lineSeparator().length
                newLineNumber = lineNumber + 1
                newCurrentLineOffset = 0
            }
            else {
                // placing cursor on the end of last line
                newOffset = offset
                newLineNumber = lineNumber
                newCurrentLineOffset = textLines[lineNumber].length
            }

            Cursor(newOffset, newLineNumber, newCurrentLineOffset)
        }
    }

    override fun backwardToPreviousWord() {
        val chunk = splitCurrentCursorLine().beforeCursor

        cursor = cursor.run {
            val newOffset: Int
            val newLineNumber: Int
            val newCurrentLineOffset: Int

            val shift = calculateShiftToNextWord(chunk.reversed())
            println("chunk='${chunk}', shift=${shift}")


            if (shift > 0) {
                // moving to the end of first word
                newOffset = offset - shift + 1
                newLineNumber = lineNumber
                newCurrentLineOffset = currentLineOffset - shift + 1
            }
            else if (lineNumber > 0) {
                // move of the next line
                newOffset = offset - System.lineSeparator().length
                newLineNumber = lineNumber - 1
                newCurrentLineOffset = textLines[newLineNumber].length
            }
            else {
                // placing cursor on the start of first line
                newOffset = offset
                newLineNumber = lineNumber
                newCurrentLineOffset = 0
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

    private fun checkLineIndex(lineIndex: Int) {
        if (!(0 <= lineIndex && lineIndex < textLines.size)) {
            throw IllegalArgumentException("Line index must be in range [0; ${textLines.size}), got $lineIndex")
        }
    }

    override fun changeCursorPosition(lineIndex: Int, lineOffset: Int) {
        checkLineIndex(lineIndex)

        val cursoredLine = textLines[lineIndex]
        if (!(0 <= lineOffset && lineOffset <= cursoredLine.length)) {
            throw IllegalArgumentException("Line offset must be in range [0; ${cursoredLine.length}], got $lineOffset")
        }

        var absoluteOffset = lineOffset
        for (i in 0 until lineIndex) {
            absoluteOffset += textLines[i].length + System.lineSeparator().length
        }

        cursor = Cursor(absoluteOffset, lineIndex, lineOffset)
    }

    override fun linesCount(): Int {
        return textLines.size
    }

    override fun textInLinesRange(fromIndex: Int, toIndex: Int): String {
        return linesToText(textLines.subList(fromIndex, toIndex))
    }

    override fun lineLength(lineIndex: Int): Int {
        checkLineIndex(lineIndex)
        return textLines[lineIndex].length
    }

    override fun totalOffsetOfLine(lineIndex: Int): Int {
        var offset = 0
        for (i in 0 until lineIndex) {
            offset += textLines[i].length + System.lineSeparator().length
        }
        return offset
    }

    override fun textLines(): List<String> {
        /**
         * Calling toList() insures that Composable components will be able to listen updates of the list
         * using LaunchedEffect()
         */
        return textLines.toList()
    }

    override fun maxLineLength(): Int {
        return textLines.maxOfOrNull { s -> s.length } ?: 0
    }
}