package views.design

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
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
    var editorSettings by mutableStateOf(EditorSettings())
}

class EditorSettings {
    var linesPanel by mutableStateOf(LinesPanel())
    var codeFontSettings by mutableStateOf(EditorFontSettingsStates.editorTextFontSettingsDefault)
    var highlightingOptions by mutableStateOf(HighlightingOptions())
    var selectionColor by mutableStateOf(Color(33, 67, 131))
    var cursoredLineColor by mutableStateOf(Color.DarkGray)
}

// TODO: move 'Color(90, 89, 86)' into separate variable (primaryColor in CustomTheme.colors?)
class LinesPanel {
    var fontSettings by mutableStateOf(EditorFontSettingsStates.linesPanelFontSettingsDefault)
    var backgroundColor = Color(49, 51,53)
    var splitLineColor = Color(79, 79, 79)
    var editorFocusedSplitLineColor = Color(92, 92, 92) // light dray
    var cursoredLineFontColor = Color.LightGray
}

class HighlightingOptions {
    var searchResultColor = Color(0xFF32593D)
    var selectedSearchResultColor = CustomTheme.colors.focusedAccentColor
}


class EditorFontSettingsStates {
    companion object {
        // default
        val linesPanelFontSettingsDefault: FontSettings = FontSettings(
            fontColor = Color(90, 89, 86), // gray
            fontSize = 16.sp,
            fontWeight = FontWeight.Light
        )
        val editorTextFontSettingsDefault: TextStyle = TextStyle(
            color = Color.LightGray,
            fontSize = 18.sp,
            fontFamily = Fonts.JetBrainsMono(),
        )

        // small
        val linesPanelFontSettingsSmall: FontSettings = FontSettings(
            fontColor = Color(90, 89, 86), // gray
            fontSize = 14.sp,
            fontWeight = FontWeight.Light
        )
        val editorTextFontSettingsSmall: TextStyle = TextStyle(
            color = Color.LightGray,
            fontSize = 16.sp,
            fontFamily = Fonts.JetBrainsMono(),
        )

        // medium
        val linesPanelFontSettingsMedium: FontSettings = FontSettings(
            fontColor = Color(90, 89, 86), // gray
            fontSize = 18.sp,
            fontWeight = FontWeight.Light
        )
        val editorTextFontSettingsMedium: TextStyle = TextStyle(
            color = Color.LightGray,
            fontSize = 20.sp,
            fontFamily = Fonts.JetBrainsMono(),
        )

        // large
        val linesPanelFontSettingsLarge: FontSettings = FontSettings(
            fontColor = Color(90, 89, 86), // gray
            fontSize = 20.sp,
            fontWeight = FontWeight.Light
        )
        val editorTextFontSettingsLarge: TextStyle = TextStyle(
            color = Color.LightGray,
            fontSize = 22.sp,
            fontFamily = Fonts.JetBrainsMono(),
        )

        // Extra large
        val linesPanelFontSettingsExtraLarge: FontSettings = FontSettings(
            fontColor = Color(90, 89, 86), // gray
            fontSize = 22.sp,
            fontWeight = FontWeight.Light
        )
        val editorTextFontSettingsExtraLarge: TextStyle = TextStyle(
            color = Color.LightGray,
            fontSize = 24.sp,
            fontFamily = Fonts.JetBrainsMono(),
        )
    }
}


data class FontSettings(
    val fontColor: Color = Color.White,
    val fontSize: TextUnit = 18.sp,
    val fontWeight: FontWeight = FontWeight.Normal,
    val fontFamily: FontFamily = Fonts.JetBrainsMono(),
)