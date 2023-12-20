package viewmodels

import components.vfs.VirtualFileSystem
import components.vfs.commands.CreateNodeCommand
import components.vfs.commands.RemoveNodeCommand
import components.vfs.commands.RenameNodeCommand
import components.vfs.nodes.VFSDirectory
import components.vfs.nodes.VFSFile
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import models.FileTreeModel

class FileTreeViewModel(
    private val vfs: VirtualFileSystem,
    private val fileTreeModel: FileTreeModel,
    private val tabsViewModel: TabsViewModel,
    private val viewScope: CoroutineScope
) {
    var nodes: List<FileTreeModel.NodeModel> = emptyList()
        get() {
            if (fileTreeModel.root != null && !fileTreeModel.isTraversed) {
                field = fileTreeModel.root!!.traverse()
            }
            return field
        }

    fun setRoot(root: VFSDirectory) = fileTreeModel.setRoot(root)

    fun click(node: FileTreeModel.NodeModel) = node.apply {
        when (type) {
            is FileTreeModel.NodeType.Folder -> toggleExpanded()
            is FileTreeModel.NodeType.File -> tabsViewModel.pin(node.file as VFSFile)
        }
    }

    fun rename(node: FileTreeModel.NodeModel, renameTo: String) {
        if (!isValidFilename(renameTo)) {
            println("Invalid name to rename to: $renameTo")
            return
        }

        println("Rename: ${node.filename} -> $renameTo")
        vfs.post(
            RenameNodeCommand(vfs, node.file, renameTo) {
                viewScope.launch {
                    fileTreeModel.setFilename(node, node.file.filename)
                    if (node.file.isFile()) {
                        tabsViewModel.updateFilenameForPinnedFile(
                            node.file as VFSFile,
                            node.file.filename
                        )
                    }
                }
            }
        )
    }

    fun addFile(node: FileTreeModel.NodeModel) {
        val uniqueName = getUniqueNameFromNodeChildren(node, "new-file-")
        vfs.post(
            CreateNodeCommand(vfs, node.file as VFSDirectory, uniqueName, true) { virtualFile ->
                viewScope.launch {
                    fileTreeModel.addChild(node, virtualFile)
                }
            }
        )
    }

    fun addFolder(node: FileTreeModel.NodeModel) {
        val uniqueName = getUniqueNameFromNodeChildren(node, "new-folder-")
        vfs.post(
            CreateNodeCommand(vfs, node.file as VFSDirectory, uniqueName, false) { virtualFile ->
                viewScope.launch {
                    fileTreeModel.addChild(node, virtualFile)
                }
            }
        )
    }

    fun remove(node: FileTreeModel.NodeModel) {
        vfs.post(
            RemoveNodeCommand(vfs, node.file) {
                viewScope.launch {
                    fileTreeModel.remove(node)

                    if (node.file.isFile() && tabsViewModel.containsFile(node.file as VFSFile)) {
                        tabsViewModel.unpin(tabsViewModel.get(node.file).id)
                    }
                    else {
                        unpinRemovedFiles(node)
                    }
                }
            }
        )
    }

    private fun unpinRemovedFiles(dir: FileTreeModel.NodeModel) {
        dir.children.forEach {
            if (it.file.isFile() && tabsViewModel.containsFile(it.file as VFSFile)) {
                tabsViewModel.unpin(tabsViewModel.get(it.file).id)
            }
            else {
                unpinRemovedFiles(it)
            }
        }
    }

    private fun isValidFilename(path: String): Boolean {
        // Define a regular expression pattern for a valid filename
        val pattern = Regex("[^\\\\/:*?\"<>|]+")

        // Check if the path matches the pattern
        return pattern.matches(path) && path.isNotBlank()
    }

    private fun getUniqueNameFromNodeChildren(node: FileTreeModel.NodeModel, prefix: String): String {
        var newFileNumber = 1
        val childNames = node.getChildrenNames()

        while (childNames.contains(prefix + newFileNumber.toString())) {
            newFileNumber++
        }

        return prefix + newFileNumber.toString()
    }
}