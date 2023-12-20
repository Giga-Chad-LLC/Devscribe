package components.vfs.nodes

import components.vfs.VirtualFileSystem
import java.nio.file.attribute.FileTime

class VFSFile(
    vfs: VirtualFileSystem,
    filename: String,
    parent: VFSNode?,
    id: Int,
    timestamp: FileTime
) : VFSNode(vfs, filename, parent, id, timestamp) {
    var data: String = ""
    var isSaved: Boolean = true
    // val textData: TextModel = LineArrayTextModel()

    override fun isFile(): Boolean {
        return true
    }

    override fun isDirectory(): Boolean {
        return false
    }

    override fun hasChildren(): Boolean = false
}