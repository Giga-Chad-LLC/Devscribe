package views

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import viewmodels.ProjectViewModel
import viewmodels.TabsViewModel
import viewmodels.TextViewModel
import views.common.CustomTheme
import views.common.Settings
import views.tabs.TabsContainer
import views.text.TextCanvas

@Composable
@Preview
fun App() {
    val projectViewModel by remember { mutableStateOf(ProjectViewModel()) }
    val tabsViewModel by remember { mutableStateOf(TabsViewModel(projectViewModel.project.tabsModel)) }
    var settings by remember { mutableStateOf(Settings()) }

    MaterialTheme(
        colors = CustomTheme.colors.material
    ) {
        Surface {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(0.2f)
                        .border(BorderStroke(1.dp, Color.Blue))
                )

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(0.7f)
                        .border(BorderStroke(1.dp, Color.Blue)),
                    verticalArrangement = Arrangement.Top
                ) {
                    TabsContainer(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(BorderStroke(1.dp, Color.Green)),
                        settings = settings,
                        tabsViewModel = tabsViewModel
                    )

                    TextCanvas(
                        modifier = Modifier
                            .fillMaxSize()
                            .border(BorderStroke(1.dp, Color.Red)),
                        activeFileModel = tabsViewModel.getActiveFile(),
                        settings = settings,
                    )
                }
            }
        }
    }
}