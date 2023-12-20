package viewmodels

import components.vfs.nodes.VFSDirectory
import components.vfs.nodes.VFSFile
import models.FileTreeModel

class FileTreeViewModel(private val fileTreeModel: FileTreeModel, private val tabsViewModel: TabsViewModel) {
    var nodes: List<FileTreeModel.NodeModel> = emptyList()
        get() {
            if (fileTreeModel.root != null && !fileTreeModel.isTraversed) {
                field = fileTreeModel.root!!.traverse()
                println("Traverse: size=${field.size}")
            } else {
                println("Use prev: size=${field.size}")
            }
            return field
        }

    fun click(node: FileTreeModel.NodeModel) = node.apply {
        when (type) {
            is FileTreeModel.NodeType.Folder -> toggleExpanded()
            is FileTreeModel.NodeType.File -> tabsViewModel.pin(node.file as VFSFile)
        }
    }

    fun setRoot(root: VFSDirectory) = fileTreeModel.setRoot(root)
}