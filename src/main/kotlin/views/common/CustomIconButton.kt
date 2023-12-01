package views.common

import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import views.design.CustomTheme


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CustomIconButton(
    onClick: () -> Unit,
    imageVector: ImageVector,
    enabled: Boolean = false,
    contentDescription: String? = null,
    modifier: Modifier = Modifier
    // tint: Color = LocalContentColor.current.copy(alpha = LocalContentAlpha.current),
) {
    val focusRequester = remember { FocusRequester() }
    val interactionSource = remember { MutableInteractionSource() }

    val hovered by interactionSource.collectIsHoveredAsState()
    val focused by interactionSource.collectIsFocusedAsState()

    fun selectBackgroundColor(): Color {
        if (!enabled) return CustomTheme.colors.disabledColor
        if (focused) return CustomTheme.colors.focusedAccentColor
        if (hovered) return CustomTheme.colors.hoveredColor
        return CustomTheme.colors.primaryColor
    }

    fun selectIconColor(): Color {
        if (!enabled) return CustomTheme.colors.disabledColor
        if (focused) return CustomTheme.colors.focusedAccentColor
        if (hovered) return CustomTheme.colors.hoveredColor
        return CustomTheme.colors.primaryColor
    }

    Icon(
        imageVector = imageVector,
        contentDescription = contentDescription,
        tint = selectIconColor(),
        modifier = modifier
            .focusRequester(focusRequester)
            .focusable(
                enabled = enabled,
                interactionSource = interactionSource
            )
            .hoverable(interactionSource = interactionSource)
            .onClick(enabled = enabled, onClick = { onClick() })
            .clickable(
                enabled = enabled,
                interactionSource = interactionSource,
                indication = null,
                onClick = { if (enabled) onClick() }
            )
            .border(0.5.dp, selectBackgroundColor(), RoundedCornerShape(4.dp))
    )
}