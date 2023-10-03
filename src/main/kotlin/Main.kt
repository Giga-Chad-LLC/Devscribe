import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.window.application

fun main() = application {
    AppWindow(onCloseRequestCallback = ::exitApplication)
    /*Window(onCloseRequest = ::exitApplication,
        onKeyEvent = {
            if (it.type == KeyEventType.KeyDown) {
                line.value += it.utf16CodePoint.toChar()
            }
            true
        }) {
        App()
    }*/
}
