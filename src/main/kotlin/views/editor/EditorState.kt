package views.editor

import androidx.compose.foundation.gestures.rememberScrollableState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.unit.IntSize
import common.TextConstants
import common.ceilToInt
import viewmodels.TextViewModel
import kotlin.math.abs
import kotlin.math.roundToInt



internal data class EditorState(
    val verticalScrollOffset: MutableState<Float>,
    val horizontalScrollOffset: MutableState<Float>,
    val canvasSize: MutableState<IntSize>,
    val symbolSize: Size,
    val textViewModel: TextViewModel,

    val isSearchBarVisible: MutableState<Boolean>,
    val searchState: SearchState,
)


internal fun EditorState.getMaxVerticalScrollOffset(): Float {
    /**
     * N := LINES_COUNT_VERTICAL_OFFSET
     *
     * Subtracting N from total lines count to make last N lines visible at the lowest position of vertical scroll
     */
    val maxLinesNumber = (textViewModel.textModel.linesCount() - LINES_COUNT_VERTICAL_OFFSET).coerceAtLeast(0)
    return maxLinesNumber * symbolSize.height
}


internal fun EditorState.getMaxHorizontalScrollOffset(): Float {
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
internal fun EditorState.scrollVerticallyByLines(k: Int) {
    scrollVerticallyByOffset(-1f * k * symbolSize.height)
}


internal fun EditorState.scrollVerticallyByOffset(offset: Float) {
    verticalScrollOffset.value = coerceVerticalOffset(verticalScrollOffset.value + offset)
}


internal fun EditorState.coerceVerticalOffset(offset: Float): Float {
    return offset
        .coerceAtLeast(0f)
        .coerceAtMost(getMaxVerticalScrollOffset())
}


internal fun EditorState.coerceHorizontalOffset(offset: Float): Float {
    return offset
        .coerceAtLeast(0f)
        .coerceAtMost(getMaxHorizontalScrollOffset())
}


internal fun EditorState.scrollHorizontallyByOffset(offset: Float) {
    horizontalScrollOffset.value = coerceHorizontalOffset(horizontalScrollOffset.value + offset)
}


internal fun EditorState.searchTextInFile(uneditedSearchText: String) {
    val searchText = uneditedSearchText.replace(' ', TextConstants.nonBreakingSpaceChar)

    val lines = textViewModel.textModel.textLines()
    searchState.searchResults.clear()

    if (searchText.isNotEmpty()) {
        for (index in lines.indices) {
            val line = lines[index]
            var startLineIndex = 0
            var offset = line.indexOf(searchText, startLineIndex, ignoreCase = true)

            while(offset != -1) {
                searchState.searchResults.add(SearchResult(index, offset))
                startLineIndex = (offset + searchText.length)
                offset = line.indexOf(searchText, startLineIndex, ignoreCase = true)
            }
        }

        searchState.searchResultLength.value = searchText.length
        searchState.searchedText.value = searchText

        if (searchState.searchResults.isNotEmpty()) {
            searchState.searchStatus.value = SearchStatus.RESULTS_FOUND
        }
        else {
            searchState.searchStatus.value = SearchStatus.NO_RESULTS_FOUND
        }
    }
    else {
        searchState.searchStatus.value = SearchStatus.IDLE
    }
}


/**
 * Accepts canvas offset (e.g. offset of mouse click event) and maps it to (lineIndex, lineOffset) pair.
 */
internal fun EditorState.canvasOffsetToCursorPosition(offset: Offset) : Pair<Int, Int> {
    val lineIndex = ((offset.y + verticalScrollOffset.value) / symbolSize.height)
        .toInt().coerceAtMost(textViewModel.textModel.linesCount() - 1)

    /*println("symbolWidth=${symbolSize.width}")
    println("lineOffsetFloat=${(offset.x + horizontalScrollOffset.value - TEXT_CANVAS_LEFT_MARGIN) / symbolSize.width}")*/

    val lineOffset = ((offset.x + horizontalScrollOffset.value - TEXT_CANVAS_LEFT_MARGIN) / symbolSize.width)
        .roundToInt().coerceAtLeast(0).coerceAtMost(textViewModel.textModel.lineLength(lineIndex))

    return lineIndex to lineOffset
}


internal fun EditorState.viewportLinesRange(): Pair<Int, Int> {
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


internal fun EditorState.calculateViewportVisibleText(): String {
    val (startLineIndex, endLineIndex) = viewportLinesRange()
    return textViewModel.textModel.textInLinesRange(startLineIndex, endLineIndex)
}


internal fun EditorState.scrollToClosestSearchResult() {
    val (startLineIndex, endLineIndex) = viewportLinesRange()
    val centerLineIndex = (startLineIndex + endLineIndex) / 2

    var index = -1
    var diff = -1

    // TODO: make faster: searchResults are sorted by line index, use lower/upper bound (abstract searching algorithm)
    for (i in searchState.searchResults.indices) {
        if (index == -1 || diff > abs(centerLineIndex - searchState.searchResults[i].lineIndex)) {
            index = i
            diff = abs(centerLineIndex - searchState.searchResults[i].lineIndex)
        }
    }

    if (index != -1) {
        scrollByKSearchResults(index - searchState.currentSearchResultIndex.value)
    }
}


internal fun EditorState.scrollToNextSearchResult() {
    scrollByKSearchResults(1)
}


internal fun EditorState.scrollToPreviousSearchResult() {
    scrollByKSearchResults(-1)
}


internal fun EditorState.scrollByKSearchResults(k: Int) {
    val (startLineIndex, endLineIndex) = viewportLinesRange()
    val centerLineIndex = (startLineIndex + endLineIndex) / 2

    searchState.run {
        val size = searchResults.size
        assert(size > 0) { "List must be non-empty" }

        val nextSearchResultIndex = (currentSearchResultIndex.value + k + size) % size
        val nextSearchResult = searchResults[nextSearchResultIndex]

        val linesDifference = centerLineIndex - nextSearchResult.lineIndex

        /*println("nextSearchResultIndex=${nextSearchResultIndex} " +
                "nextSearchResult=${nextSearchResult} " +
                "lineDiff=${linesDifference}")*/

        currentSearchResultIndex.value = nextSearchResultIndex
        scrollVerticallyByLines(linesDifference)

        // scrolling horizontally to place the result inside viewport
        val resultStartHorizontalOffset = TEXT_CANVAS_LEFT_MARGIN + nextSearchResult.lineOffset * symbolSize.width
        val resultEndHorizontalOffset = resultStartHorizontalOffset + searchedText.value.length * symbolSize.width

        if (resultStartHorizontalOffset < horizontalScrollOffset.value) {
            val offset = horizontalScrollOffset.value - resultStartHorizontalOffset
            scrollHorizontallyByOffset(-offset)
        }
        else if (resultEndHorizontalOffset > horizontalScrollOffset.value + canvasSize.value.width) {
            val offset = resultEndHorizontalOffset - (horizontalScrollOffset.value + canvasSize.value.width)
            scrollHorizontallyByOffset(offset)
        }
    }
}


@Composable
internal fun EditorState.initializeVerticalScrollbar() = rememberScrollableState { delta ->
    val newScrollOffset = coerceVerticalOffset(verticalScrollOffset.value - delta)
    val scrollConsumed = verticalScrollOffset.value - newScrollOffset
    verticalScrollOffset.value = newScrollOffset
    scrollConsumed
}


@Composable
internal fun EditorState.initializeHorizontalScrollbar() = rememberScrollableState { delta ->
    val newScrollOffset = coerceHorizontalOffset(horizontalScrollOffset.value - delta)
    val scrollConsumed = horizontalScrollOffset.value - newScrollOffset
    horizontalScrollOffset.value = newScrollOffset
    scrollConsumed
}
