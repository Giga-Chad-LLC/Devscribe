package views.tabs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import viewmodels.TabsViewModel


@Composable
fun TabsContainer(modifier: Modifier, tabsViewModel: TabsViewModel) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.Start
    ) {
        for (pinnedFileModel in tabsViewModel.files) {
            Tab(
                filename = pinnedFileModel.filename,
                active = pinnedFileModel.active,
                onTabClick = { tabsViewModel.select(pinnedFileModel.id) },
                onCloseButtonClick = {
                    // TODO: check whether file is saved
                    tabsViewModel.unpin(pinnedFileModel.id)
                }
            )
        }

    }
}