package views.editor

import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.v2.ScrollbarAdapter
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.DrawStyle
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
import common.TextConstants
import common.ceilToInt
import common.isPrintableSymbolAction
import components.DebounceHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import models.PinnedFileModel
import models.text.Cursor
import org.jetbrains.skia.Font
import viewmodels.TextViewModel
import views.common.design.CustomTheme
import views.common.design.FontSettings
import views.common.design.Settings
import views.common.text.SearchField
import kotlin.math.abs
import kotlin.math.roundToInt


internal const val LINES_COUNT_VERTICAL_OFFSET = 5
internal const val SYMBOLS_COUNT_HORIZONTAL_OFFSET = 5
internal const val LINES_PANEL_RIGHT_PADDING = 10f
internal const val LINES_PANEL_LEFT_PADDING = 40f
internal const val TEXT_CANVAS_LEFT_MARGIN = 8f


internal data class SearchResult(
    val lineIndex: Int,
    val lineOffset: Int,
)

internal data class CanvasState(
    val verticalScrollOffset: MutableState<Float>,
    val horizontalScrollOffset: MutableState<Float>,
    val canvasSize: MutableState<IntSize>,
    val symbolSize: Size,
    val textViewModel: TextViewModel,

    // TODO: move search state into different class
    val isSearchBarVisible: MutableState<Boolean>,
    val searchedText: MutableState<String>,
    val searchResults: SnapshotStateList<SearchResult>,
    var currentSearchResultIndex: MutableState<Int>,
    var searchResultLength: MutableState<Int>,
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

/**
 * Scrolls vertically by the provided lines count.
 *
 * @param[k] number of lines. If positive then scrolls up, if negative scrolls down.
 */
private fun CanvasState.scrollVerticallyByLines(k: Int) {
    scrollVerticallyByOffset(-1f * k * symbolSize.height)
}
private fun CanvasState.scrollVerticallyByOffset(offset: Float) {
    verticalScrollOffset.value = coerceVerticalOffset(verticalScrollOffset.value + offset)
    println("verticalScrollOffset=${verticalScrollOffset.value}")
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

private fun CanvasState.searchTextInFile(uneditedSearchText: String) {
    val searchText = uneditedSearchText.replace(' ', TextConstants.nonBreakingSpaceChar)

    val lines = textViewModel.textModel.textLines()
    searchResults.clear()

    if (searchText.isNotEmpty()) {
        for (index in lines.indices) {
            val line = lines[index]
            var startLineIndex = 0
            var offset = line.indexOf(searchText, startLineIndex, ignoreCase = true)

            while(offset != -1) {
                searchResults.add(SearchResult(index, offset))
                startLineIndex += (offset + searchText.length)
                offset = line.indexOf(searchText, startLineIndex, ignoreCase = true)
            }
        }

        searchResultLength.value = searchText.length
        searchedText.value = searchText
    }
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
    return textViewModel.textModel.textInLinesRange(startLineIndex, endLineIndex)
}

private fun CanvasState.scrollToClosestSearchResult() {
    val (startLineIndex, endLineIndex) = viewportLinesRange()
    val centerLineIndex = (startLineIndex + endLineIndex) / 2

    var index = -1
    var diff = -1

    // TODO: make faster: searchResults are sorted by line index, use lower/upper bound
    for (i in searchResults.indices) {
        if (index == -1 || diff > abs(centerLineIndex - searchResults[i].lineIndex)) {
            index = i
            diff = abs(centerLineIndex - searchResults[i].lineIndex)
        }
    }

    if (index != -1) {
        scrollByKSearchResult(index - currentSearchResultIndex.value)
    }

    /*
    var closestSearchResult: SearchResult? = null
    for (result in searchResults) {
        if (closestSearchResult == null ||
            abs(centerLineIndex - closestSearchResult.lineIndex) > abs(centerLineIndex - result.lineIndex)) {
            closestSearchResult = result
        }
    }

    if (closestSearchResult != null) {
        currentSearchResultIndex.value = closestSearchResult.lineIndex
        scrollVerticallyByLines(centerLineIndex - closestSearchResult.lineIndex)
    }
    */
}

private fun CanvasState.scrollToNextSearchResult() {
    scrollByKSearchResult(1)
}

private fun CanvasState.scrollToPreviousSearchResult() {
    scrollByKSearchResult(-1)
}

private fun CanvasState.scrollByKSearchResult(k: Int) {
    val (startLineIndex, endLineIndex) = viewportLinesRange()
    val centerLineIndex = (startLineIndex + endLineIndex) / 2

    assert(searchResults.isNotEmpty()) { "List must contain search results" }
    val nextSearchResultIndex = (currentSearchResultIndex.value + k + searchResults.size) % searchResults.size
    val nextSearchResult = searchResults[nextSearchResultIndex]

    val linesDifference = centerLineIndex - nextSearchResult.lineIndex

    println("nextSearchResultIndex=${nextSearchResultIndex} " +
            "nextSearchResult=${nextSearchResult} " +
            "lineDiff=${linesDifference}")

    currentSearchResultIndex.value = nextSearchResultIndex
    scrollVerticallyByLines(linesDifference)
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
// TODO: move lines panel into separate file
// TODO: move some extensions into separate file
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
                    if (keyEvent.isCtrlPressed) {
                        // CTRL + ↑ scrolls the canvas by 1 line up
                        canvasState.scrollVerticallyByLines(1)
                    }
                    else {
                        textViewModel.directionUp()
                    }
                    consumed = true
                }
                else if (keyEvent.type == KeyEventType.KeyDown && keyEvent.key == Key.DirectionRight) {
                    if (keyEvent.isCtrlPressed) {
                        // CTRL + → forwards cursor to the end of next word
                        textViewModel.forwardToNextWord()
                    }
                    else {
                        textViewModel.directionRight()
                    }
                    consumed = true
                }
                else if (keyEvent.type == KeyEventType.KeyDown && keyEvent.key == Key.DirectionDown) {
                    // CTRL + ↓ scrolls the canvas by 1 line down
                    if (keyEvent.isCtrlPressed) {
                        canvasState.scrollVerticallyByLines(-1)
                    }
                    else {
                        textViewModel.directionDown()
                    }
                    consumed = true
                }
                else if (keyEvent.type == KeyEventType.KeyDown && keyEvent.key == Key.DirectionLeft) {
                    if (keyEvent.isCtrlPressed) {
                        // CTRL + ← backwards cursor to the start of previous word
                        textViewModel.backwardToPreviousWord()
                    }
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
                else if (keyEvent.type == KeyEventType.KeyDown && keyEvent.isCtrlPressed && keyEvent.key == Key.F) {
                    canvasState.isSearchBarVisible.value = true
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
    var previousCursorState by remember { mutableStateOf(Cursor(textViewModel.cursor)) }
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

    // TODO: rename to editorState
    val canvasState = CanvasState(
        verticalScrollOffset = remember { mutableStateOf(0f) },
        horizontalScrollOffset = remember { mutableStateOf(0f) },
        canvasSize = remember { mutableStateOf(IntSize.Zero) },
        symbolSize = remember(settings.fontSettings) {
            getSymbolSize(fontFamilyResolver, textMeasurer, settings.fontSettings, density)
        },
        textViewModel = textViewModel,

        // TODO: move into another state object
        isSearchBarVisible = remember { mutableStateOf(false) },
        searchedText = remember { mutableStateOf("") },
        searchResults = remember { mutableStateListOf() },
        currentSearchResultIndex = remember { mutableStateOf(0) },
        searchResultLength = remember { mutableStateOf(0) }
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

    // TODO: move into separate function
    val searchTextInFileDebounced = remember {
        DebounceHandler(500, coroutineScope) { searchText: String ->
            canvasState.searchTextInFile(searchText)
            canvasState.scrollToClosestSearchResult()
        }
    }

    if (canvasState.isSearchBarVisible.value) {
        LaunchedEffect(canvasState.textViewModel.textModel.textLines()) {
            canvasState.searchTextInFile(canvasState.searchedText.value)
        }
    }

    Column {
        // search bar
        if (canvasState.isSearchBarVisible.value) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                SearchField(
                    settings,
                    onSearchTextChanged = { text ->
                        searchTextInFileDebounced.run(text)
                    }
                )

                Text("${canvasState.currentSearchResultIndex.value + 1}/${canvasState.searchResults.size}")

                Button(onClick = {
                    canvasState.scrollToPreviousSearchResult()
                }) {
                    Text("Prev")
                }
                Button(onClick = { canvasState.scrollToNextSearchResult() }) {
                    Text("Next")
                }
            }
        }

        // text area
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

                        // TODO: move into method
                        // scrolling on cursor getting out of viewport
                        if (previousCursorState.offset != it.cursor.offset) {
                            println("previousCursorState=${previousCursorState} " +
                                    "currentCursor=${it.cursor}")
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
                            println("updatedPrev=${previousCursorState}")
                        }

                        val translationX = -horizontalOffset + TEXT_CANVAS_LEFT_MARGIN
                        val translationY = -verticalOffset

                        // drawing highlighter of cursored line
                        drawRect(
                            color = Color.DarkGray,
                            topLeft = Offset(0f, it.cursor.lineNumber * canvasState.symbolSize.height + translationY),
                            size = Size(canvasState.canvasSize.value.width.toFloat(), canvasState.symbolSize.height)
                        )

                        // TODO: temporal, move to function
                        // TODO: highlight all occurrences but mark the selected one with border (use LaunchEffect)
                        // highlighting searched results
                        if (canvasState.isSearchBarVisible.value) {
                            for (index in canvasState.searchResults.indices) {
                                val searchResult = canvasState.searchResults[index]

                                val offsetX = searchResult.lineOffset * canvasState.symbolSize.width + translationX
                                val offsetY = searchResult.lineIndex * canvasState.symbolSize.height + translationY

                                val highlighterWidth = canvasState.searchResultLength.value * canvasState.symbolSize.width
                                val highlighterHeight = canvasState.symbolSize.height

                                drawRect(
                                    color = Color(0xFF32593D),
                                    topLeft = Offset(offsetX, offsetY),
                                    size = Size(highlighterWidth, highlighterHeight)
                                )

                                // highlighting with stroke currently selected searched result
                                if (canvasState.currentSearchResultIndex.value == index) {
                                    drawRect(
                                        color = Color.White,
                                        topLeft = Offset(offsetX, offsetY),
                                        style = Stroke(0.5.dp.toPx()),
                                        size = Size(highlighterWidth, highlighterHeight)
                                    )
                                }
                            }
                        }

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
}