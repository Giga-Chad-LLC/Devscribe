package components.models

import androidx.compose.runtime.mutableStateListOf

class ProjectModel {
    // TODO: projectGraph: Node?
    private val _projectFiles = mutableStateListOf<FileModel>()
    private val tabsModel = TabsModel()

    val projectFiles: List<FileModel>
        get() = projectFiles.toList()
}