package components.vfs.nodes

import components.vfs.VirtualFileSystem
import java.nio.file.Path
import java.nio.file.attribute.FileTime

abstract class VFSNode(
    private val vfs: VirtualFileSystem,
    private val filename: String,
    private val parent: VFSNode?,
    private val id: Int,
    var timestamp: FileTime
) {
    fun getVirtualFileSystem(): VirtualFileSystem = vfs // This call can be racy, because the VFS reference never changes inside a VFSNode
    fun getParentNode(): VFSNode? = parent
    abstract fun isFile(): Boolean
    abstract fun isDirectory(): Boolean
    abstract fun hasChildren(): Boolean
    fun getId(): Int = id
    fun getFilename(): String = filename
    fun getNodePath(): Path {
        return if (parent != null) {
            Path.of(parent.getNodePath().toString(), filename)
        } else {
            Path.of(vfs.getProjectPath()!!)
        }
    }
}