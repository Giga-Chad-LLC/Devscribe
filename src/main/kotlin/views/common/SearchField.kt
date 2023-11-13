package views.common

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.rememberScrollableState
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import views.design.CustomTheme
import views.design.Settings



internal data class SearchFieldState (
    var horizontalScrollOffset: MutableState<Float>,
    var fieldSize: MutableState<IntSize>,
    var searchText: MutableState<String>,
)


@Composable
private fun SearchFieldState.initializeHorizontalScrollableState() =
    rememberScrollableState { delta ->
    val newScrollOffset = horizontalScrollOffset.value - delta
    val scrollConsumed = horizontalScrollOffset.value - newScrollOffset

    horizontalScrollOffset.value = newScrollOffset
    scrollConsumed
}


@Composable
fun SearchField(
    settings: Settings,
    onSearchTextChanged: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val fontSettings = settings.searchFieldFontSettings
    val borderRadius = 6.dp

    val searchFieldState = SearchFieldState(
        horizontalScrollOffset = remember { mutableStateOf(0f) },
        fieldSize = remember { mutableStateOf(IntSize.Zero) },
        searchText = remember { mutableStateOf("") },
    )

    val verticalScrollState = searchFieldState.initializeHorizontalScrollableState()

    // TODO: implement scrollbar
    BasicTextField(
        value = searchFieldState.searchText.value,
        onValueChange = {
            searchFieldState.searchText.value = it
            onSearchTextChanged(searchFieldState.searchText.value)
        },
        singleLine = true,
        textStyle = TextStyle(
            color = fontSettings.fontColor,
            fontFamily = fontSettings.fontFamily,
            fontSize = fontSettings.fontSize,
            fontWeight = fontSettings.fontWeight,
        ),
        cursorBrush = SolidColor(fontSettings.fontColor),
        modifier = modifier
            // TODO: change styles & make focusable
            .onSizeChanged { searchFieldState.fieldSize.value = it }
            .clip(shape = RoundedCornerShape(borderRadius))
            .border(BorderStroke(1.dp, CustomTheme.colors.backgroundMedium), RoundedCornerShape(borderRadius))
            .background(CustomTheme.colors.backgroundLight)
            .padding(10.dp, 8.dp)
            .widthIn(120.dp, 500.dp)
            .scrollable(verticalScrollState, Orientation.Horizontal)
    )

}