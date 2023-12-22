/*
 * Copyright (c) 2011 - 2023, Zingaya, Inc. All rights reserved.
 */

package com.voximplant.demos.sdk.core.ui

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.voximplant.demos.sdk.core.designsystem.theme.VoximplantTheme

@Composable
fun CallFailedDialog(
    onConfirm: () -> Unit,
) {
    ErrorDialog(
        onConfirm = onConfirm::invoke,
        title = { Text(text = "Call failed") },
        description = { Text(text = "Call failed description") },
    )
}

@Preview
@Composable
private fun CallFailedDialogPreview() {
    VoximplantTheme {
        CallFailedDialog(
            onConfirm = {},
        )
    }
}
