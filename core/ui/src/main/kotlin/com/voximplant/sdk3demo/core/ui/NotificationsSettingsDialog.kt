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
fun NotificationsSettingsDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    SettingsDialog(
        onDismiss = onDismiss,
        onConfirm = onConfirm,
        icon = {
            Icon(
                painterResource(id = Icons.Notification),
                contentDescription = null,
            )
        },
        title = {
            Text(text = stringResource(id = R.string.permission_post_notification))
        },
        description = {
            Text(text = stringResource(id = R.string.permission_post_notification_description))
        },
    )
}

@Preview
@Composable
private fun NotificationSettingsDialogPreview() {
    VoximplantTheme {
        NotificationsSettingsDialog(
            onDismiss = {},
            onConfirm = {},
        )
    }
}
