/*
 * Copyright (c) 2011 - 2023, Zingaya, Inc. All rights reserved.
 */

package com.voximplant.demos.sdk.core.ui

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.voximplant.demos.sdk.core.designsystem.theme.VoximplantTheme

@Composable
fun CallFailedDialog(
    onConfirm: () -> Unit,
    description: String,
) {
    ErrorDialog(
        onConfirm = onConfirm::invoke,
        title = { Text(text = stringResource(R.string.call_failed)) },
        description = { Text(text = description) },
    )
}

@Preview
@Composable
private fun CallFailedDialogPreview() {
    VoximplantTheme {
        CallFailedDialog(
            onConfirm = {},
            description = "Call failed description",
        )
    }
}
