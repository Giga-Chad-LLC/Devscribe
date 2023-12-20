package components.vfs.commands

import components.GlobalReadWriteLock
import components.vfs.VirtualFileSystem
import components.vfs.nodes.VFSNode
import java.nio.file.Path
import kotlin.io.path.exists

class RenameFileCommand(
    private val vfs: VirtualFileSystem,
    private val virtualFile: VFSNode,
    private val renameTo: String,
    private val callback: Runnable
) : VFSCommand {
    override fun run() {
        println("Process RenameNodeCommand for file: $virtualFile")
        val rwlock = GlobalReadWriteLock.getInstance()
        val filePath: Path
        val newFilePath: Path

        rwlock.lockRead()
        try {
            newFilePath = Path.of((virtualFile.getParentNode()?.getNodePath()?.toString() ?: ""), renameTo)
            filePath = virtualFile.getNodePath()
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
                vfs.renameFile(virtualFile, renameTo)
            } finally {
                rwlock.unlockWrite()
            }

            rwlock.lockRead()
            try {
                callback.run()
            } finally {
                rwlock.unlockRead()
            }
        }
    }
}