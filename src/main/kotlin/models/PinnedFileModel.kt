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
    virtualFile: VFSFile
) {
    var virtualFile by mutableStateOf(virtualFile)
    var filename by mutableStateOf(virtualFile.filename)
    var id: UUID = UUID.randomUUID()

//    val filename: String
//        get() {
//            val rwlock = GlobalReadWriteLock.getInstance()
//            rwlock.lockRead()
//
//            try {
//                return virtualFile.filename
//            }
//            finally {
//                rwlock.unlockRead()
//            }
//        }
    val textModel: TextModel by mutableStateOf(LineArrayTextModel())

    override fun toString(): String {
        return "FileModel[id='$id'; virtualFile='$virtualFile']"
    }
}