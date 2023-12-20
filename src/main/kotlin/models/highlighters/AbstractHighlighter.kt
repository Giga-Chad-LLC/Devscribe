package models.highlighters

import androidx.compose.ui.text.SpanStyle

/**
 * Holds text selection as range [begin, end) and its styles.
 */
abstract class AbstractHighlighter protected constructor(begin_: Int, end_: Int, style_: SpanStyle) {
    val begin: Int = begin_
    val end: Int = end_
    val style: SpanStyle = style_

    override fun toString(): String {
        return "Highlighter[begin=$begin, end=$end]"
    }
}