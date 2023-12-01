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
import java.nio.file.attribute.FileTime
import java.time.Instant
import kotlin.io.path.exists
import kotlin.io.path.getLastModifiedTime
import kotlin.io.path.isDirectory
import kotlin.io.path.name

// TODO: make the class singleton? (for easier access across composables and logically it is correct to do so, I suppose)
class OSVirtualFileSystem : VirtualFileSystem {
    private val worker: VFSWorker = VFSWorker()
    private var projectPath: Path? = null
    private var root: VFSDirectory? = null
    private var idIncrementor: Int = 0

    override fun post(command: VFSCommand) {
        worker.postCommand(command)
    }

    override fun index() {
        println("Indexing project: '$projectPath'")
        if (projectPath == null || !projectPath!!.exists()) {
            throw FileNotFoundException("Cannot load VFS for project at '$projectPath'")
        }

        root = VFSDirectory(this, projectPath!!.name, null, getNextNodeId(), projectPath!!.getLastModifiedTime())
        indexProject(root!!, projectPath!!)
    }

    override fun syncFileWithDisk(file: VFSFile, data: String, timestamp: FileTime) {
        file.data = data
        file.timestamp = timestamp
    }

    override fun syncFileWithFrontend(file: VFSFile, data: String) {
        file.data = data
        file.timestamp = FileTime.from(Instant.now())
    }

    override fun getProjectRoot(): VFSDirectory = root!!

    override fun getProjectPath(): String? = projectPath?.toString()

    override fun setProjectPath(rootDir: Path) {
        projectPath = rootDir
    }

    private fun indexProject(node: VFSDirectory, dirPath: Path) {
        Files.list(dirPath)
            .forEach { file ->
                if (file.isDirectory()) {
                    val childDir = VFSDirectory(this, file.name, node, getNextNodeId(), file.getLastModifiedTime())
                    node.addChildNode(childDir)
                    indexProject(childDir, file.toAbsolutePath())
                }
                else {
                    // Files have content, and it must be loaded separately.
                    // That's why we set the timestamp to the minimal value
                    node.addChildNode(VFSFile(this, file.name, node, getNextNodeId(), FileTime.from(Instant.MIN)))
                }
            }
    }

    private fun getNextNodeId(): Int {
        return idIncrementor++
    }
}