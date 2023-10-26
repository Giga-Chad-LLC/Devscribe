import androidx.compose.runtime.Composable
import androidx.compose.ui.window.Window
import components.dispatcher.KeyboardEventDispatcher
import views.App


@Composable
fun AppWindow(onCloseRequestCallback: () -> Unit) {
    val keyboardDispatcher = KeyboardEventDispatcher.getInstance()
    return Window(
        onCloseRequest = onCloseRequestCallback,
        onKeyEvent = { keyboardDispatcher.dispatch(it) }
    ) {
        App()
    }
}
