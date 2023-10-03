import androidx.compose.runtime.Composable
import androidx.compose.ui.window.Window
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.type
import androidx.compose.ui.input.key.utf16CodePoint


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
    when (event.type) {
        KeyEventType.KeyDown -> textViewModel.insertCharacter(event.utf16CodePoint.toChar())
    }

    return true
}