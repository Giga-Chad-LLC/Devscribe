package viewmodels

import components.vfs.nodes.VFSFile
import models.FileTreeModel
import models.PinnedFileModel

class FileTreeViewModel(private val fileTreeMode: FileTreeModel, private val tabsViewModel: TabsViewModel) {
    val nodes: List<FileTreeModel.NodeModel>
        get() =
            if (fileTreeMode.root != null) {
                fileTreeMode.root!!.traverse()
            } else emptyList()

    fun click(node: FileTreeModel.NodeModel) = node.apply {
        when (type) {
            is FileTreeModel.NodeType.Folder -> toggleExpanded()
            is FileTreeModel.NodeType.File -> tabsViewModel.pin(PinnedFileModel(node.file as VFSFile))
        }
    }
}