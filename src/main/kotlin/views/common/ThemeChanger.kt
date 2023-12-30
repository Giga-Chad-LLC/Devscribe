package views.common

import views.design.*

class ThemeChanger {
    companion object {
        fun selectNextTheme(settings: Settings) {
            CustomTheme.colors = when(CustomTheme.colors) {
                ThemeStates.darkThemeColors -> {
                    settings.editorSettings.linesPanel.backgroundColor = ThemeStates.purpleThemeColors.backgroundDark
                    settings.editorSettings.linesPanel.splitLineColor = ThemeStates.purpleThemeColors.backgroundMedium
                    settings.editorSettings.linesPanel.editorFocusedSplitLineColor = ThemeStates.purpleThemeColors.primaryColor
                    settings.editorSettings.linesPanel.cursoredLineFontColor = ThemeStates.purpleThemeColors.primaryColor

                    settings.editorSettings.cursoredLineColor = ThemeStates.purpleThemeColors.backgroundLight
                    settings.editorSettings.highlightingOptions.selectedSearchResultColor = ThemeStates.purpleThemeColors.focusedAccentColor

                    ThemeStates.purpleThemeColors
                }
                ThemeStates.purpleThemeColors -> {
                    settings.editorSettings.linesPanel = LinesPanel()
                    settings.editorSettings = EditorSettings()
                    ThemeStates.darkThemeColors
                }
                else -> ThemeStates.darkThemeColors
            }
        }
    }
}