package components.vfs.commands

import components.GlobalReadWriteLock
import components.vfs.nodes.VFSFile
import java.nio.file.Path
import java.nio.file.attribute.FileTime
import kotlin.io.path.exists
import kotlin.io.path.getLastModifiedTime

class SaveFileOnDiskCommand(
    private val virtualFile: VFSFile,
    private val callback: () -> Unit = {}
) : VFSCommand {
    override fun run() {
        println("Process SaveFileOnDiskCommand for file: $virtualFile")

        val rwlock = GlobalReadWriteLock.getInstance()
        val fileData: String
        val filePath: Path

        rwlock.lockRead()
        try {
            fileData = String(virtualFile.data.toByteArray())
            filePath = virtualFile.getNodePath()
        }
        finally {
            rwlock.unlockRead()
        }

        if (!filePath.exists()) {
            println("File: '$filePath' does not exist on disk")
            return
        }

        var success = true
        try {
            println("Saving file: '$filePath'")
            filePath.toFile().bufferedWriter().use {
                it.write(fileData)
                it.close()
            }
            println("File: '$filePath' saved")
        }
        catch (err: Exception) {
            success = false
            println("Saving file '$filePath' error: ${err.message}")
            err.printStackTrace()
        }

        if (success) {
            rwlock.lockWrite()
            try {
                virtualFile.isSaved = true
            }
            finally {
                rwlock.unlockWrite()
            }

            rwlock.lockRead()
            try {
                callback()
            }
            finally {
                rwlock.unlockRead()
            }
        }
    }
}