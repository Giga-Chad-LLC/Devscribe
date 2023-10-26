package components.vfs.nodes

import components.vfs.VirtualFileSystem

class VFSFile(vfs: VirtualFileSystem, filename: String, parent: VFSNode?) : VFSNode(vfs, filename, parent) {
    var data: String = ""
    // val textData: TextModel = LineArrayTextModel()

    override fun isFile(): Boolean {
        return true
    }

    override fun isDirectory(): Boolean {
        return false
    }

    override fun hasChildren(): Boolean = false
}