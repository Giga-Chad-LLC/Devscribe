import androidx.compose.runtime.Composable
import androidx.compose.ui.window.Window
import components.dispatcher.KeyboardEventDispatcher
import views.App
import viewmodels.TextViewModel


@Composable
fun AppWindow(onCloseRequestCallback: () -> Unit) {
    val keyboardDispatcher = KeyboardEventDispatcher.getInstance()
    val textViewModel = TextViewModel()

    return Window(
        onCloseRequest = onCloseRequestCallback,
        onKeyEvent = { keyboardDispatcher.dispatch(it) }
    ) {
        App(textViewModel)
    }
}
