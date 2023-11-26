package models.highlighters

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.style.TextDecoration


class InvalidSequenceHighlighter(begin: Int, end: Int)
    : AbstractHighlighter(begin, end, SpanStyle(
        textDecoration = TextDecoration.Underline,
        color = Color.Red,
    ))
