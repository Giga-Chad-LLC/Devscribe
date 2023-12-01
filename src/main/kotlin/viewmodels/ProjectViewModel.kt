package viewmodels

import components.dispatcher.KeyboardEventDispatcher
import components.vfs.VirtualFileSystem
import components.vfs.commands.IndexCommand
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import models.FileTreeModel
import models.TabsModel
import java.nio.file.Path

class ProjectViewModel(
    private val vfs: VirtualFileSystem,
    private val viewScope: CoroutineScope
) {
    val tabsModel = TabsModel()
    val fileTreeModel = FileTreeModel()

    init {
        openProject(Path.of("C:/Users/dmitriiart/Downloads/ProjectFolder"))
    }

    fun openProject(dirPath: Path) {
        vfs.setProjectPath(dirPath)
        vfs.post(IndexCommand(vfs) {
            viewScope.launch {
                fileTreeModel.setRoot(vfs.getProjectRoot())
            }
        })
    }
}