package components.vfs.commands

import components.GlobalReadWriteLock
import components.vfs.VirtualFileSystem

class IndexCommand(
    private val vfs: VirtualFileSystem,
    private val callback: Runnable
) : VFSCommand {
    override fun run() {
        println("Process IndexCommand in vfs: $vfs")
        val rwlock = GlobalReadWriteLock.getInstance()

        rwlock.lockWrite()
        try {
            vfs.index()
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