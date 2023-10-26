package components.vfs

import components.vfs.commands.VFSCommand
import java.util.concurrent.Executors

class VFSWorker() {
    private val threadPool = Executors.newSingleThreadExecutor()

    fun postCommand(command: VFSCommand) {
        println("Command $command is added to the worker")
        threadPool.submit(command)
    }

    fun shutdown() {
        threadPool.shutdown()
    }
}