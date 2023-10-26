package views.text

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.sp
import components.vfs.VirtualFileSystem
import models.PinnedFileModel
import viewmodels.TextViewModel

@OptIn(ExperimentalTextApi::class)
@Composable
fun TextCanvas(modifier: Modifier, activeFileModel: PinnedFileModel?) {
    val coroutineScope = rememberCoroutineScope()
    val textMeasurer = rememberTextMeasurer()
    val textViewModel by remember { mutableStateOf(TextViewModel(coroutineScope)) }

//    if (activeFileModel != null) {
//         textViewModel.textModel = activeFileModel.textModel
//    }
    if (activeFileModel != null) {
        textViewModel.activeFileModel = activeFileModel

        Canvas(modifier = modifier) {
            textViewModel.let {
                val measuredText = textMeasurer.measure(
                    text = AnnotatedString(it.text),
                    style = TextStyle(fontSize = 20.sp, fontFamily = FontFamily.Monospace)
                )

                val cursor: Rect = measuredText.getCursorRect(it.cursor.offset)

                /*
            AnnotatedString(it.value, listOf(
                AnnotatedString.Range(SpanStyle(fontWeight = FontWeight(900)), 0, it.value.length)
            ))
            */
                drawText(measuredText)
                drawRect(
                    color = Color.Black,
                    topLeft = Offset(cursor.left, cursor.top),
                    size = cursor.size,
                    style = Stroke(5f)
                )
            }
        }
    }
}