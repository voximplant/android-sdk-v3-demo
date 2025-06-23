/*
 * Copyright (c) 2011 - 2025, Zingaya, Inc. All rights reserved.
 */

package com.voximplant.demos.sdk.core.ui

import android.content.res.Configuration
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.voximplant.demos.sdk.core.designsystem.icon.Icons
import com.voximplant.demos.sdk.core.designsystem.theme.VoximplantTheme

@Composable
fun LocalVideoRenderer(
    modifier: Modifier = Modifier,
    configuration: Int,
    content: @Composable () -> Unit,
    onSwitchCameraClick: () -> Unit,
    ) {
    var heightLocalVideoLayout = 160
    var widthLocalVideoLayout = 128

    when (configuration) {
        Configuration.ORIENTATION_PORTRAIT -> {
            heightLocalVideoLayout = 160
            widthLocalVideoLayout = 128
        }
        Configuration.ORIENTATION_LANDSCAPE -> {
            heightLocalVideoLayout = 128
            widthLocalVideoLayout = 160
        }
        Configuration.ORIENTATION_UNDEFINED -> {
            heightLocalVideoLayout = 160
            widthLocalVideoLayout = 128
        }
    }

    Card(
        modifier = modifier
            .height(heightLocalVideoLayout.dp)
            .width(widthLocalVideoLayout.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.BottomEnd
        ) {
            content()
            IconButton(
                modifier = Modifier.size(40.dp),
                onClick = onSwitchCameraClick,
            ) {
                Icon(
                    painter = painterResource(Icons.Switch),
                    contentDescription = null,
                    tint = Color.White
                )
            }
        }
    }
}


@Preview(device = "spec:width=411dp,height=891dp,orientation=portrait")
@Preview(device = "spec:width=411dp,height=891dp,orientation=landscape")
@Composable
private fun LocalVideoRendererPreview() {
    VoximplantTheme {
        LocalVideoRenderer(
            modifier = Modifier,
            configuration = LocalConfiguration.current.orientation,
            onSwitchCameraClick = {},
            content = {}
        )
    }
}