/*
 * Copyright (c) 2011 - 2023, Zingaya, Inc. All rights reserved.
 */

package com.voximplant.demos.sdk.core.ui

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
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.voximplant.demos.sdk.core.designsystem.icon.Icons
import com.voximplant.demos.sdk.core.designsystem.theme.Gray10
import com.voximplant.demos.sdk.core.designsystem.theme.Typography
import com.voximplant.demos.sdk.core.designsystem.theme.VoximplantTheme

@Composable
fun CallActionButtonSmall(
    icon: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    text: @Composable() (() -> Unit)? = null,
    enabled: Boolean = true,
    color: Color = Color(0xFFDADAE6),
    onClick: () -> Unit,
) {
    CallActionButtonDefault(
        icon = icon,
        modifier = modifier,
        text = text,
        enabled = enabled,
        color = color,
        buttonSize = 40,
        iconSize = 24,
        onClick = onClick
    )
}

@Composable
fun CallActionButton(
    icon: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    text: @Composable() (() -> Unit)? = null,
    enabled: Boolean = true,
    color: Color = Color(0xFFDADAE6),
    onClick: () -> Unit,
) {
    CallActionButtonDefault(
        icon = icon,
        modifier = modifier,
        text = text,
        enabled = enabled,
        color = color,
        onClick = onClick
    )
}

@Composable
private fun CallActionButtonDefault(
    icon: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    text: @Composable() (() -> Unit)? = null,
    enabled: Boolean = true,
    color: Color = Color(0xFFDADAE6),
    buttonSize: Int = 64,
    iconSize: Int = 32,
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
                .size(buttonSize.dp),
            enabled = enabled,
            colors = IconButtonDefaults.filledIconButtonColors(containerColor = color),
            interactionSource = interactionSource,
        ) {
            Box(
                modifier = Modifier
                    .size(iconSize.dp),
                propagateMinConstraints = true,
            ) {
                icon()
            }
        }
        if (text != null) {
            Box(
                modifier = Modifier
                    .padding(horizontal = 8.dp)
                    .size(width = 64.dp, height = 48.dp),
                contentAlignment = Alignment.TopCenter,
            ) {
                CompositionLocalProvider(
                    LocalTextStyle provides Typography.bodySmall.copy(textAlign = TextAlign.Center),
                    LocalContentColor provides Gray10,
                ) {
                    text()
                }
            }
        }
    }
}

@Preview
@Composable
private fun CallActionButtonPreview() {
    var enabled by remember { mutableStateOf(false) }

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
                color = if (enabled) Color(0xFF1F1C28) else Color(0xFFDADAE6)
            )
        }
    }
}

@Preview
@Composable
private fun CallActionButtonWithLongTextPreview() {
    var enabled by remember { mutableStateOf(false) }

    VoximplantTheme {
        Surface {
            CallActionButton(
                icon = {
                    Icon(painter = painterResource(id = Icons.Bluetooth), contentDescription = null, tint = if (enabled) Color(0xFFDADAE6) else Color(0xFF1F1C28))
                },
                text = {
                    Text(text = "Google Pixel Buds Pro")
                },
                onClick = { enabled = !enabled },
                color = if (enabled) Color(0xFF1F1C28) else Color(0xFFDADAE6)
            )
        }
    }
}

@Preview
@Composable
private fun CallActionButtonWithoutTextPreview() {
    VoximplantTheme {
        Surface {
            CallActionButton(
                icon = {
                    Icon(
                        painter = painterResource(id = Icons.Hangup),
                        contentDescription = null,
                        tint = Color.White,
                    )
                },
                color = Color(0xFFF5222D),
                onClick = {},
            )
        }
    }
}

@Preview
@Composable
private fun CallActionButtonSmallPreview() {
    var enabled by remember { mutableStateOf(false) }

    VoximplantTheme {
        Surface {
            CallActionButtonSmall(
                icon = {
                    Icon(painter = painterResource(id = Icons.Microphone), contentDescription = null)
                },
                text = {
                    Text(text = "Mute")
                },
                onClick = { enabled = !enabled },
                color = if (enabled) Color(0xFF1F1C28) else Color(0xFFDADAE6)
            )
        }
    }
}

@Preview
@Composable
private fun CallActionButtonSmallWithLongTextPreview() {
    var enabled by remember { mutableStateOf(false) }

    VoximplantTheme {
        Surface {
            CallActionButtonSmall(
                icon = {
                    Icon(painter = painterResource(id = Icons.Bluetooth), contentDescription = null, tint = if (enabled) Color(0xFFDADAE6) else Color(0xFF1F1C28))
                },
                text = {
                    Text(text = "Google Pixel Buds Pro")
                },
                onClick = { enabled = !enabled },
                color = if (enabled) Color(0xFF1F1C28) else Color(0xFFDADAE6)
            )
        }
    }
}

@Preview
@Composable
private fun CallActionButtonWithoutTextSmallPreview() {
    VoximplantTheme {
        Surface {
            CallActionButtonSmall(
                icon = {
                    Icon(
                        painter = painterResource(id = Icons.Hangup),
                        contentDescription = null,
                        tint = Color.White,
                    )
                },
                color = Color(0xFFF5222D),
                onClick = {},
            )
        }
    }
}