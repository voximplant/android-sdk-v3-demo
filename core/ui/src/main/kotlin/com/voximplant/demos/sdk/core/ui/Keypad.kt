/*
 * Copyright (c) 2011 - 2024, Zingaya, Inc. All rights reserved.
 */

package com.voximplant.demos.sdk.core.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.voximplant.demos.sdk.core.designsystem.theme.Gray10
import com.voximplant.demos.sdk.core.designsystem.theme.Typography
import com.voximplant.demos.sdk.core.designsystem.theme.VoximplantTheme

@Composable
fun Keypad(
    onKeyClick: (String) -> Unit,
    enabled: Boolean = true,
) {
    Column {
        Row {
            KeypadButton(text = { Text(text = "1") }, onClick = { onKeyClick("1") }, enabled = enabled)
            KeypadButton(text = { Text(text = "2") }, onClick = { onKeyClick("2") }, enabled = enabled)
            KeypadButton(text = { Text(text = "3") }, onClick = { onKeyClick("3") }, enabled = enabled)
        }
        Row {
            KeypadButton(text = { Text(text = "4") }, onClick = { onKeyClick("4") }, enabled = enabled)
            KeypadButton(text = { Text(text = "5") }, onClick = { onKeyClick("5") }, enabled = enabled)
            KeypadButton(text = { Text(text = "6") }, onClick = { onKeyClick("6") }, enabled = enabled)
        }
        Row {
            KeypadButton(text = { Text(text = "7") }, onClick = { onKeyClick("7") }, enabled = enabled)
            KeypadButton(text = { Text(text = "8") }, onClick = { onKeyClick("8") }, enabled = enabled)
            KeypadButton(text = { Text(text = "9") }, onClick = { onKeyClick("9") }, enabled = enabled)
        }
        Row {
            KeypadButton(text = { Text(text = "*") }, onClick = { onKeyClick("*") }, enabled = enabled)
            KeypadButton(text = { Text(text = "0") }, onClick = { onKeyClick("0") }, enabled = enabled)
            KeypadButton(text = { Text(text = "#") }, onClick = { onKeyClick("#") }, enabled = enabled)
        }
    }
}

@Composable
private fun KeypadButton(
    text: @Composable () -> Unit,
    onClick: () -> Unit,
    enabled: Boolean = true,
) {
    FilledIconButton(
        onClick = onClick,
        modifier = Modifier
            .padding(horizontal = 8.dp, vertical = 4.dp)
            .size(64.dp),
        enabled = enabled,
        colors = IconButtonDefaults.filledIconButtonColors(containerColor = Color(0xFFDADAE6)),
    ) {
        CompositionLocalProvider(
            LocalTextStyle provides Typography.titleLarge.copy(fontWeight = FontWeight.Bold, textAlign = TextAlign.Center),
            LocalContentColor provides Gray10,
        ) {
            text()
        }
    }
}

@Preview
@Composable
private fun PreviewKeypad() {
    VoximplantTheme {
        Surface {
            Keypad(onKeyClick = {})
        }
    }
}
