package views

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import components.resizable.SplitState
import components.resizable.VerticallySplittable
import components.vfs.OSVirtualFileSystem
import viewmodels.FileTreeViewModel
import viewmodels.ProjectViewModel
import viewmodels.TabsViewModel
import views.common.CustomTheme
import views.common.Fonts
import views.common.Settings
import views.filestree.FileTree
import views.filestree.FileTreeLabel
import views.tabs.TabsContainer
import views.editor.Editor
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
    val settings by remember { mutableStateOf(Settings()) }

    val sidebarState = remember { SidebarState() }

    MaterialTheme(
        colors = CustomTheme.colors.material
    ) {
        Surface {
            VerticallySplittable(
                Modifier.fillMaxSize(),
                sidebarState.splitState,
                onResize = {
                    val delta = it
                    sidebarState.width = (sidebarState.width + delta).coerceAtLeast(sidebarState.minWidth)
                }
            ) {
                /**
                 * Sidebar with project files
                 */
                Column(
                    Modifier
                        .fillMaxHeight()
                        .width(sidebarState.width)
                ) {
                    FileTreeLabel()
                    FileTree(fileTreeViewModel)
                }

                /**
                 * Editor with opened files
                 */
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Top
                ) {
                    TabsContainer(
                        modifier = Modifier.fillMaxWidth().background(CustomTheme.colors.backgroundLight),
                        settings = settings,
                        tabsViewModel = tabsViewModel
                    )

                    val modifier = Modifier.fillMaxSize().background(CustomTheme.colors.backgroundDark)
                    val activeFile = tabsViewModel.getActiveFile()

                    if (activeFile != null) {
                        Box {
                            Editor(
                                modifier = modifier,
                                activeFileModel = activeFile,
                                settings = settings,
                            )
                        }
                    }
                    else {
                        Box(
                            modifier = modifier,
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = "Select file for modifications",
                                fontFamily = Fonts.JetBrainsMono(),
                                color = Color(1.0f, 1.0f, 1.0f, 0.6f),
                                fontSize = 22.sp
                            )
                        }
                    }
                }
            }
        }
    }
}
