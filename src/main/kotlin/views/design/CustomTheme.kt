package views.design

import androidx.compose.material.darkColors
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color


object CustomTheme {
    var colors: Colors by mutableStateOf(ThemeStates.darkThemeColors)
}

// TODO: use these colors where hover effects exist
class Colors(
    val backgroundDark: Color,
    val backgroundMedium: Color,
    val backgroundLight: Color,

    // elements colors
    val primaryColor: Color,
    val focusedAccentColor: Color,
    val disabledColor: Color,
    val hoveredColor: Color,

    val material: androidx.compose.material.Colors
)

class ThemeStates {
    companion object {
        private val darkThemeBackgroundDark = Color(0xFF2B2B2B)
        private val darkThemeBackgroundMedium = Color(0xFF3C3F41)
        private val darkThemePrimaryColor = Color(0x96FFFFFF)

        val darkThemeColors = Colors(
            backgroundDark = darkThemeBackgroundDark,
            backgroundMedium = darkThemeBackgroundMedium,
            backgroundLight = Color(0xFF4E5254),

            // elements colors
            primaryColor = darkThemePrimaryColor,
            focusedAccentColor = Color(0xFF4AC0FF),
            disabledColor = Color(0x1EFFFFFF),
            hoveredColor = Color.White,

            material = darkColors(
                background = darkThemeBackgroundDark,
                surface = darkThemeBackgroundMedium,
                primary = darkThemePrimaryColor,
            ),
        )


        private val purpleThemeBackgroundDark = Color(0xFF1B1836)
        private val purpleThemeBackgroundMedium = Color(0xFF242043)
        private val purpleThemePrimaryColor = Color(0xFF6859A0)

        val purpleThemeColors = Colors(
            backgroundDark = purpleThemeBackgroundDark,
            backgroundMedium = purpleThemeBackgroundMedium,
            backgroundLight = Color(0xFF28234B),

            // elements colors
            primaryColor = purpleThemePrimaryColor,
            focusedAccentColor = Color(0xFFF2D50E),
            disabledColor = Color(0x1EFFFFFF),
            hoveredColor = Color(0xFF6A6026),

            material = darkColors(
                background = purpleThemeBackgroundDark,
                surface = purpleThemeBackgroundMedium,
                primary = purpleThemePrimaryColor,
            ),
        )
    }
}