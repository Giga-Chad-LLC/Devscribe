package views.filestree

import androidx.compose.foundation.*
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


@OptIn(ExperimentalComposeUiApi::class, ExperimentalFoundationApi::class)
@Composable
fun FileTreeItem(
    fontSize: TextUnit,
    height: Dp,
    node: FileTreeModel.NodeModel,
    fileTreeViewModel: FileTreeViewModel,
    onDrag: (DpOffset) -> Unit = {}
) {
    val styledContextMenuRepresentation = DefaultContextMenuRepresentation(
        backgroundColor = CustomTheme.colors.backgroundDark,
        textColor = FontSettings().fontColor,
        itemHoverColor = CustomTheme.colors.primaryColor.copy(alpha = 0.04f)
    )
    val borderRadius = 3.dp

    var isRenaming by remember { mutableStateOf(false) }
    var renameNodeTo by remember { mutableStateOf(node.filename) }

    var isDragging by remember { mutableStateOf(false) }
    var dragOffset by remember { mutableStateOf(Offset(0f, 0f)) }

    var isFocusOnMountRequired by remember { mutableStateOf(true) }
    val focusRequester = remember { FocusRequester() }


    Modifier
        .wrapContentHeight()

    CompositionLocalProvider(LocalContextMenuRepresentation provides styledContextMenuRepresentation) {
        ContextMenuArea(
            items = {
                listOf(
                    ContextMenuItem("Rename") {
                        println("Start renaming file")
                        println("Custom right click on ${node.filename} and renameNodeTo $renameNodeTo")
                        isRenaming = true
                        renameNodeTo = node.filename
                    },
                    ContextMenuItem("Delete") {
                        println("Delete file")
                        // TODO
                    }
                )
            }
        ) {
            Row(
                modifier = Modifier
                    .background(if (isDragging) CustomTheme.colors.focusedAccentColor else Color.Transparent)
                    .clickable {
                        fileTreeViewModel.click(node)
                    }
                    .padding(start = 24.dp * node.level)
                    .offset {
                        IntOffset(dragOffset.x.toInt(), dragOffset.y.toInt())
                    }
                    .height(height)
                    .fillMaxWidth()
                    .onDrag(
                        onDragStart = {
                            println("onDragStart: ${node.filename}")
                            isDragging = true
                        },
                        onDrag = {
                            println("onDrag($it): ${node.filename}")
                            val shift = DpOffset(x = it.x.dp, y = it.y.dp)
                            onDrag(shift)
                            dragOffset += it
                        },
                        onDragEnd = {
                            println("onDragEnd: ${node.filename}")
                            isDragging = false
                            dragOffset = Offset.Zero
                        }
                    )
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
                            println("Rename node to: $renameNodeTo")
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
