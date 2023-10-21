package components.models

import androidx.compose.runtime.mutableStateListOf

class TabsModel {
    private val _pinnedFiles = mutableStateListOf<FileModel>()

    val pinnedFiles: List<FileModel>
        get() = _pinnedFiles.toList()
}