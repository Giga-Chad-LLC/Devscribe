package views.editor

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import views.common.CustomIconButton
import views.common.SearchField
import views.design.Settings


private fun selectSearchResultMessage(editorState: EditorState): String {
    val searchState = editorState.searchState

    return when (searchState.searchStatus.value) {
        SearchStatus.IDLE -> "0 results"
        SearchStatus.RESULTS_FOUND -> "${searchState.currentSearchResultIndex.value + 1}/${searchState.searchResults.size}"
        SearchStatus.NO_RESULTS_FOUND -> "No results"
    }
}


@Composable
internal fun SearchBar(
    settings: Settings,
    onSearchTextChanged: (String) -> Unit,
    editorState: EditorState,
) {
    val enabled = (editorState.searchState.searchStatus.value == SearchStatus.RESULTS_FOUND)
    val scrollState = rememberScrollState()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp, 4.dp)
            .horizontalScroll(scrollState),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        SearchField(
            settings,
            onSearchTextChanged = onSearchTextChanged,
            searchText = editorState.searchState.searchedText,
            onEnterPressed = { if (enabled) editorState.scrollToNextSearchResult() },
            onShiftEnterPressed = { if (enabled) editorState.scrollToPreviousSearchResult() }
        )

        Text(
            text = selectSearchResultMessage(editorState),
            style = TextStyle(
                fontSize = settings.searchBarFontSettings.fontSize,
                fontWeight = settings.searchBarFontSettings.fontWeight,
                color = settings.searchBarFontSettings.fontColor,
                fontFamily = settings.searchBarFontSettings.fontFamily,
            ),
            modifier = Modifier.padding(12.dp, 0.dp, 0.dp, 0.dp)
        )

        CustomIconButton(
            onClick = { editorState.scrollToPreviousSearchResult() },
            enabled = (enabled /*canvasState.searchResults.size > 0*/),
            imageVector = Icons.Filled.KeyboardArrowUp,
            contentDescription = "Previous search result",
            modifier = Modifier.padding(25.dp, 0.dp, 0.dp, 0.dp)
        )

        CustomIconButton(
            onClick = { editorState.scrollToNextSearchResult() },
            enabled = (enabled /*canvasState.searchResults.size > 0*/),
            imageVector = Icons.Filled.KeyboardArrowDown,
            contentDescription = "Next search result",
            modifier = Modifier.padding(8.dp, 0.dp, 0.dp, 0.dp)
        )
    }
}