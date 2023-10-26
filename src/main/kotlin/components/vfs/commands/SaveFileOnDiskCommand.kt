package components.vfs.commands

import components.GlobalReadWriteLock
import components.vfs.nodes.VFSFile
import java.nio.file.Path
import kotlin.io.path.exists

class SaveFileOnDiskCommand(
    private val virtualFile: VFSFile
) : VFSCommand {
    override fun run() {
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

        println("Save file: '${filePath}'")
        if (!filePath.exists()) {
            println("File: $filePath does not exist")
            return
        }

        filePath.toFile().bufferedWriter().use {
            it.write(fileData)
            it.close()
        }

        println("Saved file: '${filePath}'")
    }
}