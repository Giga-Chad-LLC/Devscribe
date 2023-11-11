package views.common

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
    val editorSettings by mutableStateOf(EditorSettings())
}

class EditorSettings {
    val linesPanel by mutableStateOf(LinesPanel())
}

class LinesPanel {
    val fontSettings by mutableStateOf(
        FontSettings(
            fontColor = Color(90, 89, 86),
            fontSize = 16.sp,
            fontWeight = FontWeight.Light
        ))
    val backgroundColor = Color(49, 51,53)
    val splitLineColor = Color(79, 79, 79)
    val cursoredLineFontColor = Color.LightGray
}

data class FontSettings(
    val fontColor: Color = Color.White,
    val fontSize: TextUnit = 18.sp,
    val fontWeight: FontWeight = FontWeight.Normal,
    val fontFamily: FontFamily = Fonts.JetBrainsMono(),
)