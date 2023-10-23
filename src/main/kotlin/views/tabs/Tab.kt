package views.tabs

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import views.common.Settings

@Composable
fun Tab(
    filename: String,
    active: Boolean,
    settings: Settings,
    onTabClick: () -> Unit,
    onCloseButtonClick: () -> Unit
    ) {
    val tabFocusManager = LocalFocusManager.current
    val interactionSource = remember { MutableInteractionSource() }
    val hovered by interactionSource.collectIsHoveredAsState()

    fun selectBackgroundColor(): Color {
        if (active) {
            return Color.Blue
        }
        if (hovered) {
            return Color.LightGray
        }
        return Color.DarkGray
    }

    Row(
        modifier = Modifier
            .background(selectBackgroundColor())
            .hoverable(interactionSource = interactionSource)
            .clickable(
                role = Role.Tab,
                onClick = {
                    onTabClick()
                    tabFocusManager.clearFocus()
                }
            )
            .drawBehind {
                /**
                 * Drawing border on the left side of a container
                 */
                val strokeWidth = 1.dp.value * density
                val x = size.width - strokeWidth / 2

                drawLine(
                    color = Color.Black,
                    start = Offset(x, 0f),
                    end = Offset(x, size.height),
                    strokeWidth = strokeWidth
                )
            },
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            modifier = Modifier.padding(10.dp, 6.dp, 7.dp, 6.dp),
            text = filename,
            color = Color.White,
            fontFamily = settings.fontSettings.fontFamily,
            fontSize = 14.sp,
        )
        Icon(
            modifier = Modifier
                .padding(0.dp, 0.dp, 5.dp, 0.dp)
                .width(15.dp)
                .clickable {
                    onCloseButtonClick()
                    tabFocusManager.clearFocus()
               },
            imageVector = Icons.Rounded.Close,
            tint = Color.White,
            contentDescription = "Close tab '$filename'"
        )
    }
}