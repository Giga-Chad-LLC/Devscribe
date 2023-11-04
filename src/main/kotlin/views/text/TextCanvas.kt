package views.text

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.rememberScrollableState
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.onClick
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.text.*
import androidx.compose.ui.unit.IntSize
import models.PinnedFileModel
import models.text.TextModel
import viewmodels.TextViewModel
import views.common.Settings


internal const val LINES_COUNT_VERTICAL_OFFSET = 5
internal const val SYMBOLS_COUNT_HORIZONTAL_OFFSET = 5


internal data class CanvasState(
    val verticalScrollOffset: MutableState<Float>,
    val horizontalScrollOffset: MutableState<Float>,
    val canvasSize: MutableState<IntSize>,
    val symbolSize: Size,
    val textModel: TextModel,
)


@OptIn(ExperimentalTextApi::class, ExperimentalFoundationApi::class)
@Composable
fun TextCanvas(
    modifier: Modifier,
    activeFileModel: PinnedFileModel,
    settings: Settings
) {
    val coroutineScope = rememberCoroutineScope()
    val textMeasurer = rememberTextMeasurer()

    val textViewModel by remember { mutableStateOf(TextViewModel(coroutineScope)) }
    textViewModel.activeFileModel = activeFileModel

    val requester = remember { FocusRequester() }

    val canvasState = CanvasState(
        verticalScrollOffset = remember { mutableStateOf(0f) },
        horizontalScrollOffset = remember { mutableStateOf(0f) },
        canvasSize = remember { mutableStateOf(IntSize.Zero) },
        symbolSize = remember(settings.fontSettings) {
            // TODO: might be incorrect
            val style = TextStyle(
                fontSize = settings.fontSettings.fontSize,
                fontFamily = settings.fontSettings.fontFamily
            )
            val size = textMeasurer.measure(AnnotatedString("a"), style).size
            Size(size.width.toFloat(), size.height.toFloat())
        },
        textModel = activeFileModel.textModel
    )

    // println("textSymbolSize=${canvasState.symbolSize}")

    val verticalScrollState = rememberScrollableState { delta ->
        /**
         * N := LINES_COUNT_VERTICAL_OFFSET
         *
         * Subtracting N from total lines count to make last N lines visible at the lowest position of vertical scroll
         */
        val maxLinesNumber = canvasState.textModel.linesCount() - LINES_COUNT_VERTICAL_OFFSET
        val maxVerticalOffset = maxLinesNumber * canvasState.symbolSize.height

        val newScrollOffset = (canvasState.verticalScrollOffset.value - delta)
            .coerceAtLeast(0f)
            .coerceAtMost(maxVerticalOffset)

        val scrollConsumed = canvasState.verticalScrollOffset.value - newScrollOffset
        canvasState.verticalScrollOffset.value = newScrollOffset
        scrollConsumed
    }

    val horizontalScrollState = rememberScrollableState { delta ->
        val maxLineOffset = canvasState.textModel.maxLineLength() * canvasState.symbolSize.width
        val canvasWidth = canvasState.canvasSize.value.width.toFloat()

        var maxHorizontalOffset = (maxLineOffset - canvasWidth).coerceAtLeast(0f)
        if (maxHorizontalOffset > 0) {
            maxHorizontalOffset += SYMBOLS_COUNT_HORIZONTAL_OFFSET * canvasState.symbolSize.width
        }

        val newScrollOffset = (canvasState.horizontalScrollOffset.value - delta)
            .coerceAtLeast(0f)
            .coerceAtMost(maxHorizontalOffset)

        val scrollConsumed = canvasState.horizontalScrollOffset.value - newScrollOffset
        canvasState.horizontalScrollOffset.value = newScrollOffset
        scrollConsumed
    }

    Canvas(
        modifier.then(
            Modifier
                .clipToBounds()
                .focusRequester(requester)
                .focusable()
                .onSizeChanged { canvasState.canvasSize.value = it }
                .onClick { requester.requestFocus() }
                .scrollable(verticalScrollState, Orientation.Vertical)
                .scrollable(horizontalScrollState, Orientation.Horizontal)
        )
    ) {
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
            val verticalOffset = canvasState.verticalScrollOffset.value
            val horizontalOffset = canvasState.horizontalScrollOffset.value

            drawText(
                measuredText,
                topLeft = Offset(-horizontalOffset, -verticalOffset)
            )

            drawRect(
                color = Color.LightGray,
                topLeft = Offset(cursor.left - horizontalOffset, cursor.top - verticalOffset),
                size = cursor.size,
                style = Stroke(5f)
            )
        }
    }
}