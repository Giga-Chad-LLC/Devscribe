package views.editor

import androidx.compose.ui.geometry.Size
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.platform.FontLoadResult
import org.jetbrains.skia.Font
import views.design.FontSettings


/**
 * textMeasurer.measure("x").width (or .multiParagraph.width) is incorrect.
 * But skia multiplied by current density counts the symbol width correctly, but the height calculated by skia is incorrect.
 *
 * Thus, here width is calculated via skia, and height is calculated via TextMeasurer.
 * Recalculation occurs only on font size change, therefore it should not impact performance.
 *
 * @return The width and height of a symbol with respect to its font settings.
 */
@OptIn(ExperimentalTextApi::class)
internal fun getSymbolSize(
    fontFamilyResolver: FontFamily.Resolver,
    textMeasurer: TextMeasurer,
    fontSettings: FontSettings,
    density: Float
): Size {
    val fontLoadResult = fontFamilyResolver.resolve(fontSettings.fontFamily).value as FontLoadResult
    val style = TextStyle(
        fontSize = fontSettings.fontSize,
        fontFamily = fontSettings.fontFamily
    )

    val width = Font(fontLoadResult.typeface, fontSettings.fontSize.value).measureTextWidth("a") * density
    val height = textMeasurer.measure(AnnotatedString("a"), style).size.height

    return Size(width, height.toFloat())
}