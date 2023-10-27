package components.vfs.commands

import components.GlobalReadWriteLock
import components.vfs.VirtualFileSystem
import components.vfs.nodes.VFSFile
import java.nio.file.LinkOption
import java.nio.file.Path
import java.nio.file.attribute.FileTime
import kotlin.io.path.exists
import kotlin.io.path.getLastModifiedTime

class LoadFileFromDiskCommand(
    private val vfs: VirtualFileSystem,
    private val virtualFile: VFSFile,
    private val callback: Runnable
) : VFSCommand {
    override fun run() {
        println("Process LoadFileFromDiskCommand in vfs: $vfs")
        val rwlock = GlobalReadWriteLock.getInstance()
        val filePath: Path
        val fileTimestamp: FileTime

        rwlock.lockRead()
        try {
            filePath = virtualFile.getNodePath()
            fileTimestamp = FileTime.from(virtualFile.timestamp.toInstant())
        }
        finally {
            rwlock.unlockRead()
        }

        if (!filePath.exists()) {
            println("File '$filePath' does not exist on disk")
            return
        }

        if (fileTimestamp == filePath.getLastModifiedTime(LinkOption.NOFOLLOW_LINKS)) {
            println("File '$filePath' is up-to-date: no need to load again.")
        }
        else {
            println("Loading file: '$filePath'")
            val newFileData = filePath.toFile().bufferedReader().use { it.readText() }
            val newFileTimestamp = filePath.getLastModifiedTime()

            rwlock.lockWrite()
            try {
                vfs.syncFileWithDisk(virtualFile, newFileData, newFileTimestamp)
            } finally {
                rwlock.unlockWrite()
            }
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
