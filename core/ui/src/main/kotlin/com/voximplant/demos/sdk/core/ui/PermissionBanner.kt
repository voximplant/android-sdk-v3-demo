/*
 * Copyright (c) 2011 - 2023, Zingaya, Inc. All rights reserved.
 */

package com.voximplant.demos.sdk.core.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.voximplant.demos.sdk.core.designsystem.icon.Icons
import com.voximplant.demos.sdk.core.designsystem.theme.Gray10
import com.voximplant.demos.sdk.core.designsystem.theme.Typography
import com.voximplant.demos.sdk.core.designsystem.theme.VoximplantTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PermissionBanner(
    icon: @Composable () -> Unit,
    text: @Composable () -> Unit,
    onRequestClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        onClick = onRequestClick,
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFF2F3F6),
        ),
        shape = RoundedCornerShape(16.dp),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                icon()
                Box(
                    Modifier
                        .padding(
                            start = ButtonDefaults.IconSpacing,
                        ),
                ) {
                    ProvideTextStyle(value = Typography.titleMedium.copy(color = Gray10)) {
                        text()
                    }
                }
            }
        }
    }
}

@Preview
@Composable
private fun PermissionBannerPreview() {
    VoximplantTheme {
        PermissionBanner(
            icon = {
                Icon(painter = painterResource(id = Icons.Notification), contentDescription = null)
            },
            text = {
                Text("Allow permission")
            },
            onRequestClick = {}
        )
    }
}
