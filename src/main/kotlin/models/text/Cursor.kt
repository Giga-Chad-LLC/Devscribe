package models.text

data class Cursor(val offset: Int, val lineNumber: Int, val currentLineOffset: Int) {
    constructor(cursor: Cursor) : this(cursor.offset, cursor.lineNumber, cursor.currentLineOffset)
}
