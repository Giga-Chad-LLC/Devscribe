package components.text.highlighters

import androidx.compose.ui.text.SpanStyle

/**
 * Holds text selection as range [begin, end) and its styles.
 */
abstract class Highlighter(
    val begin: Int,
    val end: Int,
    val style: SpanStyle
)