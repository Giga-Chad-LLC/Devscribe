package components.shortcuts

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.key.*

class ShortcutsRegistry {
    enum class ShortcutCombination {
        CTRL_A,
        SHIFT_DIRECTION_UP,
        SHIFT_DIRECTION_RIGHT,
        SHIFT_DIRECTION_DOWN,
        SHIFT_DIRECTION_LEFT,
    }

    companion object {
        @OptIn(ExperimentalComposeUiApi::class)
        fun identify(event: KeyEvent): ShortcutCombination? {
            var action: ShortcutCombination? = null

            if (event.type == KeyEventType.KeyDown && event.isCtrlPressed && event.key == Key.A) {
                action = ShortcutCombination.CTRL_A
            }
            else if (event.type == KeyEventType.KeyDown && event.isShiftPressed && event.key == Key.DirectionUp) {
                action = ShortcutCombination.SHIFT_DIRECTION_UP
            }
            else if (event.type == KeyEventType.KeyDown && event.isShiftPressed && event.key == Key.DirectionRight) {
                action = ShortcutCombination.SHIFT_DIRECTION_RIGHT
            }
            else if (event.type == KeyEventType.KeyDown && event.isShiftPressed && event.key == Key.DirectionDown) {
                action = ShortcutCombination.SHIFT_DIRECTION_DOWN
            }
            else if (event.type == KeyEventType.KeyDown && event.isShiftPressed && event.key == Key.DirectionLeft) {
                action = ShortcutCombination.SHIFT_DIRECTION_LEFT
            }
            return action;
        }
    }
}