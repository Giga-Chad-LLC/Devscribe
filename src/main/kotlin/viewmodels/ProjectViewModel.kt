package viewmodels

import components.FileChooser
import components.KeyboardEventDispatcher
import components.vfs.VirtualFileSystem
import components.vfs.commands.IndexCommand
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.nio.file.Path

class ProjectViewModel(
    private val vfs: VirtualFileSystem,
    private val tabsViewModel: TabsViewModel,
    private val fileTreeViewModel: FileTreeViewModel,
    private val viewScope: CoroutineScope
) {
    init {
        openProject(Path.of("C:/Users/dmitriiart/Downloads/ProjectFolder"))

        KeyboardEventDispatcher.getInstance().subscribe(KeyboardEventDispatcher.KeyboardAction.OPEN_PROJECT) {
            runBlocking {
                val newProjectPath = FileChooser.chooseDirectory()
                if (newProjectPath != null) {
                    println("Project folder selected: '$newProjectPath'")
                    openProject(Path.of(newProjectPath))
                }
            }
        }
    }

    fun openProject(dirPath: Path) {
        tabsViewModel.unpinAll()

        vfs.setProjectPath(dirPath)
        vfs.post(IndexCommand(vfs) {
            viewScope.launch {
                fileTreeViewModel.setRoot(vfs.getProjectRoot())
            }
        })
    }
}