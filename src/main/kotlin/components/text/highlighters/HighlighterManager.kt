package components.text.highlighters

import androidx.compose.runtime.mutableStateListOf

class HighlighterManager {
    val highlighters = mutableStateListOf<Highlighter>()

    fun containsSelectionHighlighter(): Boolean {
        return highlighters.find { h -> h is SelectionHighlighter } != null
    }

    fun selection(begin: Int, end: Int) {
        if (containsSelectionHighlighter()) {
            throw IllegalArgumentException("Multiple selection highlighters not supported")
        }
        highlighters.add(SelectionHighlighter(begin, end))
    }

    fun adjustSelection(oldCursorOffset: Int, newCursorOffset: Int) {
        if (!containsSelectionHighlighter()) {
            throw IllegalArgumentException("No selection highlighter exists")
        }

        val highlighter = highlighters.find { h -> h is SelectionHighlighter }!!
        highlighters.remove(highlighter)

        println("oldCursorOffset=$oldCursorOffset, newCursorOffset=$newCursorOffset")

        val newBegin: Int
        val newEnd: Int

        if (highlighter.begin == oldCursorOffset) {
            if (newCursorOffset >= highlighter.end) {
                newBegin = highlighter.end
                newEnd = newCursorOffset
            }
            else {
                newBegin = newCursorOffset
                newEnd = highlighter.end
            }
        }
        else if (highlighter.end == oldCursorOffset) {
            if (newCursorOffset <= highlighter.begin) {
                newBegin = newCursorOffset
                newEnd = highlighter.begin
            }
            else {
                newBegin = highlighter.begin
                newEnd = newCursorOffset
            }
        }
        else {
            throw IllegalArgumentException(
                "Provided old cursor offset $oldCursorOffset is incorrect, expected either ${highlighter.begin} or ${highlighter.end}")
        }

        println("Adjusted to [$newBegin, $newEnd)")

        val newHighlighter = SelectionHighlighter(
            begin = newBegin,
            end = newEnd,
        )

        highlighters.add(newHighlighter)
    }

    fun dismissSelection(): Boolean {
        return highlighters.removeIf { highlighter -> highlighter is SelectionHighlighter }
    }
}