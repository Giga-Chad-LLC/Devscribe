package views.common

import androidx.compose.material.darkColors
import androidx.compose.ui.graphics.Color


object CustomTheme {
    val colors: Colors = Colors()

    class Colors(
        val backgroundDark: Color = Color(0xFF2B2B2B),
        val backgroundMedium: Color = Color(0xFF3C3F41),
        val backgroundLight: Color = Color(0xFF4E5254),
        val focusedAccentColor: Color = Color(0XFF4AC0FF),

        val material: androidx.compose.material.Colors = darkColors(
            background = backgroundDark,
            surface = backgroundMedium,
            primary = Color.White
        ),
    )
}