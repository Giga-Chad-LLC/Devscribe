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
import components.vfs.OSVirtualFileSystem
import viewmodels.FileTreeViewModel
import viewmodels.ProjectViewModel
import viewmodels.TabsViewModel
import views.common.CustomTheme
import views.common.Settings
import views.filestree.FileTree
import views.filestree.FileTreeLabel
import views.tabs.TabsContainer
import views.text.TextCanvas
import java.nio.file.Path

@Composable
@Preview
fun App() {
    val vfs = OSVirtualFileSystem(Path.of("C:/Users/dmitriiart/Downloads/ProjectFolder"))
    val coroutineScope = rememberCoroutineScope() // required to run the state updates on the same scope as components composed
    val projectViewModel by remember { mutableStateOf(ProjectViewModel(vfs, coroutineScope)) }
    val tabsViewModel by remember { mutableStateOf(TabsViewModel(projectViewModel.tabsModel, coroutineScope)) }
    val fileTreeViewModel by remember { mutableStateOf(FileTreeViewModel(projectViewModel.fileTreeModel, tabsViewModel)) }
    var settings by remember { mutableStateOf(Settings()) }

    MaterialTheme(
        colors = CustomTheme.colors.material
    ) {
        Surface {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(0.2f)
                        .border(BorderStroke(1.dp, Color.Blue))
                ) {
                    FileTreeLabel()
                    FileTree(fileTreeViewModel)
                }

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