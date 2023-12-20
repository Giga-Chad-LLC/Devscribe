package components.vfs

import components.vfs.commands.VFSCommand
import components.vfs.nodes.VFSDirectory
import components.vfs.nodes.VFSFile
import components.vfs.nodes.VFSNode
import java.nio.file.Path
import java.nio.file.attribute.FileTime

interface VirtualFileSystem {
    fun post(command: VFSCommand)

    fun index()
    fun syncFileWithDisk(file: VFSFile, data: String, timestamp: FileTime)
    fun syncFileWithFrontend(file: VFSFile, data: String)

    fun getProjectRoot(): VFSDirectory
    fun getProjectPath(): String?
    fun setProjectPath(rootDir: Path)

    fun createChildFile(parent: VFSDirectory, childFilename: String): VFSNode
    fun createChildDirectory(parent: VFSDirectory, childDirname: String): VFSNode
    fun remove(node: VFSNode)
    fun rename(node: VFSNode, renameTo: String)
}