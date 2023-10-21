package components.views.tabs

import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp

@Composable
fun Tab(filename: String) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()

    Box(
        modifier = Modifier
            .background(if (isHovered) Color.LightGray else Color.DarkGray)
            .hoverable(interactionSource = interactionSource)
            .clickable { println("Tab with filename $filename clicked") }
            .drawBehind {
                val strokeWidth = 1.dp.value * density
                val x = size.width - strokeWidth / 2

                drawLine(
                    color = Color.Black,
                    start = Offset(x, 0f),
                    end = Offset(x, size.height),
                    strokeWidth = strokeWidth
                )
            },
    ) {
        Text(
            text = filename,
            modifier = Modifier
                .padding(15.dp, 6.dp),
            color = Color.White
        )
    }
}