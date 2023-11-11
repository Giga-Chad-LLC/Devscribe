package models.text

interface TextModel {
    var text: String
    var cursor: Cursor

    fun backspace()
    fun delete()
    fun newline()
    fun insert(ch: Char) {
        insert(ch.toString())
    }
    fun insert(text: String)
    fun changeCursorPositionDirectionLeft()
    fun changeCursorPositionDirectionRight()
    fun changeCursorPositionDirectionUp()
    fun changeCursorPositionDirectionDown()

    fun changeCursorPosition(lineIndex: Int, lineOffset: Int)

    /**
     * Should be overwritten by an implementation because default version works for O(L) where L is the sum of lengths of all strings.
     */
    fun linesCount(): Int {
        return text.split(System.lineSeparator()).size
    }

    fun lineLength(lineIndex: Int): Int

    /**
     * Should be overwritten by an implementation because default version works for O(L) where L is the sum of lengths of all strings.
     */
    fun maxLineLength(): Int {
        return text.split(System.lineSeparator()).maxOfOrNull { s -> s.length } ?: 0
    }
}