package viewmodels

import components.DebounceHandler
import components.KeyboardEventDispatcher
import components.KeyboardEventDispatcher.KeyboardAction
import components.vfs.commands.SaveFileOnDiskCommand
import components.vfs.commands.SyncFileWithFrontendCommand
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import models.PinnedFileModel
import models.text.Cursor
import models.text.TextModel

class TextViewModel(
    private val coroutineScope: CoroutineScope,
    private var activeFileModel: PinnedFileModel
) {
    val textModel: TextModel
        get() {
            return activeFileModel.textModel
        }

    val cursor: Cursor
        get() {
            return activeFileModel.textModel.cursor
        }

    private val debounceHandler = DebounceHandler(
        300,
        coroutineScope,
        this::syncModelWithVFS
    )

    /**
     * Updates current active file model if provided pinned file model is not the same as current file.
     *
     * Returns true if a file updates successfully, otherwise false.
     */
    fun updateActiveFileModel(other: PinnedFileModel): Boolean {
        if (activeFileModel.id != other.id) {
            activeFileModel = other
            return true
        }
        return false
    }

    private fun syncModelWithVFS(fileToSyncWith: PinnedFileModel?) {
        if (fileToSyncWith == null) return

        println("Send local text model to VFS")
        val vfs = fileToSyncWith.virtualFile.getVirtualFileSystem()

        vfs.post(
            SyncFileWithFrontendCommand(
                vfs,
                fileToSyncWith.virtualFile,
                fileToSyncWith.textModel.text
            ) {
                coroutineScope.launch {
                    fileToSyncWith.isSaved = fileToSyncWith.virtualFile.isSaved
                }
            }
        )
    }

    private fun saveFileOnDisk(fileToSave: PinnedFileModel?) {
        if (fileToSave == null) return

        println("Save file to the disk")
        val vfs = fileToSave.virtualFile.getVirtualFileSystem()

        vfs.post(SaveFileOnDiskCommand(fileToSave.virtualFile) {
            coroutineScope.launch {
                fileToSave.isSaved = fileToSave.virtualFile.isSaved
            }
        })
    }


    fun backspace() {
        activeFileModel.textModel.backspace()
        debounceHandler.run(activeFileModel)
    }

    fun newline() {
        activeFileModel.textModel.newline()
        debounceHandler.run(activeFileModel)
    }

    fun directionUp() {
        activeFileModel.textModel.changeCursorPositionDirectionUp()
    }

    fun directionRight() {
        activeFileModel.textModel.changeCursorPositionDirectionRight()
    }

    fun directionDown() {
        activeFileModel.textModel.changeCursorPositionDirectionDown()
    }

    fun directionLeft() {
        activeFileModel.textModel.changeCursorPositionDirectionLeft()
    }

    fun whitespace() {
        activeFileModel.textModel.insert(' ')
        debounceHandler.run(activeFileModel)
    }

    fun delete() {
        activeFileModel.textModel.delete()
        debounceHandler.run(activeFileModel)
    }

    /**
     * Removes piece of text in range [startOffset, endOffset)
     */
    fun delete(startOffset: Int, endOffset: Int) {
        activeFileModel.textModel.removeRange(startOffset, endOffset)
        debounceHandler.run(activeFileModel)
    }

    fun insert(ch: Char) {
        activeFileModel.textModel.insert(ch)
        debounceHandler.run(activeFileModel)
    }

    fun insert(str: String) {
        activeFileModel.textModel.insert(str)
        debounceHandler.run(activeFileModel)
    }

    fun forwardToNextWord() {
        activeFileModel.textModel.forwardToNextWord()
    }

    fun backwardToPreviousWord() {
        activeFileModel.textModel.backwardToPreviousWord()
    }

    init {
        // TODO: remove dispatcher from TextModelView (remove it completely, I guess)
        val dispatcher = KeyboardEventDispatcher.getInstance()

        dispatcher.subscribe(KeyboardAction.SAVE_FILE) {
            println("Saving file on CTRL+S...")
            val currentFile = activeFileModel
            syncModelWithVFS(currentFile)
            saveFileOnDisk(currentFile)
        }
    }
}