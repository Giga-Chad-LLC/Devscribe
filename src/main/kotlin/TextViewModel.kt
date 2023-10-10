import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.input.key.*
import components.text.Cursor
import components.providers.TextProvider
import components.shortcuts.ShortcutsRegistry
import components.text.highlighters.Highlighter
import components.text.highlighters.HighlighterManager
import components.text.highlighters.SelectionHighlighter
import kotlin.math.max
import kotlin.math.min

@OptIn(ExperimentalComposeUiApi::class)
class TextViewModel {
    /**
     * Consecutive whitespaces are not rendered if there is no basic letter after them.
     * Instead, a special non-breaking space character '\u00A0' is used
     */
    private val nonBreakingSpaceChar = 0xA0.toChar()
    private val textProvider = TextProvider()
    private val highlighterManager = HighlighterManager()

    val highlighters: List<Highlighter>
        get() {
            return highlighterManager.highlighters
        }

    val text: String
        get() {
            return textProvider.text
        }

    val cursor: Cursor
        get() {
            return textProvider.cursor
        }

    private fun isPrintable(ch: Char): Boolean {
        return !ch.isISOControl() && !ch.isIdentifierIgnorable() && ch.isDefined()
    }

    fun processKeyEvent(event: KeyEvent): Boolean {
        val shortcut: ShortcutsRegistry.ShortcutCombination? = ShortcutsRegistry.identify(event)

        println("shortcut: $shortcut")

        if (event.type == KeyEventType.KeyDown) {
            processPressedEventKey(event, shortcut)
        }
        return true
    }

    private fun processPressedEventKey(event: KeyEvent, shortcut: ShortcutsRegistry.ShortcutCombination?) {
        val oldCursorOffset = cursor.offset

        when (event.key) {
            Key.Backspace -> textProvider.backspace()
            Key.Enter -> textProvider.newline()
            Key.DirectionUp -> textProvider.changeCursorPositionDirectionUp()
            Key.DirectionRight -> textProvider.changeCursorPositionDirectionRight()
            Key.DirectionDown -> textProvider.changeCursorPositionDirectionDown()
            Key.DirectionLeft -> textProvider.changeCursorPositionDirectionLeft()
            Key.Spacebar -> textProvider.insert(nonBreakingSpaceChar)
            Key.Delete -> textProvider.delete()
            else -> {
                val ch: Char = event.utf16CodePoint.toChar()
                if (isPrintable(ch)) {
                    textProvider.insert(ch)
                }
                else {
                    //println("Provided unsupported character '${ch.code}' is non-printable")
                }
            }
        }

        val newCursorOffset = cursor.offset

        adjustSelectionShortcut(shortcut, event, oldCursorOffset, newCursorOffset)
    }

    private fun adjustSelectionShortcut(
        shortcut: ShortcutsRegistry.ShortcutCombination?, event: KeyEvent, oldCursorOffset: Int, newCursorOffset: Int) {

        when (shortcut) {
            ShortcutsRegistry.ShortcutCombination.CTRL_A -> {
                highlighterManager.dismissSelection()
                highlighterManager.selection(0, text.length)
            }
            ShortcutsRegistry.ShortcutCombination.SHIFT_DIRECTION_UP,
            ShortcutsRegistry.ShortcutCombination.SHIFT_DIRECTION_RIGHT,
            ShortcutsRegistry.ShortcutCombination.SHIFT_DIRECTION_DOWN,
            ShortcutsRegistry.ShortcutCombination.SHIFT_DIRECTION_LEFT -> {
                if (!highlighterManager.containsSelectionHighlighter()) {
                    highlighterManager.selection(
                        min(oldCursorOffset, newCursorOffset),
                        max(oldCursorOffset, newCursorOffset),
                    )
                }
                else {
                    highlighterManager.adjustSelection(oldCursorOffset, newCursorOffset)
                }
            }
            else -> {
                if (!(event.isShiftPressed && (event.key == Key.ShiftLeft || event.key == Key.ShiftRight))) {
                    highlighterManager.dismissSelection()
                }
            }
        }
    }
}