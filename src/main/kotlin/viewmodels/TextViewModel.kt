package viewmodels

import androidx.compose.ui.input.key.utf16CodePoint
import common.TextConstants
import components.DebounceHandler
import components.dispatcher.KeyboardEventDispatcher
import components.dispatcher.KeyboardEventDispatcher.KeyboardAction
import components.vfs.commands.SaveFileOnDiskCommand
import components.vfs.commands.SyncFileWithFrontendCommand
import kotlinx.coroutines.CoroutineScope
import models.PinnedFileModel
import models.text.Cursor
import models.text.TextModel

class TextViewModel(coroutineScope: CoroutineScope, private var activeFileModel: PinnedFileModel) {
    val textModel: TextModel
        get() {
            return activeFileModel.textModel
        }

    val text: String
        get() {
            return activeFileModel.textModel.text
        }

    val cursor: Cursor
        get() {
            return activeFileModel.textModel.cursor
        }

    private val debounceHandler = DebounceHandler(
        300,
        coroutineScope,
        TextViewModel::syncModelWithVFS
    )

    /**
     * Updates current active file model if provided pinned file model is not the same as current file
     */
    fun updateActiveFileModel(other: PinnedFileModel) {
        if (activeFileModel.id != other.id) {
            activeFileModel = other
        }
    }

    companion object {
        private fun syncModelWithVFS(fileToSyncWith: PinnedFileModel?) {
            if (fileToSyncWith == null) return

            println("Send local text model to VFS")
            val vfs = fileToSyncWith.virtualFile.getVirtualFileSystem()

            vfs.post(
                SyncFileWithFrontendCommand(
                    vfs,
                    fileToSyncWith.virtualFile,
                    fileToSyncWith.textModel.text
                )
            )
        }

        private fun saveFileOnDisk(fileToSave: PinnedFileModel?) {
            if (fileToSave == null) return

            println("Save file to the disk")
            val vfs = fileToSave.virtualFile.getVirtualFileSystem()

            vfs.post(SaveFileOnDiskCommand(
                fileToSave.virtualFile
            ))
        }
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

    fun symbol(ch: Char) {
        activeFileModel.textModel.insert(ch)
        debounceHandler.run(activeFileModel)
    }

    fun forwardToNextWord() {
        activeFileModel.textModel.forwardToNextWord()
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