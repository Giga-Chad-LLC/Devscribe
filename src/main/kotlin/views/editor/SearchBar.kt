package views.editor

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import views.common.CustomIconButton
import views.common.SearchField
import views.design.Settings


private fun selectSearchResultMessage(canvasState: CanvasState): String {
    return when (canvasState.searchState.value) {
        SearchState.IDLE -> "0 results"
        SearchState.RESULTS_FOUND -> "${canvasState.currentSearchResultIndex.value + 1}/${canvasState.searchResults.size}"
        SearchState.NO_RESULTS_FOUND -> "No results"
    }
}


@Composable
fun SearchBar(
    settings: Settings,
    onSearchTextChanged: (String) -> Unit,
    canvasState: CanvasState,
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(8.dp, 3.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        SearchField(
            settings,
            onSearchTextChanged = onSearchTextChanged,
            searchText = canvasState.searchedText,
        )

        Text(
            text = selectSearchResultMessage(canvasState),
            style = TextStyle(
                fontSize = settings.searchBarFontSettings.fontSize,
                fontWeight = settings.searchBarFontSettings.fontWeight,
                color = settings.searchBarFontSettings.fontColor,
                fontFamily = settings.searchBarFontSettings.fontFamily,
            ),
            modifier = Modifier.padding(12.dp, 0.dp, 0.dp, 0.dp)
        )

        CustomIconButton(
            onClick = { canvasState.scrollToPreviousSearchResult() },
            enabled = (canvasState.searchResults.size > 0),
            imageVector = Icons.Filled.KeyboardArrowUp,
            contentDescription = "Previous search result",
            modifier = Modifier.padding(25.dp, 0.dp, 0.dp, 0.dp)
        )

        CustomIconButton(
            onClick = { canvasState.scrollToNextSearchResult() },
            enabled = (canvasState.searchResults.size > 0),
            imageVector = Icons.Filled.KeyboardArrowDown,
            contentDescription = "Next search result",
            modifier = Modifier.padding(8.dp, 0.dp, 0.dp, 0.dp)
        )
    }
}