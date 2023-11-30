/*
 * Copyright (c) 2011 - 2023, Zingaya, Inc. All rights reserved.
 */

package com.voximplant.sdk3demo.core.ui

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.voximplant.sdk3demo.core.designsystem.icon.Icons
import com.voximplant.sdk3demo.core.designsystem.theme.VoximplantTheme

@Composable
fun PermissionDialog(
    title: @Composable () -> Unit,
    description: @Composable () -> Unit,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss::invoke,
        confirmButton = {
            Button(
                onClick = onConfirm::invoke,
            ) {
                Text(text = stringResource(id = com.voximplant.sdk3demo.core.resource.R.string.confirm))
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss::invoke,
            ) {
                Text(text = stringResource(id = android.R.string.cancel))
            }
        },
        icon = {
            Icon(
                painterResource(id = Icons.Notification),
                contentDescription = null,
            )
        },
        title = title,
        text = description,
    )
}

@Preview
@Composable
private fun PreviewStopRecordingDialog() {
    VoximplantTheme {
        PermissionDialog(
            title = { Text(text = "Title") },
            description = { Text(text = "Description") },
            onDismiss = {},
            onConfirm = {},
        )
    }
}
