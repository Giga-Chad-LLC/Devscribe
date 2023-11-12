package components.resizable

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.input.pointer.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.MeasurePolicy
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import java.awt.Cursor


@Composable
fun VerticallySplittable(
    modifier: Modifier,
    state: SplitState,
    onResize: (delta: Dp) -> Unit,
    children: @Composable () -> Unit,
) {
    val content = @Composable {
        children()
        VerticalSplit(state, onResize)
    }

    val measurePolicy = MeasurePolicy { measurables, constraints ->
        require(measurables.size == 3)

        val leftPlaceable = measurables[0].measure(constraints.copy(minWidth = 0))

        val rightPlaceableWidth = constraints.maxWidth - leftPlaceable.width

        val rightPlaceable = measurables[1].measure(
            Constraints(
                minWidth = rightPlaceableWidth,
                maxWidth = rightPlaceableWidth,
                minHeight = constraints.maxHeight,
                maxHeight = constraints.maxHeight
            )
        )

        val splitPlaceable = measurables[2].measure(constraints)

        layout(constraints.maxWidth, constraints.maxHeight) {
            leftPlaceable.place(0, 0)
            rightPlaceable.place(leftPlaceable.width, 0)
            splitPlaceable.place(leftPlaceable.width, 0)
        }
    }

    Layout(content, modifier, measurePolicy)
}


class SplitState {
    var isResizing by mutableStateOf(false)
    var isResizingEnabled by mutableStateOf(true)
}


@Composable
fun VerticalSplit(
    state: SplitState,
    onResize:(delta: Dp) -> Unit,
    lineColor: Color = Color.Transparent
) = Box {
    val currentDensity = LocalDensity.current

    /**
     * draggable region
     */
    Box(
        modifier = Modifier
            .width(10.dp)
            .fillMaxSize()
            .pointerHoverIcon(PointerIcon(Cursor(Cursor.E_RESIZE_CURSOR)))
            .run {
                if (state.isResizingEnabled) {
                    this.draggable(
                        state = rememberDraggableState {
                            with(currentDensity) {
                                onResize(it.toDp())
                            }
                        },
                        orientation = Orientation.Horizontal,
                        startDragImmediately = true,
                        onDragStarted = { state.isResizing = true },
                        onDragStopped = { state.isResizing = false },
                    )
                }
                else {
                    this
                }
            },
        contentAlignment = Alignment.Center
    ) {
        /**
         * displayable splitting line
         */
        Box(
            Modifier
                .width(1.dp)
                .fillMaxHeight()
                .background(lineColor)
        )
    }
}