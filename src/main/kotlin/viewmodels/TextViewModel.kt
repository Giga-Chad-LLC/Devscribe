package viewmodels

import androidx.compose.ui.input.key.utf16CodePoint
import components.dispatcher.KeyboardEventDispatcher
import components.dispatcher.KeyboardEventDispatcher.KeyboardAction
import models.text.Cursor
import models.text.LineArrayTextModel
import models.text.TextModel

class TextViewModel {
    init {
        val dispatcher = KeyboardEventDispatcher.getInstance()

        dispatcher.subscribe(KeyboardAction.BACKSPACE) { textModel.backspace() }
        dispatcher.subscribe(KeyboardAction.NEWLINE) { textModel.newline() }
        dispatcher.subscribe(KeyboardAction.DIRECTION_UP) { textModel.changeCursorPositionDirectionUp() }
        dispatcher.subscribe(KeyboardAction.DIRECTION_RIGHT) { textModel.changeCursorPositionDirectionRight() }
        dispatcher.subscribe(KeyboardAction.DIRECTION_DOWN) { textModel.changeCursorPositionDirectionDown() }
        dispatcher.subscribe(KeyboardAction.DIRECTION_LEFT) { textModel.changeCursorPositionDirectionLeft() }
        dispatcher.subscribe(KeyboardAction.SPACE) { textModel.insert(nonBreakingSpaceChar) }
        dispatcher.subscribe(KeyboardAction.DELETE) { textModel.delete() }
        dispatcher.subscribe(KeyboardAction.PRINTABLE_SYMBOL) { textModel.insert(it.utf16CodePoint.toChar()) }
    }

    /**
     * Consecutive whitespaces are not rendered if there is no basic letter after them.
     * Instead, a special non-breaking space character '\u00A0' is used
     */
    private val nonBreakingSpaceChar = 0xA0.toChar()
    private val textModel: TextModel = LineArrayTextModel()

    val text: String
        get() {
            return textModel.text
        }

    val cursor: Cursor
        get() {
            return textModel.cursor
        }
}