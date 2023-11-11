package models

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import components.vfs.nodes.VFSDirectory
import components.vfs.nodes.VFSNode

class FileTreeModel {
    var root by mutableStateOf<NodeModel?>(null)

    fun setRoot(vfsRoot: VFSDirectory) {
        root = NodeModel(vfsRoot, 0).apply {
            toggleExpanded()
        }
    }


    inner class NodeModel(
        val file: VFSNode,
        val level: Int = 0 // determines the offset inside sidebar
    ) {
        val filename: String get() = file.getFilename()
        private var children: List<NodeModel> by mutableStateOf(emptyList())
        private val canExpand: Boolean get() = file.hasChildren()

        val type: NodeType
            get() = if (file.isDirectory()) {
                NodeType.Folder(isExpanded = children.isNotEmpty(), canExpand = canExpand)
            } else {
                NodeType.File(extension = file.getFilename().substringAfterLast(".").lowercase())
            }

        fun toggleExpanded() {
            children = if (children.isEmpty() && file.isDirectory()) {
                (file as VFSDirectory).getChildren()
                    .map { NodeModel(it, level + 1) }
                    .sortedWith(compareBy({ !it.file.isDirectory() }, { it.file.getFilename() }))
            } else {
                emptyList()
            }
        }

        fun traverse(list: MutableList<NodeModel> = mutableListOf()): List<NodeModel> {
            list.add(this)
            children.forEach { it.traverse(list) }
            return list
        }
    }

    sealed class NodeType {
        class Folder(val isExpanded: Boolean, val canExpand: Boolean) : NodeType()
        class File(val extension: String) : NodeType()
    }
}