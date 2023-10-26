package components.vfs.nodes

import components.vfs.VirtualFileSystem
import java.util.*

class VFSDirectory(vfs: VirtualFileSystem, filename: String, parent: VFSNode?) : VFSNode(vfs, filename, parent) {
    private val childrenNodes: MutableList<VFSNode> = mutableListOf()

    fun addChildNode(child: VFSNode) {
        childrenNodes.add(child)
    }

    fun getChildren(): List<VFSNode> {
        return Collections.unmodifiableList(childrenNodes)
    }

    override fun isFile(): Boolean {
        return false
    }

    override fun isDirectory(): Boolean {
        return true
    }

    override fun hasChildren(): Boolean = childrenNodes.isNotEmpty()
}