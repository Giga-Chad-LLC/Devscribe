package components.vfs

import components.vfs.commands.VFSCommand
import components.vfs.nodes.VFSDirectory
import components.vfs.nodes.VFSFile

interface VirtualFileSystem {
    fun post(command: VFSCommand)
    fun index()
    fun syncFileWithDisk(file: VFSFile, data: String)
    fun syncFileWithFrontend(file: VFSFile, data: String)
    fun getProjectRoot(): VFSDirectory
    fun getProjectPathPrefix(): String
}