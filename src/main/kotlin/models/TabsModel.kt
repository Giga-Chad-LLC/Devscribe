package models

import androidx.compose.runtime.mutableStateListOf
import components.vfs.nodes.VFSFile
import java.util.*

class TabsModel {
    var pinnedFiles = mutableStateListOf<PinnedFileModel>()

    fun add(file: VFSFile): PinnedFileModel {
        val pinnedFile = PinnedFileModel(file)
        pinnedFiles.add(pinnedFile)
        return pinnedFile
    }

    fun indexOf(id: UUID): Int {
        for (index in pinnedFiles.indices) {
            if (pinnedFiles[index].id == id) {
                return index
            }
        }
        return -1
    }

    fun removePinnedFile(id: UUID): Boolean {
        return pinnedFiles.removeIf { file -> file.id == id }
    }

    fun containsFile(file: VFSFile): Boolean {
        return pinnedFiles.any{ fileModel -> fileModel.virtualFile.id == file.id }
    }

    fun get(file: VFSFile): PinnedFileModel {
        return pinnedFiles.find { fileModel -> fileModel.virtualFile.id == file.id }
            ?: throw IllegalArgumentException("VFS file with id ${file.id} not found among pinned files")
    }

    fun rename(file: VFSFile, renameTo: String) {
        if (containsFile(file)) {
            get(file).filename = renameTo
        }
    }
}