package components.models

import components.models.text.TextModel

class FileModel(
    val filename: String,
    val textModel: TextModel,
    val highlighterModel: HighlighterModel,
    private var active: Boolean = false
) {
    fun activate() {
        active = true
        // TODO: subscribe for key events via dispatcher
    }

    fun deactivate() {
        active = false
        // TODO: unsubscribe of key events via dispatcher
    }
}