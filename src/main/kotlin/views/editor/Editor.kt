package views.editor

import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.v2.ScrollbarAdapter
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.key.*
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFontFamilyResolver
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.platform.FontLoadResult
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import common.ceilToInt
import common.isPrintableSymbolAction
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import models.PinnedFileModel
import models.text.Cursor
import org.jetbrains.skia.Font
import viewmodels.TextViewModel
import views.common.CustomTheme
import views.common.FontSettings
import views.common.Settings
import kotlin.math.roundToInt


internal const val LINES_COUNT_VERTICAL_OFFSET = 5
internal const val SYMBOLS_COUNT_HORIZONTAL_OFFSET = 5
internal const val LINES_PANEL_RIGHT_PADDING = 10f
internal const val LINES_PANEL_LEFT_PADDING = 40f
internal const val TEXT_CANVAS_LEFT_MARGIN = 8f

internal data class CanvasState(
    val verticalScrollOffset: MutableState<Float>,
    val horizontalScrollOffset: MutableState<Float>,
    val canvasSize: MutableState<IntSize>,
    val symbolSize: Size,
    val textViewModel: TextViewModel,
)

private fun CanvasState.getMaxVerticalScrollOffset(): Float {
    /**
     * N := LINES_COUNT_VERTICAL_OFFSET
     *
     * Subtracting N from total lines count to make last N lines visible at the lowest position of vertical scroll
     */
    val maxLinesNumber = (textViewModel.textModel.linesCount() - LINES_COUNT_VERTICAL_OFFSET).coerceAtLeast(0)
    return maxLinesNumber * symbolSize.height
}

