package views.filestree

import androidx.compose.foundation.clickable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.*
import androidx.compose.material.Icon
import androidx.compose.material.LocalContentColor
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import models.FileTreeModel
import viewmodels.FileTreeViewModel


@Composable
fun FileTreeItem(fontSize: TextUnit, height: Dp, node: FileTreeModel.NodeModel, fileTreeViewModel: FileTreeViewModel) = Row(
    modifier = Modifier
        .wrapContentHeight()
        .clickable { fileTreeViewModel.click(node) }
        .padding(start = 24.dp * node.level)
        .height(height)
        .fillMaxWidth()
) {
    val interactionSource = remember { MutableInteractionSource() }
    val active by interactionSource.collectIsHoveredAsState()

    // TODO: add FileItemIcon(Modifier.align(Alignment.CenterVertically)) which chooses icon based on file extension
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
