package components.providers

import components.text.Cursor

interface TextProvider {
    val text: String
    var cursor: Cursor

    fun backspace()
    fun delete()
    fun newline()
    fun insert(ch: Char) {
        insert(ch.toString())
    }
    fun insert(str: String)
    fun changeCursorPositionDirectionLeft()
    fun changeCursorPositionDirectionRight()
    fun changeCursorPositionDirectionUp()
    fun changeCursorPositionDirectionDown()
}