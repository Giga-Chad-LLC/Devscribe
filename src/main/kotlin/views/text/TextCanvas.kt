package views.text

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.sp
import models.FileModel
import viewmodels.TextViewModel
import views.common.Settings

@OptIn(ExperimentalTextApi::class)
@Composable
fun TextCanvas(modifier: Modifier, activeFileModel: FileModel?, settings: Settings) {
    val textMeasurer = rememberTextMeasurer()
    val textViewModel by remember { mutableStateOf(TextViewModel()) }

    if (activeFileModel != null) {
        textViewModel.textModel = activeFileModel.textModel

        Canvas(modifier = modifier) {
            textViewModel.let {
                val measuredText = textMeasurer.measure(
                    text = AnnotatedString(textViewModel.text),
                    style = TextStyle(
                        color = Color.White,
                        fontSize = settings.fontSettings.fontSize,
                        fontFamily = settings.fontSettings.fontFamily
                    )
                )

                val cursor: Rect = measuredText.getCursorRect(textViewModel.cursor.offset)

                /*
                AnnotatedString(it.value, listOf(
                    AnnotatedString.Range(SpanStyle(fontWeight = FontWeight(900)), 0, it.value.length)
                ))
                */
                drawText(measuredText)
                drawRect(
                    color = Color.LightGray,
                    topLeft = Offset(cursor.left, cursor.top),
                    size = cursor.size,
                    style = Stroke(5f)
                )
            }
        }
    }
}