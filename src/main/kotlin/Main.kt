@file:OptIn(ExperimentalTextApi::class, ExperimentalTextApi::class, ExperimentalTextApi::class,
    ExperimentalTextApi::class, ExperimentalTextApi::class, ExperimentalTextApi::class, ExperimentalTextApi::class
)

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.type
import androidx.compose.ui.input.key.utf16CodePoint
import androidx.compose.ui.text.*
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application


val line = mutableStateOf("Hello")

@OptIn(ExperimentalTextApi::class)
@Composable
@Preview
fun App() {
    val textMeasurer = rememberTextMeasurer()

    MaterialTheme {
        Box() {
            Button(onClick = {
                line.value = "Button pressed"
            }) {
                Text("Hello")
            }

            Canvas(modifier = Modifier.fillMaxSize()) {
                line.let {
                    val measuredText = textMeasurer.measure(
                        AnnotatedString(it.value),
                        style = TextStyle(fontSize = 20.sp)
                    )

                    translate(100f, 100f) {
                        drawText(measuredText)
                    }
                }
            }
        }
    }
}

fun main() = application {
    Window(onCloseRequest = ::exitApplication,
        onKeyEvent = {
            if (it.type == KeyEventType.KeyUp) {
                line.value += it.utf16CodePoint.toChar()
            }
            true
        }) {
        App()
    }
}
