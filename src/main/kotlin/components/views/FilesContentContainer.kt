package components.views

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import components.viewmodels.TextViewModel
import components.views.tabs.TabsContainer
import components.views.text.TextCanvas


@Composable
fun FilesContentContainer(modifier: Modifier, textViewModel: TextViewModel) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.Top
    ) {
        TabsContainer(
            modifier = Modifier
                .fillMaxWidth()
                .border(BorderStroke(1.dp, Color.Green))
        )

        TextCanvas(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.LightGray)
                .border(BorderStroke(1.dp, Color.Red)),
            textViewModel = textViewModel,
        )
    }
}