package viewmodels

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.input.key.*
import models.text.Cursor
import models.text.LineArrayTextModel
import models.text.TextModel

class TextViewModel {
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

    private fun isPrintable(ch: Char): Boolean {
        return !ch.isISOControl() && !ch.isIdentifierIgnorable() && ch.isDefined()
    }

    @OptIn(ExperimentalComposeUiApi::class)
    fun processKeyEvent(event: KeyEvent): Boolean {
        if (event.type == KeyEventType.KeyDown) {
            when (event.key) {
                Key.Backspace -> textModel.backspace()
                Key.Enter -> textModel.newline()
                Key.DirectionUp -> textModel.changeCursorPositionDirectionUp()
                Key.DirectionRight -> textModel.changeCursorPositionDirectionRight()
                Key.DirectionDown -> textModel.changeCursorPositionDirectionDown()
                Key.DirectionLeft -> textModel.changeCursorPositionDirectionLeft()
                Key.Spacebar -> textModel.insert(nonBreakingSpaceChar)
                Key.Delete -> textModel.delete()
                else -> {
                    val ch: Char = event.utf16CodePoint.toChar()
                    if (isPrintable(ch)) {
                        textModel.insert(ch)
                    }
                    else {
                        println("Provided unsupported character '${ch.code}' is non-printable")
                    }
                }
            }
        }

        return true
    }


}