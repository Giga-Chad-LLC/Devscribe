package viewmodels

import common.TextConstants
import components.vfs.commands.LoadFileFromDiskCommand
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import models.PinnedFileModel
import models.TabsModel
import java.util.*

class TabsViewModel(
    private val tabsModel: TabsModel,
    private val viewScope: CoroutineScope
) {
    val files: List<PinnedFileModel> = tabsModel.pinnedFiles

    fun pin(file: PinnedFileModel) {
        tabsModel.addPinnedFile(file)
        val vfs = file.virtualFile.getVirtualFileSystem()

        vfs.post(LoadFileFromDiskCommand(vfs, file.virtualFile) {
            println("VFS file loaded!")
            viewScope.launch {
                file.textModel.text = file.virtualFile.data
                println("Text model synced with VFS file!")
            }
        })
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

    fun getActiveFile(): PinnedFileModel? {
        for (fileModel in tabsModel.pinnedFiles) {
            if (fileModel.active) {
                return fileModel
            }
        }
        return null
    }
}