package views.editor

import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.v2.ScrollbarAdapter
import androidx.compose.runtime.*
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
import components.DebounceHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import models.PinnedFileModel
import models.text.Cursor
import org.jetbrains.skia.Font
import viewmodels.TextViewModel
import views.design.CustomTheme
import views.design.FontSettings
import views.design.Settings
import views.editor.SearchState.IDLE


internal const val LINES_COUNT_VERTICAL_OFFSET = 5
internal const val SYMBOLS_COUNT_HORIZONTAL_OFFSET = 5
internal const val LINES_PANEL_RIGHT_PADDING = 10f
internal const val LINES_PANEL_LEFT_PADDING = 40f
internal const val TEXT_CANVAS_LEFT_MARGIN = 8f


internal data class SearchResult(
    val lineIndex: Int,
    val lineOffset: Int,
)

internal enum class SearchState {
    IDLE,
    RESULTS_FOUND,
    NO_RESULTS_FOUND,
}

@Composable
private fun BoxScope.CanvasVerticalScrollbar(editorState: EditorState) {
    VerticalScrollbar(
        object : ScrollbarAdapter {
            override val contentSize: Double
                get() = editorState.getMaxVerticalScrollOffset().toDouble() + viewportSize
            override val scrollOffset: Double
                get() = editorState.verticalScrollOffset.value.toDouble()
            override val viewportSize: Double
                get() = editorState.canvasSize.value.height.toDouble()

            override suspend fun scrollTo(scrollOffset: Double) {
                editorState.verticalScrollOffset.value =
                    editorState.coerceVerticalOffset(scrollOffset.toFloat())
            }
        },
        Modifier.align(Alignment.CenterEnd)
    )
}

@Composable
private fun BoxScope.CanvasHorizontalScrollbar(editorState: EditorState) {
    HorizontalScrollbar(
        object : ScrollbarAdapter {
            override val contentSize: Double
                get() = editorState.getMaxHorizontalScrollOffset() + viewportSize
            override val scrollOffset: Double
                get() = editorState.horizontalScrollOffset.value.toDouble()
            override val viewportSize: Double
                get() = editorState.canvasSize.value.width.toDouble()

            override suspend fun scrollTo(scrollOffset: Double) {
                editorState.horizontalScrollOffset.value = editorState.coerceHorizontalOffset(scrollOffset.toFloat())
            }

        },
        Modifier.align(Alignment.BottomCenter)
    )
}

private fun DrawScope.highlightSearchResults(
    settings: Settings,
    editorState: EditorState,
    initialOffset: Offset
) {
    for (index in editorState.searchResults.indices) {
        val searchResult = editorState.searchResults[index]

        val offsetX = initialOffset.x + searchResult.lineOffset * editorState.symbolSize.width //translationX
        val offsetY = initialOffset.y + searchResult.lineIndex * editorState.symbolSize.height // translationY

        val highlighterWidth = editorState.searchResultLength.value * editorState.symbolSize.width
        val highlighterHeight = editorState.symbolSize.height

        drawRect(
            color = settings.editorSettings.highlightingOptions.searchResultColor,
            topLeft = Offset(offsetX, offsetY),
            size = Size(highlighterWidth, highlighterHeight)
        )

        // highlighting with stroke currently selected searched result
        if (editorState.currentSearchResultIndex.value == index) {
            drawRect(
                color = settings.editorSettings.highlightingOptions.selectedSearchResultColor,
                topLeft = Offset(offsetX, offsetY),
                style = Stroke(1.dp.toPx()),
                size = Size(highlighterWidth, highlighterHeight)
            )
        }
    }
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
    editorState: EditorState,
    horizontalOffset: Float,
    cursor: Cursor,
    horizontalScrollState: ScrollableState
) {
    scrollOnCursorOutOfCanvasViewport(
        coroutineScope = coroutineScope,
        cursorPosition = cursor.currentLineOffset,
        symbolSizeDimension = editorState.symbolSize.width,
        canvasDimensionOffset = horizontalOffset,
        viewportSize = editorState.canvasSize.value.width,
        scrollState = horizontalScrollState
    )
}

