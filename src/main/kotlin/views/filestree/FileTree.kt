package views.filestree

import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.clickable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.material.Icon
import androidx.compose.material.LocalContentColor
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import models.FileTreeModel
import viewmodels.FileTreeViewModel


@Composable
fun FileTreeLabel() = Row(
    Modifier.padding(8.dp),
    verticalAlignment = Alignment.CenterVertically
) {
    Text(
        "Project files"
    )
}


@Composable
fun FileTree(fileTreeViewModel: FileTreeViewModel) = Box {
    val scrollState = rememberLazyListState()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        state = scrollState
    ) {
        items(fileTreeViewModel.nodes.size) { // model.nodes.size
            FileTreeItem(14.sp, 21.dp, fileTreeViewModel.nodes[it], fileTreeViewModel)
        }
    }

    VerticalScrollbar(
        rememberScrollbarAdapter(scrollState),
        Modifier.align(Alignment.CenterEnd)
    )
}


@Composable
private fun FileTreeItem(fontSize: TextUnit, height: Dp, node: FileTreeModel.NodeModel, fileTreeViewModel: FileTreeViewModel) = Row(
    modifier = Modifier
        .wrapContentHeight()
        .clickable { fileTreeViewModel.click(node) }
        .padding(start = 24.dp * node.level)
        .height(height)
        .fillMaxWidth()
) {
    val interactionSource = remember { MutableInteractionSource() }
    val active by interactionSource.collectIsHoveredAsState()

    // FileItemIcon(Modifier.align(Alignment.CenterVertically))
    Icon(Icons.Default.Email, contentDescription = null, tint = Color(0xFF87939A))
    Text(
        text = node.filename,
        color = if (active) LocalContentColor.current.copy(alpha = 0.60f) else LocalContentColor.current,
        modifier = Modifier
            .align(Alignment.CenterVertically)
            .clipToBounds()
            .hoverable(interactionSource),
        softWrap = true,
        fontSize = fontSize,
        overflow = TextOverflow.Ellipsis,
        maxLines = 1
    )
}
