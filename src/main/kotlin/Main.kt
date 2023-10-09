import androidx.compose.ui.window.application

fun main() = application {
    AppWindow(onCloseRequestCallback = ::exitApplication)
}
