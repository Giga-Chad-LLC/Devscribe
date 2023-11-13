package views.design

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp

// TODO: move settings into categories represented by classes (as with editor settings)
class Settings {
    var fontSettings by mutableStateOf(FontSettings())
    val searchFieldFontSettings by mutableStateOf(
        FontSettings(
            fontSize = 14.sp,
            fontWeight = FontWeight.Light,
            fontColor = Color.White,
            fontFamily = FontFamily.SansSerif,
        )
    )
    val searchBarFontSettings by mutableStateOf(
        FontSettings(
            fontSize = 12.sp,
            fontWeight = FontWeight.Light,
            fontColor = CustomTheme.colors.primaryColor,
            fontFamily = FontFamily.SansSerif,
        )
    )
    val editorSettings by mutableStateOf(EditorSettings())
}

class EditorSettings {
    val linesPanel by mutableStateOf(LinesPanel())
    val highlightingOptions by mutableStateOf(HighlightingOptions())
}

// TODO: move 'Color(90, 89, 86)' into separate variable
class LinesPanel {
    val fontSettings by mutableStateOf(
        FontSettings(
            fontColor = Color(90, 89, 86), // gray
            fontSize = 16.sp,
            fontWeight = FontWeight.Light
        )
    )
    val backgroundColor = Color(49, 51,53)
    val splitLineColor = Color(79, 79, 79)
    val editorFocusedSplitLineColor = Color(92, 92, 92) // light dray
    val cursoredLineFontColor = Color.LightGray
}

class HighlightingOptions {
    val searchResultColor = Color(0xFF32593D)
    val selectedSearchResultColor = CustomTheme.colors.focusedAccentColor
}


data class FontSettings(
    val fontColor: Color = Color.White,
    val fontSize: TextUnit = 18.sp,
    val fontWeight: FontWeight = FontWeight.Normal,
    val fontFamily: FontFamily = Fonts.JetBrainsMono(),
)