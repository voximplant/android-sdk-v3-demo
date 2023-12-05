/*
 * Copyright (c) 2011 - 2023, Zingaya, Inc. All rights reserved.
 */

package com.voximplant.sdk3demo.core.ui

import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.voximplant.sdk3demo.core.designsystem.icon.Icons
import com.voximplant.sdk3demo.core.designsystem.theme.VoximplantTheme

@Composable
fun MicrophoneSettingsDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    SettingsDialog(
        onDismiss = onDismiss,
        onConfirm = onConfirm,
        icon = {
            Icon(
                painterResource(id = Icons.Microphone),
                contentDescription = null,
            )
        },
        title = {
            Text(text = stringResource(id = R.string.permission_record_audio))
        },
        description = {
            Text(text = stringResource(id = R.string.permission_record_audio_description))
        },
    )
}

@Preview
@Composable
private fun MicrophoneSettingsDialogPreview() {
    VoximplantTheme {
        MicrophoneSettingsDialog(
            onDismiss = {},
            onConfirm = {},
        )
    }
}
