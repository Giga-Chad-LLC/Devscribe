package components.vfs.commands

import components.GlobalReadWriteLock
import components.vfs.VirtualFileSystem
import components.vfs.nodes.VFSFile
import java.io.BufferedReader
import java.nio.file.Path
import kotlin.io.path.exists

class LoadFileFromDiskCommand(
    private val vfs: VirtualFileSystem,
    private val virtualFile: VFSFile,
    private val callback: Runnable
) : VFSCommand {
    override fun run() {
        println("Process LoadFileCommand in vfs: $vfs")
        val rwlock = GlobalReadWriteLock.getInstance()
        val filePath: Path

        rwlock.lockRead()
        try {
            filePath = virtualFile.getNodePath()
        }
        finally {
            rwlock.unlockRead()
        }

        println("Load file: '${filePath}'")
        if (!filePath.exists()) {
            println("File: $filePath does not exist")
            return
        }

        val bufferedReader: BufferedReader = filePath.toFile().bufferedReader()
        val fileData = bufferedReader.use { it.readText() }

        rwlock.lockWrite()
        try {
            vfs.syncFileWithDisk(virtualFile, fileData)
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
