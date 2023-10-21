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
        for (fileModel in tabsViewModel.files) {
            Tab(
                filename = fileModel.filename,
                active = fileModel.active,
                onTabClick = { tabsViewModel.select(fileModel.id) },
                onCloseButtonClick = {
                    // TODO: check whether file is saved
                    tabsViewModel.unpin(fileModel.id)
                }
            )
        }

    }
}