private fun CanvasState.getMaxHorizontalScrollOffset(): Float {
    /**
     * If max line of a text exceeds the viewport of canvas then set the max horizontal offset to the difference
     * between the viewport width and line width extended by SYMBOLS_COUNT_HORIZONTAL_OFFSET.
     * Otherwise, set max offset to 0.
     */
    val maxLineOffset = textViewModel.textModel.maxLineLength() * symbolSize.width
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

/**
 * Accepts canvas offset (e.g. offset of mouse click event) and maps it to (lineIndex, lineOffset) pair.
 */
private fun CanvasState.canvasOffsetToCursorPosition(offset: Offset) : Pair<Int, Int> {
    val lineIndex = ((offset.y + verticalScrollOffset.value) / symbolSize.height)
        .toInt().coerceAtMost(textViewModel.textModel.linesCount() - 1)

    /*println("symbolWidth=${symbolSize.width}")
    println("lineOffsetFloat=${(offset.x + horizontalScrollOffset.value - TEXT_CANVAS_LEFT_MARGIN) / symbolSize.width}")*/

    val lineOffset = ((offset.x + horizontalScrollOffset.value - TEXT_CANVAS_LEFT_MARGIN) / symbolSize.width)
        .roundToInt().coerceAtLeast(0).coerceAtMost(textViewModel.textModel.lineLength(lineIndex))

    return lineIndex to lineOffset
}

private fun CanvasState.viewportLinesRange(): Pair<Int, Int> {
    val startOffset = Offset(horizontalScrollOffset.value, verticalScrollOffset.value)
    val endOffset = Offset(
        horizontalScrollOffset.value + canvasSize.value.width,
        verticalScrollOffset.value + canvasSize.value.height
    )

    val startLineIndex = (startOffset.y / symbolSize.height)
        .toInt().coerceAtMost(textViewModel.textModel.linesCount() - 1)

    val endLineIndex = ((endOffset.y / symbolSize.height).ceilToInt())
        .coerceAtMost(textViewModel.textModel.linesCount())

    return startLineIndex to endLineIndex
}

private fun CanvasState.calculateViewportVisibleText(): String {
    val (startLineIndex, endLineIndex) = viewportLinesRange()
    //println("startLineIndex=$startLineIndex, endLineIndex=$endLineIndex")

    return textViewModel.textModel.textInLinesRange(startLineIndex, endLineIndex)
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

@Composable
private fun CanvasState.initializeVerticalScrollbar() = rememberScrollableState { delta ->
    val newScrollOffset = coerceVerticalOffset(verticalScrollOffset.value - delta)
    val scrollConsumed = verticalScrollOffset.value - newScrollOffset
    verticalScrollOffset.value = newScrollOffset
    scrollConsumed
}

@Composable
private fun CanvasState.initializeHorizontalScrollbar() = rememberScrollableState { delta ->
    val newScrollOffset = coerceHorizontalOffset(horizontalScrollOffset.value - delta)
    val scrollConsumed = horizontalScrollOffset.value - newScrollOffset
    horizontalScrollOffset.value = newScrollOffset
    scrollConsumed
}


private fun scrollOnCursorOutOfCanvasViewport(
    coroutineScope: CoroutineScope,
    cursorPosition: Int,
    symbolSizeDimension: Float,
    canvasDimensionOffset: Float,
    viewportSize: Int,
    scrollState: ScrollableState
) {
    val cursorOffsetInsideViewport = cursorPosition * symbolSizeDimension - canvasDimensionOffset

    if (cursorOffsetInsideViewport > viewportSize) {
        val offset = cursorOffsetInsideViewport - viewportSize
        coroutineScope.launch {
            scrollState.scrollBy(-offset)
        }
    }
    else if (cursorOffsetInsideViewport < 0) {
        coroutineScope.launch {
            scrollState.scrollBy(-cursorOffsetInsideViewport)
        }
    }
}

private fun scrollHorizontallyOnCursorOutOfCanvasViewport(
    coroutineScope: CoroutineScope,
    canvasState: CanvasState,
    horizontalOffset: Float,
    cursor: Cursor,
    horizontalScrollState: ScrollableState
) {
    scrollOnCursorOutOfCanvasViewport(
        coroutineScope = coroutineScope,
        cursorPosition = cursor.currentLineOffset,
        symbolSizeDimension = canvasState.symbolSize.width,
        canvasDimensionOffset = horizontalOffset,
        viewportSize = canvasState.canvasSize.value.width,
        scrollState = horizontalScrollState
    )
}

private fun scrollVerticallyOnCursorOutOfCanvasViewport(
    coroutineScope: CoroutineScope,
    canvasState: CanvasState,
    verticalOffset: Float,
    cursor: Cursor,
    verticalScrollState: ScrollableState
) {
    /**
     * Truncation of viewport height by the height of the cursor is required because
     * otherwise when navigating down cursor will be fully invisible before the manual scrolling starts
     * (only a point of (0, 0) of cursor rectangle will be visible).
     * This is so because cursor is considered to be out of a viewport once its point with offset (0, 0) becomes out of the viewport.
     *
     * Thus, in order to keep cursor fully visible inside the viewport while navigating both up & down
     * the truncation of viewport by the size of the cursor is required.
     *
     * Note that this is not the case for horizontal movements.
     */
    val ceiledSymbolHeight = canvasState.symbolSize.height.ceilToInt()
    val truncatedViewportHeight = canvasState.canvasSize.value.height - ceiledSymbolHeight

    scrollOnCursorOutOfCanvasViewport(
        coroutineScope = coroutineScope,
        cursorPosition = cursor.lineNumber,
        symbolSizeDimension = canvasState.symbolSize.height,
        canvasDimensionOffset = verticalOffset,
        viewportSize = truncatedViewportHeight,
        scrollState = verticalScrollState
    )
}


/**
 * textMeasurer.measure("x").width (or .multiParagraph.width) is incorrect.
 * But skia multiplied by current density counts the symbol width correctly, but the height calculated by skia is incorrect.
 *
 * Thus, here width is calculated via skia, and height is calculated via TextMeasurer.
 * Recalculation occurs only on font size change, therefore it should not impact performance.
 *
 * @return The width and height of a symbol with respect to its font settings.
 */
@OptIn(ExperimentalTextApi::class)
private fun getSymbolSize(
    fontFamilyResolver: FontFamily.Resolver,
    textMeasurer: TextMeasurer,
    fontSettings: FontSettings,
    density: Float
): Size {
    val fontLoadResult = fontFamilyResolver.resolve(fontSettings.fontFamily).value as FontLoadResult
    val style = TextStyle(
        fontSize = fontSettings.fontSize,
        fontFamily = fontSettings.fontFamily
    )

    val width = Font(fontLoadResult.typeface, fontSettings.fontSize.value).measureTextWidth("a") * density
    val height = textMeasurer.measure(AnnotatedString("a"), style).size.height

    return Size(width, height.toFloat())
}


@OptIn(ExperimentalTextApi::class)
private fun determineLinesPanelSize(
    fontFamilyResolver: FontFamily.Resolver,
    textMeasurer: TextMeasurer,
    canvasState: CanvasState,
    settings: Settings,
    density: Float
): Size {
    val lineSymbolSize = getSymbolSize(
        fontFamilyResolver,
        textMeasurer,
        settings.editorSettings.linesPanel.fontSettings,
        density
    )

    val maxLineNumber = canvasState.textViewModel.textModel.linesCount()

    return Size(
        // left & right paddings + width of the longest line number
        LINES_PANEL_RIGHT_PADDING + maxLineNumber.toString().length * lineSymbolSize.width + LINES_PANEL_LEFT_PADDING,
        // canvas width
        canvasState.canvasSize.value.height.toFloat()
    )
}

/**
 * Draws panel with line numbers.
 * @return Size of the drawn panel.
 */
@OptIn(ExperimentalTextApi::class)
private fun DrawScope.drawLinesPanel(
    fontFamilyResolver: FontFamily.Resolver,
    textMeasurer: TextMeasurer,
    scrollOffsetY: Float,
    canvasState: CanvasState,
    cursor: Cursor,
    settings: Settings,
    density: Float
) {
    val linesPanelSettings = settings.editorSettings.linesPanel

    val lineSymbolSize = getSymbolSize(
        fontFamilyResolver,
        textMeasurer,
        linesPanelSettings.fontSettings,
        density,
    )

    val (startLineIndex, endLineIndex) = canvasState.viewportLinesRange()

    val startLineNumber = startLineIndex + 1
    val maxLineNumber = endLineIndex

    /**
     * Starting with centering offsets that place the line number in the center of code symbol.
     *
     * Assuming that font size of line number <= font size of code symbol.
     */
    val centeringOffsetX = (canvasState.symbolSize.width - lineSymbolSize.width) / 2
    val centeringOffsetY = (canvasState.symbolSize.height - lineSymbolSize.height) / 2

    /**
     * offsetY starts from the offset of scrolled up lines (i.e. first 'startLineIndex' line)
     */
    var offsetY = centeringOffsetY + startLineIndex * canvasState.symbolSize.height

    val linesPanelSize = determineLinesPanelSize(
        fontFamilyResolver,
        textMeasurer,
        canvasState,
        settings,
        density,
    )

    /**
     * Drawing a split line on the right border of the lines panel
     */
    drawRect(
        color = linesPanelSettings.splitLineColor,
        topLeft = Offset(linesPanelSize.width - 1f, 0f),
        size = Size(1f, linesPanelSize.height)
    )

    /**
     * Drawing lines numbers
     */
    for (lineNumber in startLineNumber .. maxLineNumber) {
        // if current line is under cursor lighten it
        val color = if (cursor.lineNumber + 1 == lineNumber)
                    linesPanelSettings.cursoredLineFontColor else linesPanelSettings.fontSettings.fontColor

        val measuredText = textMeasurer.measure(
            AnnotatedString(lineNumber.toString()),
            style = TextStyle(
                color = color,
                fontSize = linesPanelSettings.fontSettings.fontSize,
                fontWeight = linesPanelSettings.fontSettings.fontWeight,
                fontFamily = linesPanelSettings.fontSettings.fontFamily,
            )
        )

        // the translation makes the line numbers to be aligned by the smallest digit, i.e. digits grow from right to left
        val translationX = (maxLineNumber.toString().length - lineNumber.toString().length) * lineSymbolSize.width
        val resultOffsetX = LINES_PANEL_RIGHT_PADDING + translationX + centeringOffsetX /*+ scrollOffset.x*/

        // drawing line number
        drawText(
            measuredText,
            topLeft = Offset(resultOffsetX, offsetY + scrollOffsetY)
        )

        offsetY += canvasState.symbolSize.height
    }
}


/**
 * Handles mouse click on the canvas by changing cursor position
 */
private fun Modifier.pointerInput(focusRequester: FocusRequester, canvasState: CanvasState): Modifier {
    return this.then(
        Modifier
            .pointerInput(Unit) {
                detectTapGestures(onPress = { offset ->
                    // focusing on the canvas
                    focusRequester.requestFocus()

                    // moving cursor to the mouse click position
                    val (lineIndex, lineOffset) = canvasState.canvasOffsetToCursorPosition(offset)
                    canvasState.textViewModel.textModel.changeCursorPosition(lineIndex, lineOffset)
                })
            }
    )
}

/**
 * Handles keyboard inputs by executing commands of TextViewModel
 */
@OptIn(ExperimentalComposeUiApi::class)
private fun Modifier.handleKeyboardInput(canvasState: CanvasState): Modifier {
    val textViewModel = canvasState.textViewModel

    return this.then(
        Modifier
            .onKeyEvent { keyEvent ->
                var consumed = false

                if (keyEvent.type == KeyEventType.KeyDown && keyEvent.key == Key.Backspace) {
                    textViewModel.backspace()
                    consumed = true
                }
                else if (keyEvent.type == KeyEventType.KeyDown && keyEvent.key == Key.Enter) {
                    textViewModel.newline()
                    consumed = true
                }
                else if (keyEvent.type == KeyEventType.KeyDown && keyEvent.key == Key.DirectionUp) {
                    textViewModel.directionUp()
                    consumed = true
                }
                else if (keyEvent.type == KeyEventType.KeyDown && keyEvent.key == Key.DirectionRight) {
                    textViewModel.directionRight()
                    consumed = true
                }
                else if (keyEvent.type == KeyEventType.KeyDown && keyEvent.key == Key.DirectionDown) {
                    textViewModel.directionDown()
                    consumed = true
                }
                else if (keyEvent.type == KeyEventType.KeyDown && keyEvent.key == Key.DirectionLeft) {
                    textViewModel.directionLeft()
                    consumed = true
                }
                else if (keyEvent.type == KeyEventType.KeyDown && keyEvent.key == Key.Spacebar) {
                    textViewModel.whitespace()
                    consumed = true
                }
                else if (keyEvent.type == KeyEventType.KeyDown && keyEvent.key == Key.Delete) {
                    textViewModel.delete()
                    consumed = true
                }
                else if (keyEvent.type == KeyEventType.KeyDown && isPrintableSymbolAction(keyEvent)) {
                    textViewModel.symbol(keyEvent.utf16CodePoint.toChar())
                    consumed = true
                }

                consumed
            }
    )
}


@OptIn(ExperimentalTextApi::class)
@Composable
fun Editor(activeFileModel: PinnedFileModel, settings: Settings) {
    val coroutineScope = rememberCoroutineScope()
    val textMeasurer = rememberTextMeasurer()
    val textViewModel by remember { mutableStateOf(TextViewModel(coroutineScope, activeFileModel)) }
    var previousCursorState = remember { Cursor(textViewModel.cursor) }
    val requester = remember { FocusRequester() }
    val editorTextStyle = remember {
        TextStyle(
            color = Color.LightGray,
            fontSize = settings.fontSettings.fontSize,
            fontFamily = settings.fontSettings.fontFamily
        )
    }

    /**
     * Required to try to update pinned file model on each invocation because
     * 'remember' of textViewModel does not recreate the object thus outdated pinned file model persists on the canvas.
     */
    textViewModel.updateActiveFileModel(activeFileModel)

    val fontFamilyResolver = LocalFontFamilyResolver.current
    val density = LocalDensity.current.density

    val canvasState = CanvasState(
        verticalScrollOffset = remember { mutableStateOf(0f) },
        horizontalScrollOffset = remember { mutableStateOf(0f) },
        canvasSize = remember { mutableStateOf(IntSize.Zero) },
        symbolSize = remember(settings.fontSettings) {
            getSymbolSize(fontFamilyResolver, textMeasurer, settings.fontSettings, density)
        },
        textViewModel = textViewModel
    )

    val verticalScrollState = canvasState.initializeVerticalScrollbar()
    val horizontalScrollState = canvasState.initializeHorizontalScrollbar()

    val linesPanelSize = determineLinesPanelSize(
        fontFamilyResolver,
        textMeasurer,
        canvasState,
        settings,
        density,
    )

    Row(Modifier.fillMaxSize()) {
        /**
         * Drawing lines panel (panel that contains lines numbers)
         */
        Canvas(
            Modifier
                // dividing by current density to make the width in dp match the actual width
                .width((linesPanelSize.width / LocalDensity.current.density).dp)
                .scrollable(verticalScrollState, Orientation.Vertical)
                .fillMaxHeight()
                .clipToBounds()
                .background(settings.editorSettings.linesPanel.backgroundColor)
        ) {
            drawLinesPanel(
                fontFamilyResolver = fontFamilyResolver,
                textMeasurer = textMeasurer,
                scrollOffsetY = -canvasState.verticalScrollOffset.value,
                canvasState = canvasState,
                cursor = textViewModel.cursor,
                settings = settings,
                density = density,
            )
        }

        /**
         * Drawing canvas with text
         */
        Box {
            Canvas(
                modifier = Modifier
                    // focusRequester() should be added BEFORE focusable()
                    .focusRequester(requester)
                    .focusable()
                    .handleKeyboardInput(canvasState)
                    .onSizeChanged { canvasState.canvasSize.value = it }
                    .pointerInput(requester, canvasState)
                    .scrollable(verticalScrollState, Orientation.Vertical)
                    .scrollable(horizontalScrollState, Orientation.Horizontal)
                    .background(CustomTheme.colors.backgroundDark)
                    .clipToBounds()
                    .fillMaxSize()
            ) {
                textViewModel.let {
                    val verticalOffset = canvasState.verticalScrollOffset.value
                    val horizontalOffset = canvasState.horizontalScrollOffset.value

                    // scrolling on cursor getting out of viewport
                    if (previousCursorState.offset != it.cursor.offset) {
                        scrollHorizontallyOnCursorOutOfCanvasViewport(
                            coroutineScope = coroutineScope,
                            canvasState = canvasState,
                            horizontalOffset = horizontalOffset,
                            cursor = it.cursor,
                            horizontalScrollState = horizontalScrollState,
                        )

                        scrollVerticallyOnCursorOutOfCanvasViewport(
                            coroutineScope = coroutineScope,
                            canvasState = canvasState,
                            verticalOffset = verticalOffset,
                            cursor = it.cursor,
                            verticalScrollState = verticalScrollState,
                        )

                        previousCursorState = Cursor(it.cursor)
                    }

                    val translationX = -horizontalOffset + TEXT_CANVAS_LEFT_MARGIN
                    val translationY = -verticalOffset

                    // drawing highlighter of cursored line
                    drawRect(
                        color = Color.DarkGray,
                        topLeft = Offset(0f, it.cursor.lineNumber * canvasState.symbolSize.height + translationY),
                        size = Size(canvasState.canvasSize.value.width.toFloat(), canvasState.symbolSize.height)
                    )

                    // drawing text that is visible in the viewport
                    val (startLineIndex, endLineIndex) = canvasState.viewportLinesRange()
                    val viewportVisibleText = canvasState.calculateViewportVisibleText()

                    val measuredText = textMeasurer.measure(
                        text = AnnotatedString(viewportVisibleText),
                        style = editorTextStyle
                    )
                    drawText(
                        measuredText,
                        topLeft = Offset(translationX, translationY + startLineIndex * canvasState.symbolSize.height)
                    )

                    // drawing cursor if it is in the viewport
                    if (it.cursor.lineNumber in startLineIndex until endLineIndex) {
                        val cursorOffset =
                            textViewModel.textModel.totalOffsetOfLine(it.cursor.lineNumber) +
                                    it.cursor.currentLineOffset - textViewModel.textModel.totalOffsetOfLine(startLineIndex)
                        /*println("cursorOffset=$cursorOffset, cursorLine=${it.cursor.lineNumber}, " +
                                "cursorLineOffset=${it.cursor.currentLineOffset}")*/

                        val cursor: Rect = measuredText.getCursorRect(cursorOffset/*it.cursor.offset*/)
                        drawRect(
                            color = Color.White,
                            topLeft = Offset(
                                cursor.left + translationX,
                                cursor.top + translationY + startLineIndex * canvasState.symbolSize.height
                            ),
                            size = cursor.size,
                            style = Stroke(2f)
                        )
                    }
                }
            }

            // focus on canvas on every update of opened file
            LaunchedEffect(activeFileModel) {
                requester.requestFocus()
            }

            // scrollbars
            CanvasVerticalScrollbar(canvasState)
            CanvasHorizontalScrollbar(canvasState)
        }
    }
}