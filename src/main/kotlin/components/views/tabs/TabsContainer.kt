package components.views.tabs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier


@Composable
fun TabsContainer(modifier: Modifier) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.Start
    ) {

        for (i in 1..3) {
            Tab("File $i")
        }
    }
}