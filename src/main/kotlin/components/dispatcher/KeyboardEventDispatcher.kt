package components.dispatcher

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.input.key.*
import java.util.*
import java.util.function.Consumer
import kotlin.collections.ArrayList

class KeyboardEventDispatcher private constructor() {
    private val subscribers: MutableMap<KeyboardAction, MutableList<EventHandler>> = EnumMap(KeyboardAction::class.java)

    @OptIn(ExperimentalComposeUiApi::class)
    fun dispatch(event: KeyEvent): Boolean {
        var action: KeyboardAction? = null

        if (event.type == KeyEventType.KeyDown) {
            when (event.key) {
                Key.Backspace -> action = KeyboardAction.BACKSPACE
                Key.Enter -> action = KeyboardAction.NEWLINE
                Key.DirectionUp -> action = KeyboardAction.DIRECTION_UP
                Key.DirectionRight -> action = KeyboardAction.DIRECTION_RIGHT
                Key.DirectionDown -> action = KeyboardAction.DIRECTION_DOWN
                Key.DirectionLeft -> action = KeyboardAction.DIRECTION_LEFT
                Key.Spacebar -> action = KeyboardAction.SPACE
                Key.Delete -> action = KeyboardAction.DELETE
                else -> {
                    val ch: Char = event.utf16CodePoint.toChar()
                    if (isPrintable(ch)) {
                        action = KeyboardAction.PRINTABLE_SYMBOL
                    }
                    else {
                        println("Provided unsupported character '${ch.code}' is non-printable")
                    }
                }
            }
        }

        val actionHandled: Boolean = (action != null)
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

    private fun isPrintable(ch: Char): Boolean {
        return !ch.isISOControl() && !ch.isIdentifierIgnorable() && ch.isDefined()
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
        BACKSPACE,
        NEWLINE,
        DIRECTION_UP,
        DIRECTION_RIGHT,
        DIRECTION_DOWN,
        DIRECTION_LEFT,
        SPACE,
        DELETE,
        PRINTABLE_SYMBOL
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
