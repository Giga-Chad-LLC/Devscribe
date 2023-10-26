package viewmodels

import components.vfs.VirtualFileSystem
import components.vfs.commands.IndexCommand
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import models.FileTreeModel
import models.TabsModel

class ProjectViewModel(vfs: VirtualFileSystem, private val viewScope: CoroutineScope) {
    val tabsModel = TabsModel()
    val fileTreeModel = FileTreeModel()

    init {
        vfs.post(IndexCommand(vfs) {
            viewScope.launch {
                fileTreeModel.setRoot(vfs.getProjectRoot())
            }
        })
    }
}