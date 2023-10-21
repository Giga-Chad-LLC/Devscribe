package viewmodels

import models.FileModel
import models.TabsModel
import java.lang.IllegalStateException
import java.util.UUID

class TabsViewModel(private val tabsModel: TabsModel) {
    val files: List<FileModel> = tabsModel.pinnedFiles

    fun pin(file: FileModel) {
        tabsModel.addPinnedFile(file)
    }

    fun unpin(fileModelId: UUID) {
        tabsModel.removePinnedFile(fileModelId)
    }

    /**
     * Activates tab with provided id
     */
    fun select(fileModelId: UUID) {
        var selected = false
        for (fileModel in tabsModel.pinnedFiles) {
            if (fileModelId == fileModel.id) {
                fileModel.activate()
                selected = true
            }
            else {
                fileModel.deactivate()
            }
        }

        if (!selected) {
            throw IllegalStateException("File model with provided id $fileModelId not found")
        }
    }

    fun getActiveFile(): FileModel? {
        for (fileModel in tabsModel.pinnedFiles) {
            if (fileModel.active) {
                return fileModel
            }
        }
        return null
    }
}