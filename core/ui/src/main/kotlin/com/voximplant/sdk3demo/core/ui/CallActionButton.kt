package com.voximplant.sdk3demo.core.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.voximplant.sdk3demo.core.designsystem.icon.Icons
import com.voximplant.sdk3demo.core.designsystem.theme.VoximplantTheme

@Composable
fun CallActionButton(
    icon: @Composable () -> Unit,
    text: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    color: Color = Color(0x65202020),
    onClick: () -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    Column(
        modifier = modifier
            .padding(vertical = 4.dp)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = enabled,
                onClick = onClick,
            ),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        FilledIconButton(
            onClick = onClick,
            modifier = Modifier
                .padding(horizontal = 8.dp)
                .size(64.dp),
            enabled = enabled,
            colors = IconButtonDefaults.filledIconButtonColors(containerColor = color),
            interactionSource = interactionSource,
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp),
                propagateMinConstraints = true,
            ) {
                icon()
            }
        }
        Box(modifier = Modifier.padding(horizontal = 8.dp)) {
            text()
        }
    }
}

@Preview
@Composable
private fun CallActionButtonPreview() {
    var enabled by remember { mutableStateOf(true) }

    VoximplantTheme {
        Surface {
            CallActionButton(
                icon = {
                    Icon(painter = painterResource(id = Icons.Microphone), contentDescription = null)
                },
                text = {
                    Text(text = "Mute")
                },
                onClick = { enabled = !enabled },
                color = if (enabled) Color(0x65202020) else Color.White
            )
        }
    }
}
