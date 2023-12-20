package models.highlighters

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle

class BooleanLiteralHighlighter(begin: Int, end: Int)
    : AbstractHighlighter(begin, end, SpanStyle(color = Color(201, 113, 49)))
