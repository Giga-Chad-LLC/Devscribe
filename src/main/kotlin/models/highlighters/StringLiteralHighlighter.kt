package models.highlighters

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle


class StringLiteralHighlighter(begin: Int, end: Int)
    : AbstractHighlighter(begin, end, SpanStyle(color = Color(92, 117, 78)))