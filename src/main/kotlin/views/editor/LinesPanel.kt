package views.editor

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.ScrollableState
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import models.text.Cursor
import viewmodels.TextViewModel
import views.design.Settings



/**
 * Draws panel with line numbers.
 * @return Size of the drawn panel.
 */
@OptIn(ExperimentalTextApi::class)
private fun DrawScope.drawLinesPanel(
    fontFamilyResolver: FontFamily.Resolver,
    textMeasurer: TextMeasurer,
    scrollOffsetY: Float,
    editorState: EditorState,
    cursor: Cursor,
    settings: Settings,
    editorFocused: Boolean,
    density: Float
) {
    val linesPanelSettings = settings.editorSettings.linesPanel

    val lineSymbolSize = getSymbolSize(
        fontFamilyResolver,
        textMeasurer,
        linesPanelSettings.fontSettings,
        density,
    )

    val (startLineIndex, endLineIndex) = editorState.viewportLinesRange()

    val startLineNumber = startLineIndex + 1
    val maxLineNumber = endLineIndex

    /**
     * Starting with centering offsets that place the line number in the center of code symbol.
     *
     * Assuming that font size of line number <= font size of code symbol.
     */
    val centeringOffsetX = (editorState.symbolSize.width - lineSymbolSize.width) / 2
    val centeringOffsetY = (editorState.symbolSize.height - lineSymbolSize.height) / 2

    /**
     * offsetY starts from the offset of scrolled up lines (i.e. first 'startLineIndex' line)
     */
    var offsetY = centeringOffsetY + startLineIndex * editorState.symbolSize.height

    val linesPanelSize = determineLinesPanelSize(
        fontFamilyResolver,
        textMeasurer,
        editorState,
        settings,
        density,
    )

    /**
     * Drawing a split line on the right border of the lines panel
     */
    drawRect(
        color = if (editorFocused) linesPanelSettings.editorFocusedSplitLineColor else linesPanelSettings.splitLineColor,
        topLeft = Offset(linesPanelSize.width - 1f, 0f),
        size = Size(1f, linesPanelSize.height)
    )

    /**
     * Drawing lines numbers
     */
    for (lineNumber in startLineNumber .. maxLineNumber) {
        // if current line is under cursor lighten it
        val color = if (cursor.lineNumber + 1 == lineNumber)
            linesPanelSettings.cursoredLineFontColor else linesPanelSettings.fontSettings.fontColor

        val measuredText = textMeasurer.measure(
            AnnotatedString(lineNumber.toString()),
            style = TextStyle(
                color = color,
                fontSize = linesPanelSettings.fontSettings.fontSize,
                fontWeight = linesPanelSettings.fontSettings.fontWeight,
                fontFamily = linesPanelSettings.fontSettings.fontFamily,
            )
        )

        // the translation makes the line numbers to be aligned by the smallest digit, i.e. digits grow from right to left
        val translationX = (maxLineNumber.toString().length - lineNumber.toString().length) * lineSymbolSize.width
        val resultOffsetX = LINES_PANEL_RIGHT_PADDING + translationX + centeringOffsetX /*+ scrollOffset.x*/

        // drawing line number
        drawText(
            measuredText,
            topLeft = Offset(resultOffsetX, offsetY + scrollOffsetY)
        )

        offsetY += editorState.symbolSize.height
    }
}


@OptIn(ExperimentalTextApi::class)
private fun determineLinesPanelSize(
    fontFamilyResolver: FontFamily.Resolver,
    textMeasurer: TextMeasurer,
    editorState: EditorState,
    settings: Settings,
    density: Float
): Size {
    val lineSymbolSize = getSymbolSize(
        fontFamilyResolver,
        textMeasurer,
        settings.editorSettings.linesPanel.fontSettings,
        density
    )

    val maxLineNumber = editorState.textViewModel.textModel.linesCount()

    return Size(
        // left & right paddings + width of the longest line number
        LINES_PANEL_RIGHT_PADDING + maxLineNumber.toString().length * lineSymbolSize.width + LINES_PANEL_LEFT_PADDING,
        // canvas width
        editorState.canvasSize.value.height.toFloat()
    )
}




@OptIn(ExperimentalTextApi::class)
@Composable
internal fun LinesPanel(
    verticalScrollState: ScrollableState,
    settings: Settings,
    fontFamilyResolver: FontFamily.Resolver,
    textMeasurer: TextMeasurer,
    editorState: EditorState,
    textViewModel: TextViewModel,
    density: Float,
    editorFocused: Boolean,
    ) {
    val linesPanelSize = determineLinesPanelSize(
        fontFamilyResolver,
        textMeasurer,
        editorState,
        settings,
        density,
    )

    Canvas(
        androidx.compose.ui.Modifier
            // dividing by current density to make the width in dp match the actual width
            .width((linesPanelSize.width / LocalDensity.current.density).dp)
            .scrollable(verticalScrollState, Orientation.Vertical)
            .fillMaxHeight()
            .clipToBounds()
            .background(settings.editorSettings.linesPanel.backgroundColor)
    ) {
        drawLinesPanel(
            fontFamilyResolver = fontFamilyResolver,
            textMeasurer = textMeasurer,
            scrollOffsetY = -editorState.verticalScrollOffset.value,
            editorState = editorState,
            cursor = textViewModel.cursor,
            settings = settings,
            editorFocused = editorFocused,
            density = density,
        )
    }
}