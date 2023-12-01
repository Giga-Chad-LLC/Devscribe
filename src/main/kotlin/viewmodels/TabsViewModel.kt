package viewmodels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import common.kthBeforeIndex
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
    var activeFile by mutableStateOf<PinnedFileModel?>(null)

    fun pin(file: VFSFile) {
        if (!tabsModel.containsFile(file)) {
            val pinnedFileModel = tabsModel.add(file)
            // selecting pinned file
            select(file)

            val vfs = pinnedFileModel.virtualFile.getVirtualFileSystem()

            vfs.post(LoadFileFromDiskCommand(vfs, pinnedFileModel.virtualFile) {
                println("VFS file loaded!")
                viewScope.launch {
                    pinnedFileModel.textModel.install(pinnedFileModel.virtualFile.data)
                    println("Text model synced with VFS file!")
                }
            })
        }
        else {
            // selecting pinned file
            select(file)
        }
    }

    fun unpin(fileModelId: UUID) {
        if (activeFile?.id == fileModelId) {
            var newActiveFile: PinnedFileModel? = null

            val index = tabsModel.indexOf(fileModelId)
            if (index != -1 && tabsModel.pinnedFiles.size > 1) {
                newActiveFile = tabsModel.pinnedFiles.kthBeforeIndex(index, 1)
            }

            activeFile = newActiveFile
        }

        tabsModel.removePinnedFile(fileModelId)
    }

    /**
     * Activates tab with VFS file
     */
    private fun select(file: VFSFile) {
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
                activeFile = fileModel
                selected = true
                break
            }
        }

        if (!selected) {
            throw IllegalStateException("File model with provided id '$fileModelId' not found")
        }
    }


    /*fun getActiveFile(): PinnedFileModel? {
        for (fileModel in tabsModel.pinnedFiles) {
            if (fileModel.active) {
                return fileModel
            }
        }
        return null
    }*/
}