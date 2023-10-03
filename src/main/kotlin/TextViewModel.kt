import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

class TextViewModel {
    var text by mutableStateOf("")
        private set

    fun insertCharacter(ch: Char) {
        text += ch
    }
}