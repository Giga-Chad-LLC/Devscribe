package models

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import components.vfs.nodes.VFSDirectory
import components.vfs.nodes.VFSNode

class FileTreeModel {
    var root by mutableStateOf<NodeModel?>(null)
    var isTraversed by mutableStateOf(false)

    fun setRoot(vfsRoot: VFSDirectory) {
        root = NodeModel(vfsRoot, null, 0).apply {
            toggleExpanded()
        }
        isTraversed = false
    }

    fun setFilename(node: NodeModel, newFilename: String) {
        isTraversed = false
        node.filename = newFilename
        node.parent?.sortChildren()
    }

    inner class NodeModel(
        val file: VFSNode,
        val parent: NodeModel?,
        val level: Int = 0 // determines the offset inside sidebar
    ) {
        var filename: String by mutableStateOf(file.filename)
        private var children: List<NodeModel> by mutableStateOf(emptyList())
        private val canExpand: Boolean get() = file.hasChildren()

        val type: NodeType
            get() = if (file.isDirectory()) {
                NodeType.Folder(isExpanded = children.isNotEmpty(), canExpand = canExpand)
            } else {
                NodeType.File(extension = file.filename.substringAfterLast(".").lowercase())
            }

        fun toggleExpanded() {
            children = if (children.isEmpty() && file.isDirectory()) {
                (file as VFSDirectory).getChildren()
                    .map { NodeModel(it, this, level + 1) }
                    .sortedWith(compareBy({ !it.file.isDirectory() }, { it.file.filename }))
            } else {
                emptyList()
            }
            isTraversed = false
        }

        fun traverse(): List<NodeModel> {
            val nodes = traverseImpl()
            isTraversed = true
            return nodes
        }

        fun sortChildren() {
            children = children.sortedWith(compareBy({ !it.file.isDirectory() }, { it.file.filename }))
        }

        private fun traverseImpl(list: MutableList<NodeModel> = mutableListOf()): List<NodeModel> {
            list.add(this)
            children.forEach { it.traverseImpl(list) }
            return list
        }
    }

    sealed class NodeType {
        class Folder(val isExpanded: Boolean, val canExpand: Boolean) : NodeType()
        class File(val extension: String) : NodeType()
    }
}