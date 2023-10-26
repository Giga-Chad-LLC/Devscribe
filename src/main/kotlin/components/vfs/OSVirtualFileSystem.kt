package components.vfs

import components.vfs.commands.VFSCommand
import components.vfs.nodes.VFSDirectory
import components.vfs.nodes.VFSFile
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.FileNotFoundException
import java.io.FileWriter
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.exists
import kotlin.io.path.isDirectory
import kotlin.io.path.name

// TODO: make instance singleton (for easier access across composables and logically it is correct to do so, I suppose)
class OSVirtualFileSystem(private var projectPath: Path) : VirtualFileSystem {
    private val worker: VFSWorker = VFSWorker()
    private var root: VFSDirectory? = null

    override fun post(command: VFSCommand) {
        worker.postCommand(command)
    }

    override fun index() {
        println("Index project: $projectPath")
        if (!projectPath.exists()) {
            throw FileNotFoundException("Cannot load VFS for project at '$projectPath'")
        }
        root = VFSDirectory(this, projectPath.name, null)
        loadDirectory(root!!, projectPath)

        printProjectFiles()
    }

    override fun syncFileWithDisk(file: VFSFile, data: String) {
        file.data = data
    }

    override fun syncFileWithFrontend(file: VFSFile, data: String) {
        file.data = data
    }

    override fun getProjectRoot(): VFSDirectory = root!!

    override fun getProjectPathPrefix(): String = projectPath.toString()

    private fun loadDirectory(node: VFSDirectory, dirPath: Path) {
        Files.list(dirPath)
            .forEach { file ->
                if (file.isDirectory()) {
                    val childDir = VFSDirectory(this, file.name, node)
                    node.addChildNode(childDir)
                    loadDirectory(childDir, file.toAbsolutePath())
                }
                else {
                    node.addChildNode(VFSFile(this, file.name, node))
                }
            }
    }

    private fun printProjectFiles() = printProjectFilesImpl(root)
    private fun printProjectFilesImpl(root: VFSDirectory?) {
        if (root == null) {
            println("Empty project")
            return
        }

        for (node in root.getChildren()) {
            println(node.getFilename())
            if (node.isDirectory()) {
                printProjectFilesImpl(node as VFSDirectory)
            }
        }
    }
}