/*
 * Copyright (c) 2011 - 2023, Zingaya, Inc. All rights reserved.
 */

package com.voximplant.demos.sdk.core.ui

import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.voximplant.demos.sdk.core.designsystem.icon.Icons
import com.voximplant.demos.sdk.core.designsystem.theme.VoximplantTheme

@Composable
fun CameraSettingsDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    SettingsDialog(
        onDismiss = onDismiss,
        onConfirm = onConfirm,
        icon = {
            Icon(
                painterResource(id = Icons.Camera),
                contentDescription = null,
            )
        },
        title = {
            Text(text = stringResource(id = R.string.permission_camera))
        },
        description = {
            Text(text = stringResource(id = R.string.permission_camera_description))
        },
    )
}

@Preview
@Composable
private fun CameraSettingsDialogPreview() {
    VoximplantTheme {
        CameraSettingsDialog(
            onDismiss = {},
            onConfirm = {},
        )
    }
}
