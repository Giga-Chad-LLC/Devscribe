package views.editor

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.snapshots.SnapshotStateList


internal data class SearchResult(
    val lineIndex: Int,
    val lineOffset: Int,
)


internal enum class SearchStatus {
    IDLE,
    RESULTS_FOUND,
    NO_RESULTS_FOUND,
}


internal data class SearchState(
    val searchStatus: MutableState<SearchStatus>,
    val searchedText: MutableState<String>,
    val searchResults: SnapshotStateList<SearchResult>,
    var currentSearchResultIndex: MutableState<Int>,
    var searchResultLength: MutableState<Int>,
)