package components.text.highlighters

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle

/**
 * Represents text selection highlighter
 */
class SelectionHighlighter(begin: Int, end: Int)
    : Highlighter(
        begin,
        end,
        SpanStyle(
            background = Color.Cyan
        )
    )