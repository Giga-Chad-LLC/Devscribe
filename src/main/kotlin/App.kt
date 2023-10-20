import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import components.ui.text.TextCanvas

@Composable
@Preview
fun App(textViewModel: TextViewModel) {
    MaterialTheme {
        Box {
            TextCanvas(
                modifier = Modifier
                    .padding(10.dp)
                    .fillMaxSize()
                    .border(BorderStroke(1.dp, Color.Red)),
                textViewModel = textViewModel,
            )
        }
    }
}