private fun scrollVerticallyOnCursorOutOfCanvasViewport(
    coroutineScope: CoroutineScope,
    editorState: EditorState,
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
    val ceiledSymbolHeight = editorState.symbolSize.height.ceilToInt()
    val truncatedViewportHeight = editorState.canvasSize.value.height - ceiledSymbolHeight

    scrollOnCursorOutOfCanvasViewport(
        coroutineScope = coroutineScope,
        cursorPosition = cursor.lineNumber,
        symbolSizeDimension = editorState.symbolSize.height,
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
    editorState: EditorState,
    settings: Settings,
    density: Float
): Size {
    val lineSymbolSize = getSymbolSize(
        fontFamilyResolver,
        textMeasurer,
        settings.editorSettings.linesPanel.fontSettings,
        density
    )

    val maxLineNumber = editorState.textViewModel.textModel.linesCount()

    return Size(
        // left & right paddings + width of the longest line number
        LINES_PANEL_RIGHT_PADDING + maxLineNumber.toString().length * lineSymbolSize.width + LINES_PANEL_LEFT_PADDING,
        // canvas width
        editorState.canvasSize.value.height.toFloat()
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
    editorState: EditorState,
    cursor: Cursor,
    settings: Settings,
    editorFocused: Boolean,
    density: Float
) {
    val linesPanelSettings = settings.editorSettings.linesPanel

    val lineSymbolSize = getSymbolSize(
        fontFamilyResolver,
        textMeasurer,
        linesPanelSettings.fontSettings,
        density,
    )

    val (startLineIndex, endLineIndex) = editorState.viewportLinesRange()

    val startLineNumber = startLineIndex + 1
    val maxLineNumber = endLineIndex

    /**
     * Starting with centering offsets that place the line number in the center of code symbol.
     *
     * Assuming that font size of line number <= font size of code symbol.
     */
    val centeringOffsetX = (editorState.symbolSize.width - lineSymbolSize.width) / 2
    val centeringOffsetY = (editorState.symbolSize.height - lineSymbolSize.height) / 2

    /**
     * offsetY starts from the offset of scrolled up lines (i.e. first 'startLineIndex' line)
     */
    var offsetY = centeringOffsetY + startLineIndex * editorState.symbolSize.height

    val linesPanelSize = determineLinesPanelSize(
        fontFamilyResolver,
        textMeasurer,
        editorState,
        settings,
        density,
    )

    /**
     * Drawing a split line on the right border of the lines panel
     */
    drawRect(
        color = if (editorFocused) linesPanelSettings.editorFocusedSplitLineColor else linesPanelSettings.splitLineColor,
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

        offsetY += editorState.symbolSize.height
    }
}


/**
 * Handles mouse click on the canvas by changing cursor position
 */
private fun Modifier.pointerInput(focusRequester: FocusRequester, editorState: EditorState): Modifier {
    return this.then(
        Modifier
            .pointerInput(Unit) {
                detectTapGestures(onPress = { offset ->
                    // focusing on the canvas
                    focusRequester.requestFocus()

                    // moving cursor to the mouse click position
                    val (lineIndex, lineOffset) = editorState.canvasOffsetToCursorPosition(offset)
                    editorState.textViewModel.textModel.changeCursorPosition(lineIndex, lineOffset)
                })
            }
    )
}

/**
 * Handles keyboard inputs by executing commands of TextViewModel
 */
@OptIn(ExperimentalComposeUiApi::class)
private fun Modifier.handleKeyboardInput(editorState: EditorState): Modifier {
    val textViewModel = editorState.textViewModel

    return this.then(
        Modifier
            .onKeyEvent { keyEvent ->
                var consumed = false

                if (keyEvent.type == KeyEventType.KeyDown && keyEvent.key == Key.Escape) {
                    editorState.isSearchBarVisible.value = false
                    consumed = true
                }
                else if (keyEvent.type == KeyEventType.KeyDown && keyEvent.key == Key.Backspace) {
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
                        editorState.scrollVerticallyByLines(1)
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
                        editorState.scrollVerticallyByLines(-1)
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
                    editorState.isSearchBarVisible.value = true
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
    val editorState = EditorState(
        verticalScrollOffset = remember { mutableStateOf(0f) },
        horizontalScrollOffset = remember { mutableStateOf(0f) },
        canvasSize = remember { mutableStateOf(IntSize.Zero) },
        symbolSize = remember(settings.fontSettings) {
            getSymbolSize(fontFamilyResolver, textMeasurer, settings.fontSettings, density)
        },
        textViewModel = textViewModel,

        // TODO: move into another state object
        isSearchBarVisible = remember { mutableStateOf(false) },
        searchState = remember { mutableStateOf(IDLE) },
        searchedText = remember { mutableStateOf("") },
        searchResults = remember { mutableStateListOf() },
        currentSearchResultIndex = remember { mutableStateOf(0) },
        searchResultLength = remember { mutableStateOf(0) }
    )

    val verticalScrollState = editorState.initializeVerticalScrollbar()
    val horizontalScrollState = editorState.initializeHorizontalScrollbar()

    val linesPanelSize = determineLinesPanelSize(
        fontFamilyResolver,
        textMeasurer,
        editorState,
        settings,
        density,
    )

    val editorInteractionSource = remember { MutableInteractionSource() }
    val editorFocused by editorInteractionSource.collectIsFocusedAsState()

    val searchTextInFileDebounced = remember {
        DebounceHandler(500, coroutineScope) { searchText: String ->
            editorState.searchTextInFile(searchText)
            editorState.scrollToClosestSearchResult()
        }
    }

    if (editorState.isSearchBarVisible.value) {
        // redraw search results highlighters after text modification
        LaunchedEffect(editorState.textViewModel.textModel.textLines()) {
            editorState.searchTextInFile(editorState.searchedText.value)
        }
    }

    Column {
        // search bar
        if (editorState.isSearchBarVisible.value) {
            SearchBar(
                settings = settings,
                onSearchTextChanged =  { text -> searchTextInFileDebounced.run(text) },
                editorState = editorState,
            )
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
                    scrollOffsetY = -editorState.verticalScrollOffset.value,
                    editorState = editorState,
                    cursor = textViewModel.cursor,
                    settings = settings,
                    editorFocused = editorFocused,
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
                        .focusable(interactionSource = editorInteractionSource)
                        .handleKeyboardInput(editorState)
                        .onSizeChanged { editorState.canvasSize.value = it }
                        .pointerInput(requester, editorState)
                        .scrollable(verticalScrollState, Orientation.Vertical)
                        .scrollable(horizontalScrollState, Orientation.Horizontal)
                        .background(CustomTheme.colors.backgroundDark)
                        .clipToBounds()
                        .fillMaxSize()
                ) {
                    textViewModel.let {
                        val verticalOffset = editorState.verticalScrollOffset.value
                        val horizontalOffset = editorState.horizontalScrollOffset.value

                        // TODO: move into method
                        // scrolling on cursor getting out of viewport
                        if (previousCursorState.offset != it.cursor.offset) {
                            scrollHorizontallyOnCursorOutOfCanvasViewport(
                                coroutineScope = coroutineScope,
                                editorState = editorState,
                                horizontalOffset = horizontalOffset,
                                cursor = it.cursor,
                                horizontalScrollState = horizontalScrollState,
                            )

                            scrollVerticallyOnCursorOutOfCanvasViewport(
                                coroutineScope = coroutineScope,
                                editorState = editorState,
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
                            topLeft = Offset(0f, it.cursor.lineNumber * editorState.symbolSize.height + translationY),
                            size = Size(editorState.canvasSize.value.width.toFloat(), editorState.symbolSize.height)
                        )

                        // TODO: temporal, move to function
                        // TODO: highlight all occurrences but mark the selected one with border (use LaunchEffect)
                        // highlighting searched results
                        if (editorState.isSearchBarVisible.value) {
                            highlightSearchResults(
                                settings,
                                editorState,
                                Offset(translationX, translationY)
                            )
                        }

                        // drawing text that is visible in the viewport
                        val (startLineIndex, endLineIndex) = editorState.viewportLinesRange()
                        val viewportVisibleText = editorState.calculateViewportVisibleText()

                        val measuredText = textMeasurer.measure(
                            text = AnnotatedString(viewportVisibleText),
                            style = editorTextStyle
                        )

                        drawText(
                            measuredText,
                            topLeft = Offset(translationX, translationY + startLineIndex * editorState.symbolSize.height)
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
                                    cursor.top + translationY + startLineIndex * editorState.symbolSize.height
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
                CanvasVerticalScrollbar(editorState)
                CanvasHorizontalScrollbar(editorState)
            }
        }
    }
}