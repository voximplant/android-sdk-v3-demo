/*
 * Copyright (c) 2011 - 2023, Zingaya, Inc. All rights reserved.
 */

package com.voximplant.demos.sdk.feature.catalog.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.voximplant.demos.sdk.core.designsystem.icon.Icons
import com.voximplant.demos.sdk.core.designsystem.theme.Gray10
import com.voximplant.demos.sdk.core.designsystem.theme.Typography
import com.voximplant.demos.sdk.core.designsystem.theme.VoximplantTheme
import com.voximplant.demos.sdk.core.model.data.User
import com.voximplant.demos.sdk.core.model.data.isNotEmpty
import com.voximplant.demos.sdk.feature.catalog.R

@Composable
fun UserBanner(
    user: User,
    modifier: Modifier = Modifier,
    onLogoutClick: () -> Unit,
    onLoginClick: () -> Unit,
) {
    Surface(
        modifier = modifier,
        color = Color(0xFFF2F3F6),
        shape = RoundedCornerShape(16.dp),
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                painter = painterResource(id = Icons.Person),
                contentDescription = null,
            )
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center,
            ) {
                if (user.isNotEmpty()) {
                    Text(
                        text = user.displayName,
                        color = Gray10,
                        style = Typography.titleMedium,
                    )
                    Text(
                        text = user.username,
                        color = Gray10,
                        style = Typography.bodyMedium,
                    )
                }
            }
            if (user.isNotEmpty()) {
                Button(onClick = onLogoutClick) {
                    Text(text = stringResource(R.string.log_out))
                }
            } else {
                Button(onClick = onLoginClick) {
                    Text(text = stringResource(com.voximplant.demos.sdk.core.resources.R.string.log_in))
                }
            }
        }
    }
}

@Preview
@Composable
fun LoggedUserBannerPreview() {
    VoximplantTheme {
        UserBanner(
            user = User("username", "Display Name"),
            onLogoutClick = {},
            onLoginClick = {},
        )
    }
}

@Preview
@Composable
fun UserBannerPreview() {
    VoximplantTheme {
        UserBanner(
            user = User("", ""),
            onLogoutClick = {},
            onLoginClick = {},
        )
    }
}
