package models

import models.text.LineArrayTextModel
import models.text.TextModel
import java.util.UUID

class FileModel(
    val filename: String,
    val textModel: TextModel = LineArrayTextModel(),
    val highlighterModel: HighlighterModel = HighlighterModel(),
    val active: Boolean = false
) {
    val id: UUID = UUID.randomUUID()

    init {
        println("FileModel $filename created")
    }

    override fun toString(): String {
        return "FileModel[filename='$filename', active=$active]"
    }

    fun activate(): FileModel {
        // TODO: subscribe for key events via dispatcher
        return FileModel(filename, textModel, highlighterModel, true)
    }

    fun deactivate(): FileModel {
        // TODO: unsubscribe of key events via dispatcher
        return FileModel(filename, textModel, highlighterModel, false)
    }

    override fun equals(other: Any?): Boolean {
        if (other is FileModel) {
            return id == other.id
        }
        return false
    }
}