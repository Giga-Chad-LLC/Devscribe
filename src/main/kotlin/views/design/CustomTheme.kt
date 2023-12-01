package views.design

import androidx.compose.material.darkColors
import androidx.compose.ui.graphics.Color


object CustomTheme {
    val colors: Colors = Colors()

    // TODO: use these colors where hover effects exist
    class Colors(
        val backgroundDark: Color = Color(0xFF2B2B2B),
        val backgroundMedium: Color = Color(0xFF3C3F41),
        val backgroundLight: Color = Color(0xFF4E5254),

        // elements colors
        val primaryColor: Color = Color(0x96FFFFFF),
        val focusedAccentColor: Color = Color(0xFF4AC0FF),
        val disabledColor: Color = Color(0x1EFFFFFF),
        val hoveredColor: Color = Color.White,

        val material: androidx.compose.material.Colors = darkColors(
            background = backgroundDark,
            surface = backgroundMedium,
            primary = primaryColor
        ),
    )
}