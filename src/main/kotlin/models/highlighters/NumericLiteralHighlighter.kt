package models.highlighters

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle


// TODO: move styles into Settings
class NumericLiteralHighlighter(begin: Int, end: Int)
    : AbstractHighlighter(begin, end, SpanStyle(color = Color(73, 117, 167)))
