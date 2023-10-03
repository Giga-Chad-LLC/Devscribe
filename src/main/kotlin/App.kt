@file:OptIn(ExperimentalTextApi::class, ExperimentalTextApi::class, ExperimentalTextApi::class,
    ExperimentalTextApi::class, ExperimentalTextApi::class, ExperimentalTextApi::class, ExperimentalTextApi::class
)

import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.*
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalTextApi::class)
@Composable
@Preview
fun App(textViewModel: TextViewModel) {
    val textMeasurer = rememberTextMeasurer()

    MaterialTheme {
        Box {
            Canvas(
                modifier = Modifier
                    .padding(10.dp)
                    .fillMaxSize()
                    .border(BorderStroke(1.dp, Color.Red)),
            ) {
                textViewModel.let {
                    val measuredText = textMeasurer.measure(
                        AnnotatedString(textViewModel.text),
                        style = TextStyle(fontSize = 20.sp)
                    )

                    /*
                    AnnotatedString(it.value, listOf(
                        AnnotatedString.Range(SpanStyle(fontWeight = FontWeight(900)), 0, it.value.length)
                    ))
                    */
                    drawText(measuredText)
                }
            }
        }
    }
}