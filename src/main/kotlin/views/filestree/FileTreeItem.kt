package views.filestree

import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.gestures.onDrag
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.awtEventOrNull
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.key.*
import androidx.compose.ui.input.pointer.*
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.*
import kotlinx.coroutines.job
import models.FileTreeModel
import viewmodels.FileTreeViewModel
import views.design.CustomTheme
import views.design.FontSettings


@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun FileTreeItem(
    fontSize: TextUnit,
    height: Dp,
    node: FileTreeModel.NodeModel,
    fileTreeViewModel: FileTreeViewModel,
) {
    val styledContextMenuRepresentation = DefaultContextMenuRepresentation(
        backgroundColor = CustomTheme.colors.backgroundDark,
        textColor = FontSettings().fontColor,
        itemHoverColor = CustomTheme.colors.primaryColor.copy(alpha = 0.04f)
    )
    val borderRadius = 3.dp

    var isRenaming by remember { mutableStateOf(false) }
    var renameNodeTo by remember { mutableStateOf(node.filename) }

    var isFocusOnMountRequired by remember { mutableStateOf(true) }
    val focusRequester = remember { FocusRequester() }

    val contextMenuCommands = mutableListOf(
        ContextMenuItem("Rename") {
            println("Start renaming file")
            println("Custom right click on ${node.filename} and renameNodeTo $renameNodeTo")
            isRenaming = true
            renameNodeTo = node.filename
        },
        ContextMenuItem("Delete") {
            println("Delete file")
            fileTreeViewModel.remove(node)
        }
    )

    if (node.type is FileTreeModel.NodeType.Folder) {
        contextMenuCommands.addAll(listOf(
            ContextMenuItem("Add file") {
                println("Create file in ${node.filename}")
                fileTreeViewModel.addFile(node)
            },
            ContextMenuItem("Add folder") {
                println("Create folder in ${node.filename}")
                fileTreeViewModel.addFolder(node)
            }
        ))
    }

    Modifier
        .wrapContentHeight()

    CompositionLocalProvider(LocalContextMenuRepresentation provides styledContextMenuRepresentation) {
        ContextMenuArea(
            items = { contextMenuCommands }
        ) {
            Row(
                modifier = Modifier
                    .clickable {
                        fileTreeViewModel.click(node)
                    }
                    .padding(start = 24.dp * node.level)
                    .height(height)
                    .fillMaxWidth()
            ) {
                FileTreeItemIcon(Modifier.align(Alignment.CenterVertically), node)

                if (isRenaming) {
                    LaunchedEffect(Unit) {
                        println("Focus")
                        focusRequester.requestFocus()
                        isFocusOnMountRequired = false
                    }

                    BasicTextField(
                        value = renameNodeTo,
                        onValueChange = {
                            renameNodeTo = it
                        },
                        textStyle = TextStyle(
                            color = LocalContentColor.current,
                            fontSize = fontSize
                        ),
                        cursorBrush = SolidColor(LocalContentColor.current),
                        singleLine = true,

                        modifier = Modifier
                            .focusRequester(focusRequester)
                            .onKeyEvent { keyEvent ->
                                var consumed = false

                                if (
                                    keyEvent.type == KeyEventType.KeyDown &&
                                    (
                                        keyEvent.key == Key.Enter ||
                                        keyEvent.key == Key.Escape
                                    )
                                ) {
                                    isRenaming = false
                                    consumed = true

                                    if (keyEvent.key == Key.Enter) {
                                        fileTreeViewModel.rename(node, String(renameNodeTo.toByteArray()))
                                    }
                                }

                                consumed
                            }
                            .onFocusChanged {
                                if (!isFocusOnMountRequired && !it.isFocused) {
                                    isRenaming = false
                                    isFocusOnMountRequired = true
                                }
                            }
                            .clip(shape = RoundedCornerShape(borderRadius))
                            .border(
                                BorderStroke(0.5.dp, CustomTheme.colors.backgroundMedium),
                                RoundedCornerShape(borderRadius)
                            )
                            .background(CustomTheme.colors.backgroundLight)
                            .padding(2.dp, 0.dp)
                            .align(Alignment.CenterVertically)
                    )
                } else {
                    Text(
                        text = node.filename,
                        color = LocalContentColor.current,
                        modifier = Modifier
                            .align(Alignment.CenterVertically)
                            .clipToBounds(),
                        softWrap = true,
                        fontSize = fontSize,
                        overflow = TextOverflow.Ellipsis,
                        maxLines = 1
                    )
                }
            }
        }
    }
}
