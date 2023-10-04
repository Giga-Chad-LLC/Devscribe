import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.input.key.*
import androidx.compose.ui.window.Window


@Composable
fun AppWindow(onCloseRequestCallback: () -> Unit) {
    val textViewModel = TextViewModel()

    return Window(
        onCloseRequest = onCloseRequestCallback,
        onKeyEvent = { textViewModel.processKeyEvent(it) }
    ) {
        App(textViewModel)
    }
}
