/*
 * Copyright (c) 2011 - 2025, Zingaya, Inc. All rights reserved.
 */

package com.voximplant.demos.sdk.core.ui

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.voximplant.demos.sdk.core.designsystem.icon.Icons
import com.voximplant.demos.sdk.core.designsystem.theme.VoximplantTheme

@Composable
fun MicrophoneAndCameraPermissionDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    PermissionDialog(
        onDismiss = onDismiss,
        onConfirm = onConfirm,
        icon = {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painterResource(id = Icons.Microphone),
                    contentDescription = null,
                )
                Text(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    text = "+",
                )
                Icon(
                    painterResource(id = Icons.Camera),
                    contentDescription = null,
                )
            }
        },
        title = {
            Text(text = stringResource(id = R.string.permission_record_audio_and_camera))
        },
        description = {
            Text(text = stringResource(id = R.string.permission_record_audio_and_camera_description))
        },
    )
}

@Preview
@Composable
private fun MicrophonePermissionDialogPreview() {
    VoximplantTheme {
        MicrophoneAndCameraPermissionDialog(
            onDismiss = {},
            onConfirm = {},
        )
    }
}
