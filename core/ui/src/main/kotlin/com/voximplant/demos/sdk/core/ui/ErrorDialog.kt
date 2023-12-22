/*
 * Copyright (c) 2011 - 2023, Zingaya, Inc. All rights reserved.
 */

package com.voximplant.demos.sdk.core.ui

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.window.DialogProperties
import com.voximplant.demos.sdk.core.designsystem.icon.Icons
import com.voximplant.demos.sdk.core.designsystem.theme.VoximplantTheme

@Composable
fun ErrorDialog(
    onConfirm: () -> Unit,
    icon: @Composable (() -> Unit)? = null,
    title: @Composable () -> Unit,
    description: @Composable () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onConfirm::invoke,
        confirmButton = {
            Button(
                onClick = onConfirm::invoke,
            ) {
                Text(text = stringResource(id = com.voximplant.demos.sdk.core.resources.R.string.confirm))
            }
        },
        icon = icon,
        title = title,
        text = description,
        properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false),
    )
}

@Preview
@Composable
private fun ErrorDialogPreview() {
    VoximplantTheme {
        ErrorDialog(
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
