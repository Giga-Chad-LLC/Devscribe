package models

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import models.text.LineArrayTextModel
import models.text.TextModel
import java.util.UUID

class FileModel(
    val filename: String,
    val textModel: TextModel = LineArrayTextModel(),
    val highlighterModel: HighlighterModel = HighlighterModel(),
) {
    val id: UUID = UUID.randomUUID()
    var active by mutableStateOf(false)

    init {
        println("FileModel $filename created")
    }

    override fun toString(): String {
        return "FileModel[filename='$filename', active=$active]"
    }

    fun activate() {
        // TODO: subscribe for key events via dispatcher
        active = true
    }

    fun deactivate() {
        // TODO: unsubscribe of key events via dispatcher
        active = false
    }
}