package components.vfs.commands

import components.GlobalReadWriteLock
import components.vfs.VirtualFileSystem
import components.vfs.nodes.VFSNode
import java.nio.file.Path
import kotlin.io.path.exists

class RenameNodeCommand(
    private val vfs: VirtualFileSystem,
    private val virtualNode: VFSNode,
    private val renameTo: String,
    private val callback: () -> Unit = {}
) : VFSCommand {
    override fun run() {
        println("Process RenameNodeCommand for file: $virtualNode")
        val rwlock = GlobalReadWriteLock.getInstance()
        val filePath: Path
        val newFilePath: Path

        rwlock.lockRead()
        try {
            newFilePath = Path.of((virtualNode.getParentNode()?.getNodePath()?.toString() ?: ""), renameTo)
            filePath = virtualNode.getNodePath()
        }
        finally {
            rwlock.unlockRead()
        }

        var success = false
        if (!filePath.exists()) {
            println("File '$filePath' does not exist on disk")
            return
        }
        try {
            success = filePath.toFile().renameTo(newFilePath.toFile())
            if (success) {
                println("Successfully renamed to $newFilePath")
            }
            else {
                println("Error while renaming files")
            }
        }
        catch (err: Exception) {
            println("Exception while renaming file: ${err.message}")
        }

        if (success) {
            rwlock.lockWrite()
            try {
                vfs.rename(virtualNode, renameTo)
            } finally {
                rwlock.unlockWrite()
            }

            rwlock.lockRead()
            try {
                callback()
            } finally {
                rwlock.unlockRead()
            }
        }
    }
}