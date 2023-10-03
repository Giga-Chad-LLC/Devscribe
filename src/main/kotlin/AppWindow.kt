import androidx.compose.runtime.Composable
import androidx.compose.ui.input.key.*
import androidx.compose.ui.window.Window


private val textViewModel = TextViewModel()

@Composable
fun AppWindow(onCloseRequestCallback: () -> Unit) {
    return Window(
        onCloseRequest = onCloseRequestCallback,
        onKeyEvent = { handleEventKey(it) }
    ) {
        App(textViewModel)
    }
}


private fun handleEventKey(event: KeyEvent): Boolean {
    return textViewModel.processKeyEvent(event)
}