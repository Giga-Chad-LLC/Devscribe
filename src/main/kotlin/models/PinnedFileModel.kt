package models

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import components.GlobalReadWriteLock
import components.vfs.nodes.VFSFile
import kotlinx.coroutines.CoroutineScope
import models.text.LineArrayTextModel
import models.text.TextModel
import java.util.*

class PinnedFileModel(
    val virtualFile: VFSFile
) {
    var id: UUID = UUID.randomUUID()

    val filename: String
        get() {
            val rwlock = GlobalReadWriteLock.getInstance()
            rwlock.lockRead()

            try {
                return virtualFile.getFilename()
            }
            finally {
                rwlock.unlockRead()
            }
        }
    val textModel: TextModel by mutableStateOf(LineArrayTextModel())

    override fun toString(): String {
        return "FileModel[id='$id'; virtualFile='$virtualFile']"
    }
}