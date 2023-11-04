package models

import androidx.compose.runtime.mutableStateListOf
import java.util.*

class TabsModel {
    val pinnedFiles = mutableStateListOf<PinnedFileModel>()

    fun addPinnedFile(file: PinnedFileModel) {
        pinnedFiles.add(file)
    }

    fun removePinnedFile(id: UUID): Boolean {
        return pinnedFiles.removeIf { file -> file.id == id }
    }

    fun containsPinnedFile(file: PinnedFileModel): Boolean {
        return pinnedFiles.contains(file)
    }
}