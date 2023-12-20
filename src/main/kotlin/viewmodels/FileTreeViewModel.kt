package viewmodels

import components.vfs.VirtualFileSystem
import components.vfs.commands.RenameFileCommand
import components.vfs.nodes.VFSDirectory
import components.vfs.nodes.VFSFile
import components.vfs.nodes.VFSNode
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
            RenameFileCommand(vfs, node.file, renameTo) {
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

    private fun isValidFilename(path: String): Boolean {
        // Define a regular expression pattern for a valid filename
        val pattern = Regex("[^\\\\/:*?\"<>|]+")

        // Check if the path matches the pattern
        return pattern.matches(path) && path.isNotBlank()
    }
}