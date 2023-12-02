package views.filestree

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.Icon
import androidx.compose.material.LocalContentColor
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import models.FileTreeModel


@Composable
fun FileTreeItemIcon(modifier: Modifier, node: FileTreeModel.NodeModel) {
    Box(modifier.size(24.dp).padding(4.dp)) {
        when (val type = node.type) {
            is FileTreeModel.NodeType.Folder -> if (type.canExpand) {
                Icon(
                    if (type.isExpanded) Icons.Default.KeyboardArrowDown else Icons.Default.KeyboardArrowRight,
                    contentDescription = null,
                    tint = LocalContentColor.current
                )
            }

            is FileTreeModel.NodeType.File -> when (type.extension) {
                // TODO: add some stylish to the icons
                "txt" -> Icon(Icons.Default.Description, contentDescription = null, tint = Color(0xFF87939A))
                else -> Icon(Icons.Default.QuestionMark, contentDescription = null, tint = Color(0xFF87939A))
            }
        }
    }
}