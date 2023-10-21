package components.models

import androidx.compose.runtime.mutableStateListOf
import java.util.*

class TabsModel {
    val pinnedFiles = mutableStateListOf(
        // TODO: remove later, for testing only
        FileModel("File 1"),
        FileModel("File 2"),
        FileModel("File 3")
    )
    fun addPinnedFile(file: FileModel) {
        pinnedFiles.add(file)
    }

    fun removePinnedFile(id: UUID): Boolean {
        return pinnedFiles.removeIf { file -> file.id == id }
    }
}