package views.editor

import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.v2.ScrollbarAdapter
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
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.key.*
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFontFamilyResolver
import androidx.compose.ui.text.*
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import common.ceilToInt
import common.isPrintableSymbolAction
import components.DebounceHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import models.PinnedFileModel
import models.text.Cursor
import viewmodels.TextViewModel
import views.design.CustomTheme
import views.design.Settings


internal const val LINES_COUNT_VERTICAL_OFFSET = 5
internal const val SYMBOLS_COUNT_HORIZONTAL_OFFSET = 5
internal const val LINES_PANEL_RIGHT_PADDING = 10f
internal const val LINES_PANEL_LEFT_PADDING = 40f
internal const val TEXT_CANVAS_LEFT_MARGIN = 8f



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
    val searchState = editorState.searchState

    for (index in searchState.searchResults.indices) {
        val searchResult = searchState.searchResults[index]

        val offsetX = initialOffset.x + searchResult.lineOffset * editorState.symbolSize.width //translationX
        val offsetY = initialOffset.y + searchResult.lineIndex * editorState.symbolSize.height // translationY

        val highlighterWidth = searchState.searchResultLength.value * editorState.symbolSize.width
        val highlighterHeight = editorState.symbolSize.height

        drawRect(
            color = settings.editorSettings.highlightingOptions.searchResultColor,
            topLeft = Offset(offsetX, offsetY),
            size = Size(highlighterWidth, highlighterHeight)
        )

        // highlighting with stroke currently selected searched result
        if (searchState.currentSearchResultIndex.value == index) {
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
     *
     * TODO: maybe not the best solution
     */
    textViewModel.updateActiveFileModel(activeFileModel)

    val fontFamilyResolver = LocalFontFamilyResolver.current
    val density = LocalDensity.current.density

    val editorState = EditorState(
        verticalScrollOffset = remember { mutableStateOf(0f) },
        horizontalScrollOffset = remember { mutableStateOf(0f) },
        canvasSize = remember { mutableStateOf(IntSize.Zero) },
        symbolSize = remember(settings.fontSettings) {
            getSymbolSize(fontFamilyResolver, textMeasurer, settings.fontSettings, density)
        },
        textViewModel = textViewModel,

        isSearchBarVisible = remember { mutableStateOf(false) },
        searchState = SearchState(
            searchStatus = remember { mutableStateOf(SearchStatus.IDLE) },
            searchedText = remember { mutableStateOf("") },
            searchResults = remember { mutableStateListOf() },
            currentSearchResultIndex = remember { mutableStateOf(0) },
            // length in symbols of a string that is a search result
            searchResultLength = remember { mutableStateOf(0) },
        )
    )

    val verticalScrollState = editorState.initializeVerticalScrollbar()
    val horizontalScrollState = editorState.initializeHorizontalScrollbar()

    val editorInteractionSource = remember { MutableInteractionSource() }
    val editorFocused by editorInteractionSource.collectIsFocusedAsState()

    val searchTextInFileDebounced = remember {
        DebounceHandler(500, coroutineScope) { searchText: String ->
            editorState.searchTextInFile(searchText)
            editorState.scrollToClosestSearchResult()
        }
    }

    // highlighting search results only when search bar is opened
    if (editorState.isSearchBarVisible.value) {
        // redraw search results highlighters after text modification
        LaunchedEffect(editorState.textViewModel.textModel.textLines()) {
            editorState.searchTextInFile(editorState.searchState.searchedText.value)
        }
    }

    // focus on canvas on every update of opened file
    LaunchedEffect(activeFileModel) {
        requester.requestFocus()
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
            LinesPanel(
                verticalScrollState,
                settings,
                fontFamilyResolver,
                textMeasurer,
                editorState,
                textViewModel,
                density,
                editorFocused,
            )

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

                        // highlighting searched results only if search bar is opened
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
                            val cursorOffset = textViewModel.textModel.totalOffsetOfLine(it.cursor.lineNumber) -
                                        textViewModel.textModel.totalOffsetOfLine(startLineIndex) + it.cursor.currentLineOffset

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

                // scrollbars
                CanvasVerticalScrollbar(editorState)
                CanvasHorizontalScrollbar(editorState)
            }
        }
    }
}