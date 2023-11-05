package viewmodels

import common.TextConstants
import components.vfs.commands.LoadFileFromDiskCommand
import components.vfs.nodes.VFSFile
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

    fun pin(file: VFSFile) {
        if (!tabsModel.containsFile(file)) {
            val pinnedFileModel = tabsModel.add(file)

            val vfs = pinnedFileModel.virtualFile.getVirtualFileSystem()

            vfs.post(LoadFileFromDiskCommand(vfs, pinnedFileModel.virtualFile) {
                println("VFS file loaded!")
                viewScope.launch {
                    pinnedFileModel.textModel.text = pinnedFileModel.virtualFile.data
                    println("Text model synced with VFS file!")
                }
            })
        }

        // selecting pinned file
        select(file)
    }

    fun unpin(fileModelId: UUID) {
        tabsModel.removePinnedFile(fileModelId)
    }

    /**
     * Activates tab with VFS file
     */
    fun select(file: VFSFile) {
        val pinnedFileModel = tabsModel.get(file)
        select(pinnedFileModel.id)
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