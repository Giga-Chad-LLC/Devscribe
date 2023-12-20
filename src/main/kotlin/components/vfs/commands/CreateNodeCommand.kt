package components.vfs.commands

import components.GlobalReadWriteLock
import components.vfs.VirtualFileSystem
import components.vfs.nodes.VFSDirectory
import components.vfs.nodes.VFSNode
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardOpenOption
import java.util.function.Consumer
import kotlin.io.path.exists


class CreateNodeCommand(
    private val vfs: VirtualFileSystem,
    private val parentVirtualDirectory: VFSDirectory,
    private val filename: String,
    private val isFile: Boolean,
    private val callback: (VFSNode) -> Unit = {}
) : VFSCommand {
    override fun run() {
        println("Process CreateFileCommand for parent folder: $parentVirtualDirectory")
        val rwlock = GlobalReadWriteLock.getInstance()
        val parentPath: Path

        rwlock.lockRead()
        try {
            parentPath = parentVirtualDirectory.getNodePath()
        }
        finally {
            rwlock.unlockRead()
        }

        if (!parentPath.exists()) {
            println("File '$parentPath' does not exist on disk")
            return
        }

        var success = false
        try {
            val childPath: Path = Path.of(parentPath.toString(), filename)
            if (isFile) {
                Files.write(childPath, byteArrayOf(), StandardOpenOption.CREATE)
            }
            else {
                Files.createDirectory(childPath)
            }

            println("Successfully create node $childPath")
            success = true
        }
        catch (err: Exception) {
            println("Exception while creating file: ${err.message}")
        }

        val createdNode: VFSNode
        if (success) {
            rwlock.lockWrite()
            try {
                createdNode = if (isFile) {
                    vfs.createChildFile(parentVirtualDirectory, filename)
                } else {
                    vfs.createChildDirectory(parentVirtualDirectory, filename)
                }
            } finally {
                rwlock.unlockWrite()
            }

            rwlock.lockRead()
            try {
                callback(createdNode)
            } finally {
                rwlock.unlockRead()
            }
        }
    }
}