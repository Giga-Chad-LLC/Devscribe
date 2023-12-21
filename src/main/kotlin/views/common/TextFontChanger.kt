package views.common

import androidx.compose.ui.unit.sp
import views.design.EditorFontSettingsStates
import views.design.Settings


class TextFontChanger {
    companion object {
        fun increaseFontSize(settings: Settings) {
            settings.editorSettings.linesPanel.fontSettings = when(settings.editorSettings.linesPanel.fontSettings) {
                EditorFontSettingsStates.linesPanelFontSettingsSmall -> EditorFontSettingsStates.linesPanelFontSettingsDefault
                EditorFontSettingsStates.linesPanelFontSettingsDefault -> EditorFontSettingsStates.linesPanelFontSettingsMedium
                EditorFontSettingsStates.linesPanelFontSettingsMedium -> EditorFontSettingsStates.linesPanelFontSettingsLarge
                EditorFontSettingsStates.linesPanelFontSettingsLarge -> EditorFontSettingsStates.linesPanelFontSettingsExtraLarge
                EditorFontSettingsStates.linesPanelFontSettingsExtraLarge -> EditorFontSettingsStates.linesPanelFontSettingsExtraLarge
                else -> EditorFontSettingsStates.linesPanelFontSettingsDefault
            }

            settings.editorSettings.codeFontSettings = when(settings.editorSettings.codeFontSettings) {
                EditorFontSettingsStates.editorTextFontSettingsSmall -> EditorFontSettingsStates.editorTextFontSettingsDefault
                EditorFontSettingsStates.editorTextFontSettingsDefault -> EditorFontSettingsStates.editorTextFontSettingsMedium
                EditorFontSettingsStates.editorTextFontSettingsMedium -> EditorFontSettingsStates.editorTextFontSettingsLarge
                EditorFontSettingsStates.editorTextFontSettingsLarge -> EditorFontSettingsStates.editorTextFontSettingsExtraLarge
                EditorFontSettingsStates.editorTextFontSettingsExtraLarge -> EditorFontSettingsStates.editorTextFontSettingsExtraLarge
                else -> EditorFontSettingsStates.editorTextFontSettingsDefault
            }
        }

        fun decreaseFontSize(settings: Settings) {
            settings.editorSettings.linesPanel.fontSettings = when(settings.editorSettings.linesPanel.fontSettings) {
                EditorFontSettingsStates.linesPanelFontSettingsSmall -> EditorFontSettingsStates.linesPanelFontSettingsSmall
                EditorFontSettingsStates.linesPanelFontSettingsDefault -> EditorFontSettingsStates.linesPanelFontSettingsSmall
                EditorFontSettingsStates.linesPanelFontSettingsMedium -> EditorFontSettingsStates.linesPanelFontSettingsDefault
                EditorFontSettingsStates.linesPanelFontSettingsLarge -> EditorFontSettingsStates.linesPanelFontSettingsMedium
                EditorFontSettingsStates.linesPanelFontSettingsExtraLarge -> EditorFontSettingsStates.linesPanelFontSettingsLarge
                else -> EditorFontSettingsStates.linesPanelFontSettingsDefault
            }

            settings.editorSettings.codeFontSettings = when(settings.editorSettings.codeFontSettings) {
                EditorFontSettingsStates.editorTextFontSettingsSmall -> EditorFontSettingsStates.editorTextFontSettingsSmall
                EditorFontSettingsStates.editorTextFontSettingsDefault -> EditorFontSettingsStates.editorTextFontSettingsSmall
                EditorFontSettingsStates.editorTextFontSettingsMedium -> EditorFontSettingsStates.editorTextFontSettingsDefault
                EditorFontSettingsStates.editorTextFontSettingsLarge -> EditorFontSettingsStates.editorTextFontSettingsMedium
                EditorFontSettingsStates.editorTextFontSettingsExtraLarge -> EditorFontSettingsStates.editorTextFontSettingsLarge
                else -> EditorFontSettingsStates.editorTextFontSettingsDefault
            }
        }
    }
}