package views.common

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp

class Settings {
    var fontSettings by mutableStateOf(FontSettings())
}

data class FontSettings(
    val fontSize: TextUnit = 18.sp,
    val fontFamily: FontFamily = Fonts.JetBrainsMono()
)