package components.vfs.commands

import components.GlobalReadWriteLock
import components.vfs.VirtualFileSystem
import components.vfs.nodes.VFSNode
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.exists

class RemoveNodeCommand(
    private val vfs: VirtualFileSystem,
    private val virtualNode: VFSNode,
    private val callback: Runnable
) : VFSCommand {
    override fun run() {
        println("Process RemoveNodeCommand in vfs: $vfs")
        val rwlock = GlobalReadWriteLock.getInstance()
        val nodePath: Path

        rwlock.lockRead()
        try {
            nodePath = virtualNode.getNodePath()
        }
        finally {
            rwlock.unlockRead()
        }

        if (!nodePath.exists()) {
            println("Node '$nodePath' does not exist on disk")
            return
        }

        try {
            if (virtualNode.isFile()) {
                Files.delete(nodePath)
                println("File '$nodePath' deleted successfully.")
            }
            else {
                Files.walk(nodePath)
                    .sorted(Comparator.reverseOrder()) // Reverse order for deleting from deepest to shallowest
                    .forEach { Files.delete(it) }

                println("Directory '$nodePath' and its contents deleted successfully.")
            }
        } catch (e: Exception) {
            println("Failed to delete the directory: ${e.message}")
        }

        rwlock.lockWrite()
        try {
            vfs.remove(virtualNode)
        }
        finally {
            rwlock.unlockWrite()
        }

        rwlock.lockRead()
        try {
            callback.run()
        }
        finally {
            rwlock.unlockRead()
        }
    }
}
