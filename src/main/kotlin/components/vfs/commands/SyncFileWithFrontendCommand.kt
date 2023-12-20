package components.vfs.commands

import components.GlobalReadWriteLock
import components.vfs.VirtualFileSystem
import components.vfs.nodes.VFSFile

class SyncFileWithFrontendCommand(
    private val vfs: VirtualFileSystem,
    private val file: VFSFile,
    private val data: String,
    private val callback: () -> Unit = {}
) : VFSCommand {
    override fun run() {
        println("Process SyncFileWithFrontendCommand in vfs: $vfs")
        val rwlock = GlobalReadWriteLock.getInstance()

        rwlock.lockWrite()
        try {
            vfs.syncFileWithFrontend(file, data)
            println("File $file is synced with frontend")
            // println("File data: '${file.data}'")
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
