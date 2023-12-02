import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowState
import components.KeyboardEventDispatcher
import views.App


@Composable
fun AppWindow(onCloseRequestCallback: () -> Unit) {
    val keyboardDispatcher = KeyboardEventDispatcher.getInstance()
    return Window(
        title = "Devscribe",
        onCloseRequest = onCloseRequestCallback,
        state = WindowState(width = 1280.dp, height = 768.dp),
        onKeyEvent = { keyboardDispatcher.dispatch(it) }
    ) {
        App()
    }
}
