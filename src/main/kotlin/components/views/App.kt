package components.views

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import components.viewmodels.TextViewModel

@Composable
@Preview
fun App(textViewModel: TextViewModel) {
    MaterialTheme {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(0.3f)
                    .border(BorderStroke(1.dp, Color.Blue))
            )

            FilesContentContainer(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(0.7f)
                    .border(BorderStroke(1.dp, Color.Blue)),
                textViewModel = textViewModel
            )
        }
    }
}