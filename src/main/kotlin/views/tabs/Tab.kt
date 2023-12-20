package views.tabs

import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import views.design.CustomTheme
import views.design.Settings

@Composable
fun Tab(
    filename: String,
    active: Boolean,
    saved: Boolean,
    settings: Settings,
    onTabClick: () -> Unit,
    onCloseButtonClick: () -> Unit
    ) {
    val tabFocusRequester = remember { FocusRequester() }
    val tabInteractionSource = remember { MutableInteractionSource() }
    val closeIconFocusRequester = remember { FocusRequester() }
    val closeIconInteractionSource = remember { MutableInteractionSource() }

    val tabHovered by tabInteractionSource.collectIsHoveredAsState()
    val tabFocused by tabInteractionSource.collectIsFocusedAsState()

    val closeIconHovered by closeIconInteractionSource.collectIsHoveredAsState()
    val closeIconFocused by closeIconInteractionSource.collectIsFocusedAsState()

    fun selectTabBackgroundColor(): Color {
        if (tabHovered) return CustomTheme.colors.backgroundMedium
        if (active) return CustomTheme.colors.backgroundDark
        if (tabFocused) return CustomTheme.colors.backgroundMedium
        return CustomTheme.colors.backgroundLight
    }

    fun selectCloseIconColor(): Color {
        if (closeIconHovered) return Color.White
        if (closeIconFocused) return CustomTheme.colors.focusedAccentColor
        return Color.Gray
    }

    Row(
        modifier = Modifier
            .background(selectTabBackgroundColor())
            // focusRequester() should be added BEFORE focusable()
            .focusRequester(tabFocusRequester)
            .focusable(interactionSource = tabInteractionSource)
            .hoverable(interactionSource = tabInteractionSource)
            .clickable(
                interactionSource = tabInteractionSource,
                indication = null,
                onClick = { onTabClick() }
            )
            .drawBehind {
                /**
                 * Drawing border on the left side
                 */
                var strokeWidth = 0.5.dp.value * density
                val x = size.width - strokeWidth / 2 // TODO: need to divide to 2?
                drawLine(
                    color = CustomTheme.colors.backgroundMedium,
                    start = Offset(x, 0f),
                    end = Offset(x, size.height),
                    strokeWidth = strokeWidth,
                )

                /**
                 * Drawing border on the bottom if active or focused
                 */
                if (active || tabFocused) {
                    val color = if (tabFocused) CustomTheme.colors.focusedAccentColor else Color.White
                    strokeWidth = 1.dp.value * density
                    val y = size.height - strokeWidth
                    drawLine(
                        color = color,
                        start = Offset(0f, y),
                        end = Offset(size.width, y),
                        strokeWidth = strokeWidth,
                    )
                }
            },
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            modifier = Modifier.padding(10.dp, 6.dp, 7.dp, 6.dp),
            text = filename,
            color = if (saved) Color.White else Color.Gray,
            fontFamily = settings.fontSettings.fontFamily,
            fontSize = 14.sp,
        )

        Icon(
            modifier = Modifier
                .padding(0.dp, 0.dp, 5.dp, 0.dp)
                .width(15.dp)
                // focusRequester() should be added BEFORE focusable()
                .focusRequester(closeIconFocusRequester)
                .focusable(interactionSource = closeIconInteractionSource)
                .hoverable(interactionSource = closeIconInteractionSource)
                .clickable(
                    interactionSource = closeIconInteractionSource,
                    indication = null,
                    onClick = { onCloseButtonClick() }
                ),
            imageVector = Icons.Rounded.Close,
            tint = selectCloseIconColor(),
            contentDescription = "Close tab '$filename'"
        )
    }
}