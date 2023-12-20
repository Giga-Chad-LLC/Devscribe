package views.filestree

import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import viewmodels.FileTreeViewModel


@Composable
fun FileTree(fileTreeViewModel: FileTreeViewModel) = Box {
    val scrollState = rememberLazyListState()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        state = scrollState
    ) {
        items(
            count = fileTreeViewModel.nodes.size,
            itemContent = { index ->
                FileTreeItem(14.sp, 21.dp, fileTreeViewModel.nodes[index], fileTreeViewModel)
            }
        )
    }

    VerticalScrollbar(
        rememberScrollbarAdapter(scrollState),
        Modifier.align(Alignment.CenterEnd)
    )
}

