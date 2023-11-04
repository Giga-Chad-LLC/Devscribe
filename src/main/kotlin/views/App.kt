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
import components.resizable.SplitState
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


class SidebarState {
    var width by mutableStateOf(300.dp)
    val minWidth =100.dp
    val splitState = SplitState()
}

@Composable
@Preview
fun App() {
    val vfs = OSVirtualFileSystem(Path.of("C:/Users/Vladislav/Downloads/devscribe-project-folder"))
    val coroutineScope = rememberCoroutineScope() // required to run the state updates on the same scope as components composed
    val projectViewModel by remember { mutableStateOf(ProjectViewModel(vfs, coroutineScope)) }
    val tabsViewModel by remember { mutableStateOf(TabsViewModel(projectViewModel.tabsModel, coroutineScope)) }
    val fileTreeViewModel by remember { mutableStateOf(FileTreeViewModel(projectViewModel.fileTreeModel, tabsViewModel)) }
    var settings by remember { mutableStateOf(Settings()) }

    var sidebarState = remember { SidebarState() }

    MaterialTheme(
        colors = CustomTheme.colors.material
    ) {
        Surface {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                /**
                 * Sidebar with project files
                 */
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(0.2f)
                ) {
                    FileTreeLabel()
                    FileTree(fileTreeViewModel)
                }

                /**
                 * Editor with opened files
                 */
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
