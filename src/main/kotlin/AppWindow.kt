import androidx.compose.runtime.Composable
import androidx.compose.ui.window.Window
import components.views.App
import components.viewmodels.TextViewModel


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
