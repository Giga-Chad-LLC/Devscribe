package models.text

import androidx.compose.runtime.snapshots.SnapshotStateList

interface TextModel {
    val textLines: SnapshotStateList<String>
    val text: String
    val cursor: Cursor

    fun backspace()
    fun delete()
    fun newline()
    fun insert(ch: Char) {
        insert(ch.toString())
    }

    /**
     * Inserts text into text model but does not change the cursor position
     */
    fun install(text: String)

    fun insert(text: String)
    fun changeCursorPositionDirectionLeft()
    fun changeCursorPositionDirectionRight()
    fun changeCursorPositionDirectionUp()
    fun changeCursorPositionDirectionDown()

    fun changeCursorPosition(lineIndex: Int, lineOffset: Int)

    fun forwardToNextWord()

    fun backwardToPreviousWord()

    /**
     * Should be overwritten by an implementation because default version works for O(L) where L is the sum of lengths of all strings.
     */
    fun linesCount(): Int {
        return text.split(System.lineSeparator()).size
    }

    fun lineLength(lineIndex: Int): Int

    fun totalOffsetOfLine(lineIndex: Int): Int

    fun textLines(): List<String>

    /**
     * Returns lines in range fromIndex (inclusive) to toIndex (exclusive).
     * The result array contains lines without trailing newline characters
     */
    fun textInLinesRange(fromIndex: Int, toIndex: Int): String

    /**
     * Should be overwritten by an implementation because default version works for O(L) where L is the sum of lengths of all strings.
     */
    fun maxLineLength(): Int {
        return text.split(System.lineSeparator()).maxOfOrNull { s -> s.length } ?: 0
    }
}