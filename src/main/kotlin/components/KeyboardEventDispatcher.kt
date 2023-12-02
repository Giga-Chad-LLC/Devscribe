package components

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.input.key.*
import java.util.*
import java.util.function.Consumer
import kotlin.collections.ArrayList

@OptIn(ExperimentalComposeUiApi::class)
class KeyboardEventDispatcher private constructor() {
    private val subscribers: MutableMap<KeyboardAction, MutableList<EventHandler>> = EnumMap(KeyboardAction::class.java)

    private fun isSaveFileAction(event: KeyEvent): Boolean {
        return (event.type == KeyEventType.KeyDown && event.key == Key.S && event.isCtrlPressed)
    }

    private fun isOpenProjectAction(event: KeyEvent): Boolean {
        return (event.type == KeyEventType.KeyDown && event.key == Key.O && event.isCtrlPressed)
    }

    /*private fun isBackspaceAction(event: KeyEvent): Boolean {
        return (event.type == KeyEventType.KeyDown && event.key == Key.Backspace)
    }

    private fun isNewlineAction(event: KeyEvent): Boolean {
        return (event.type == KeyEventType.KeyDown && event.key == Key.Enter)
    }

    private fun isDirectionUpAction(event: KeyEvent): Boolean {
        return (event.type == KeyEventType.KeyDown && event.key == Key.DirectionUp)
    }

    private fun isDirectionRightAction(event: KeyEvent): Boolean {
        return (event.type == KeyEventType.KeyDown && event.key == Key.DirectionRight)
    }

    private fun isDirectionDownAction(event: KeyEvent): Boolean {
        return (event.type == KeyEventType.KeyDown && event.key == Key.DirectionDown)
    }

    private fun isDirectionLeftAction(event: KeyEvent): Boolean {
        return (event.type == KeyEventType.KeyDown && event.key == Key.DirectionLeft)
    }

    private fun isSpaceAction(event: KeyEvent): Boolean {
        return (event.type == KeyEventType.KeyDown && event.key == Key.Spacebar)
    }

    private fun isDeleteAction(event: KeyEvent): Boolean {
        return (event.type == KeyEventType.KeyDown && event.key == Key.Delete)
    }

    private fun isPrintableSymbolAction(event: KeyEvent): Boolean {
        val ch = event.utf16CodePoint.toChar()
        return event.type == KeyEventType.KeyDown && !ch.isISOControl() && !ch.isIdentifierIgnorable() && ch.isDefined()
    }*/

    fun dispatch(event: KeyEvent): Boolean {
        var action: KeyboardAction? = null

        if (isSaveFileAction(event)) {
            action = KeyboardAction.SAVE_FILE
        }
        if (isOpenProjectAction(event)) {
            action = KeyboardAction.OPEN_PROJECT
        }
        /*if (isBackspaceAction(event)) {
            action = KeyboardAction.BACKSPACE
        }
        else if (isNewlineAction(event)) {
            action = KeyboardAction.NEWLINE
        }
        else if (isDirectionUpAction(event)) {
            action = KeyboardAction.DIRECTION_UP
        }
        else if (isDirectionRightAction(event)) {
            action = KeyboardAction.DIRECTION_RIGHT
        }
        else if (isDirectionDownAction(event)) {
            action = KeyboardAction.DIRECTION_DOWN
        }
        else if (isDirectionLeftAction(event)) {
            action = KeyboardAction.DIRECTION_LEFT
        }
        else if (isSpaceAction(event)) {
            action = KeyboardAction.SPACE
        }
        else if (isDeleteAction(event)) {
            action = KeyboardAction.DELETE
        }
        else if (isPrintableSymbolAction(event)) {
            action = KeyboardAction.PRINTABLE_SYMBOL
        }*/

        val actionHandled = (action != null)

        if (actionHandled && subscribers.containsKey(action)) {
            for (handler in subscribers[action]!!) {
                handler.execute(event)
            }
        }

        return actionHandled
    }

    fun subscribe(action: KeyboardAction, callback: Consumer<KeyEvent>): SubscriptionId {
        if (!subscribers.containsKey(action)) {
            subscribers[action] = ArrayList()
        }

        val handler = EventHandler(callback)
        subscribers[action]!!.add(handler)

        return handler.getSubscriptionId()
    }

    fun unsubscribe(subId: SubscriptionId) {
        outer@ for (entry in subscribers) {
            for (handler in entry.value) {
                if (handler.getSubscriptionId() == subId) {
                    entry.value.remove(handler)
                    break@outer
                }
            }
        }
    }

    private object SingletonHelper {
        val INSTANCE = KeyboardEventDispatcher()
    }
    companion object {
        fun getInstance(): KeyboardEventDispatcher {
            return SingletonHelper.INSTANCE
        }
    }


    enum class KeyboardAction {
        SAVE_FILE,
        OPEN_PROJECT,
        BACKSPACE,
        NEWLINE,
        DIRECTION_UP,
        DIRECTION_RIGHT,
        DIRECTION_DOWN,
        DIRECTION_LEFT,
        SPACE,
        DELETE,
        PRINTABLE_SYMBOL,
    }

    data class SubscriptionId(private val uuid: UUID? = UUID.randomUUID())

    private class EventHandler(private val callback: Consumer<KeyEvent>) {
        private val subscriptionId = SubscriptionId()

        fun getSubscriptionId(): SubscriptionId {
            return subscriptionId
        }

        fun execute(event: KeyEvent) {
            callback.accept(event)
        }
    }
}
