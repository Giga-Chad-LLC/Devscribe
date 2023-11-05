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

class TextViewModel(coroutineScope: CoroutineScope, private var activeFileModel: PinnedFileModel) {
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

    init {
        // TODO: remove dispatcher from TextModelView (remove it completely, I guess)
        val dispatcher = KeyboardEventDispatcher.getInstance()

        dispatcher.subscribe(KeyboardAction.SAVE_FILE) {
            val currentFile = activeFileModel
            syncModelWithVFS(currentFile)
            saveFileOnDisk(currentFile)
        }
        dispatcher.subscribe(KeyboardAction.BACKSPACE) {
            activeFileModel.textModel.backspace()
            debounceHandler.run(activeFileModel)
        }
        dispatcher.subscribe(KeyboardAction.NEWLINE) {
            activeFileModel.textModel.newline()
            debounceHandler.run(activeFileModel)
        }
        dispatcher.subscribe(KeyboardAction.DIRECTION_UP) {
            activeFileModel.textModel.changeCursorPositionDirectionUp()
        }
        dispatcher.subscribe(KeyboardAction.DIRECTION_RIGHT) {
            activeFileModel.textModel.changeCursorPositionDirectionRight()
        }
        dispatcher.subscribe(KeyboardAction.DIRECTION_DOWN) {
            activeFileModel.textModel.changeCursorPositionDirectionDown()
        }
        dispatcher.subscribe(KeyboardAction.DIRECTION_LEFT) {
            activeFileModel.textModel.changeCursorPositionDirectionLeft()
        }
        dispatcher.subscribe(KeyboardAction.SPACE) {
            activeFileModel.textModel.insert(TextConstants.nonBreakingSpaceChar)
            debounceHandler.run(activeFileModel)
        }
        dispatcher.subscribe(KeyboardAction.DELETE) {
            activeFileModel.textModel.delete()
            debounceHandler.run(activeFileModel)
        }
        dispatcher.subscribe(KeyboardAction.PRINTABLE_SYMBOL) {
            activeFileModel.textModel.insert(it.utf16CodePoint.toChar())
            debounceHandler.run(activeFileModel)
        }
    }
}