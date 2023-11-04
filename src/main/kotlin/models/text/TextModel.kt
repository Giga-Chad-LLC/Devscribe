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

    fun linesCount(): Int
}