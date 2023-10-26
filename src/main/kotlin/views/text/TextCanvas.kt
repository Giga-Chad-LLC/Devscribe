package views.text

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.*
import androidx.compose.ui.unit.sp
import components.vfs.VirtualFileSystem
import models.PinnedFileModel
import viewmodels.TextViewModel
import views.common.Fonts
import views.common.Settings

@OptIn(ExperimentalTextApi::class)
@Composable
fun TextCanvas(
    modifier: Modifier,
    activeFileModel: PinnedFileModel?,
    settings: Settings
) {
    val coroutineScope = rememberCoroutineScope()
    val textMeasurer = rememberTextMeasurer()
    val textViewModel by remember { mutableStateOf(TextViewModel(coroutineScope)) }

    if (activeFileModel != null) {
        textViewModel.activeFileModel = activeFileModel

        Canvas(modifier = modifier) {
            textViewModel.let {
                val measuredText = textMeasurer.measure(
                    text = AnnotatedString(it.text),
                    style = TextStyle(
                        color = Color.White,
                        fontSize = settings.fontSettings.fontSize,
                        fontFamily = settings.fontSettings.fontFamily
                    )
                )

                val cursor: Rect = measuredText.getCursorRect(it.cursor.offset)

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
    else {
        Box(
            modifier = modifier,
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "Select file for modifications",
                fontFamily = Fonts.JetBrainsMono(),
                color = Color(1.0f, 1.0f, 1.0f, 0.6f),
                fontSize = 22.sp
            )
        }
    }
}