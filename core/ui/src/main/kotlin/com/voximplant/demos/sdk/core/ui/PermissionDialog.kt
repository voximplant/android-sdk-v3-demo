/*
 * Copyright (c) 2011 - 2023, Zingaya, Inc. All rights reserved.
 */

package com.voximplant.demos.sdk.core.ui

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.voximplant.demos.sdk.core.designsystem.icon.Icons
import com.voximplant.demos.sdk.core.designsystem.theme.VoximplantTheme

@Composable
fun PermissionDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    icon: @Composable (() -> Unit)? = null,
    title: @Composable () -> Unit,
    description: @Composable () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss::invoke,
        confirmButton = {
            Button(
                onClick = onConfirm::invoke,
            ) {
                Text(text = stringResource(id = com.voximplant.demos.sdk.core.resources.R.string.confirm))
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss::invoke,
            ) {
                Text(text = stringResource(id = android.R.string.cancel))
            }
        },
        icon = icon,
        title = title,
        text = description,
    )
}

@Preview
@Composable
private fun PermissionDialogPreview() {
    VoximplantTheme {
        PermissionDialog(
            onDismiss = {},
            onConfirm = {},
            icon = {
                Icon(
                    painterResource(id = Icons.Notification),
                    contentDescription = null,
                )
            },
            title = { Text(text = "Title") },
            description = { Text(text = "Description") },
        )
    }
}
