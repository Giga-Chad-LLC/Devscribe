package views

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import viewmodels.ProjectViewModel
import viewmodels.TabsViewModel
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
                )

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(0.7f),
                    verticalArrangement = Arrangement.Top
                ) {
                    TabsContainer(
                        modifier = Modifier.fillMaxWidth().background(CustomTheme.colors.backgroundLight),
                        settings = settings,
                        tabsViewModel = tabsViewModel
                    )

                    TextCanvas(
                        modifier = Modifier.fillMaxSize().background(CustomTheme.colors.backgroundDark),
                        activeFileModel = tabsViewModel.getActiveFile(),
                        settings = settings,
                    )
                }
            }
        }
    }
}