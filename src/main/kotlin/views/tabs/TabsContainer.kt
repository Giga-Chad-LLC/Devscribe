package views.tabs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import viewmodels.TabsViewModel
import views.design.Settings


@Composable
fun TabsContainer(modifier: Modifier, settings: Settings, tabsViewModel: TabsViewModel) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.Start
    ) {
        for (pinnedFileModel in tabsViewModel.files) {
            val isActive = (tabsViewModel.activeFile?.id == pinnedFileModel.id)

            Tab(
                filename = pinnedFileModel.filename,
                active = isActive,
                onTabClick = { tabsViewModel.select(pinnedFileModel.id) },
                settings = settings,
                onCloseButtonClick = {
                    // TODO: check whether file is saved
                    tabsViewModel.unpin(pinnedFileModel.id)
                }
            )
        }

    }
}