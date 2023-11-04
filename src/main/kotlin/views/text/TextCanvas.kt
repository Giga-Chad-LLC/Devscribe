package views.text

import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.rememberScrollableState
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.v2.ScrollbarAdapter
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
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
import androidx.compose.ui.unit.dp
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

private fun CanvasState.getMaxVerticalScrollOffset(): Float {
    val maxLinesNumber = textModel.linesCount() - LINES_COUNT_VERTICAL_OFFSET
    return maxLinesNumber * symbolSize.height
}

private fun CanvasState.getMaxHorizontalScrollOffset(): Float {
    val maxLineOffset = textModel.maxLineLength() * symbolSize.width
    val canvasWidth = canvasSize.value.width.toFloat()

    var maxHorizontalOffset = (maxLineOffset - canvasWidth).coerceAtLeast(0f)
    if (maxHorizontalOffset > 0) {
        maxHorizontalOffset += SYMBOLS_COUNT_HORIZONTAL_OFFSET * symbolSize.width
    }

    return maxHorizontalOffset
}

private fun CanvasState.coerceVerticalOffset(offset: Float): Float {
    return offset
        .coerceAtLeast(0f)
        .coerceAtMost(getMaxVerticalScrollOffset())
}

private fun CanvasState.coerceHorizontalOffset(offset: Float): Float {
    return offset
        .coerceAtLeast(0f)
        .coerceAtMost(getMaxHorizontalScrollOffset())
}


@Composable
private fun BoxScope.CanvasVerticalScrollbar(canvasState: CanvasState) {
    VerticalScrollbar(
        object : ScrollbarAdapter {
            override val contentSize: Double
                get() = canvasState.getMaxVerticalScrollOffset().toDouble() + viewportSize
            override val scrollOffset: Double
                get() = canvasState.verticalScrollOffset.value.toDouble()
            override val viewportSize: Double
                get() = canvasState.canvasSize.value.height.toDouble()

            override suspend fun scrollTo(scrollOffset: Double) {
                canvasState.verticalScrollOffset.value =
                    canvasState.coerceVerticalOffset(scrollOffset.toFloat())
            }
        },
        Modifier.align(Alignment.CenterEnd)
    )
}


@Composable
private fun BoxScope.CanvasHorizontalScrollbar(canvasState: CanvasState) {
    HorizontalScrollbar(
        object : ScrollbarAdapter {
            override val contentSize: Double
                get() = canvasState.getMaxHorizontalScrollOffset() + viewportSize
            override val scrollOffset: Double
                get() = canvasState.horizontalScrollOffset.value.toDouble()
            override val viewportSize: Double
                get() = canvasState.canvasSize.value.width.toDouble()

            override suspend fun scrollTo(scrollOffset: Double) {
                canvasState.horizontalScrollOffset.value = canvasState.coerceHorizontalOffset(scrollOffset.toFloat())
            }

        },
        Modifier.align(Alignment.BottomCenter)
    )
}


@OptIn(ExperimentalTextApi::class, ExperimentalFoundationApi::class)
@Composable
fun BoxScope.TextCanvas(
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

    // TODO: use extension function of CanvasState
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
        /**
         * If max line of a text exceeds the viewport of canvas then set the max horizontal offset to the difference
         * between the viewport width and line width extended by SYMBOLS_COUNT_HORIZONTAL_OFFSET.
         * Otherwise, set max offset to 0.
         */
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

    // scrollbars
    CanvasVerticalScrollbar(canvasState)
    CanvasHorizontalScrollbar(canvasState)
}