package components.vfs.nodes

import components.vfs.VirtualFileSystem
import java.nio.file.Path

abstract class VFSNode(
    private val vfs: VirtualFileSystem,
    private val filename: String,
    private val parent: VFSNode?,
) {
    /*
    * This call can be race, because the VFS reference never changes inside a VFSNode
    * */
    fun getVirtualFileSystem(): VirtualFileSystem = vfs
    fun getParentNode(): VFSNode? = parent
    abstract fun isFile(): Boolean
    abstract fun isDirectory(): Boolean
    abstract fun hasChildren(): Boolean
    fun getFilename(): String = filename
    fun getNodePath(): Path {
        return if (parent != null) {
            Path.of(parent.getNodePath().toString(), filename)
        } else {
            Path.of(vfs.getProjectPathPrefix())
        }
    }
}