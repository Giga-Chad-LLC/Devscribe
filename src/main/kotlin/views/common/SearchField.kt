package views.common

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.rememberScrollableState
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.runtime.*
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.key.*
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


@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun SearchField(
    settings: Settings,
    onSearchTextChanged: (String) -> Unit,
    searchText: MutableState<String> = remember { mutableStateOf("") },
    onShiftEnterPressed: () -> Unit = {},
    onEnterPressed: () -> Unit = {},
) {
    val focusRequester = remember { FocusRequester() }
    val interactionSource = remember { MutableInteractionSource() }

    val focused by interactionSource.collectIsFocusedAsState()

    val fontSettings = settings.searchFieldFontSettings
    val borderRadius = 6.dp

    val searchFieldState = SearchFieldState(
        horizontalScrollOffset = remember { mutableStateOf(0f) },
        fieldSize = remember { mutableStateOf(IntSize.Zero) },
        searchText = searchText,
    )

    val verticalScrollState = searchFieldState.initializeHorizontalScrollableState()

    fun selectBorderColor(): Color {
        if (focused) return CustomTheme.colors.focusedAccentColor
        return CustomTheme.colors.backgroundMedium
    }

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
        modifier = Modifier
            .focusRequester(focusRequester)
            .focusable(interactionSource = interactionSource)
            .onKeyEvent { keyEvent ->
                var consumed = false

                if (keyEvent.type == KeyEventType.KeyDown && keyEvent.key == Key.Enter) {
                    if (keyEvent.isShiftPressed) onShiftEnterPressed()
                    else onEnterPressed()
                    consumed = true
                }

                consumed
            }
            .onSizeChanged { searchFieldState.fieldSize.value = it }
            .clip(shape = RoundedCornerShape(borderRadius))
            .border(BorderStroke(0.5.dp, selectBorderColor()), RoundedCornerShape(borderRadius))
            .background(CustomTheme.colors.backgroundLight)
            .padding(10.dp, 6.dp)
            .widthIn(150.dp, 500.dp)
            .scrollable(verticalScrollState, Orientation.Horizontal)
    )

}