/*
 * Copyright (c) 2011 - 2023, Zingaya, Inc. All rights reserved.
 */

package com.voximplant.demos.sdk.core.ui

import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.voximplant.demos.sdk.core.designsystem.icon.Icons
import com.voximplant.demos.sdk.core.designsystem.theme.VoximplantTheme

@Composable
fun CameraBanner(
    modifier: Modifier = Modifier,
    onRequestClick: () -> Unit,
) {
    PermissionBanner(
        icon = {
            Icon(
                painter = painterResource(id = Icons.Camera),
                contentDescription = null,
            )
        },
        text = {
            Text(
                text = stringResource(R.string.allow_camera),
            )
        },
        onRequestClick = onRequestClick,
        modifier = modifier,
    )
}

@Preview
@Composable
private fun CameraBannerPreview() {
    VoximplantTheme {
        CameraBanner(
            onRequestClick = {}
        )
    }
}
