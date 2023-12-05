package com.voximplant.sdk3demo.core.ui

import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.voximplant.sdk3demo.core.designsystem.icon.Icons
import com.voximplant.sdk3demo.core.designsystem.theme.VoximplantTheme

@Composable
fun NotificationsBanner(
    modifier: Modifier = Modifier,
    onRequestClick: () -> Unit,
) {
    PermissionBanner(
        icon = {
            Icon(
                painter = painterResource(id = Icons.Notification),
                contentDescription = null,
            )
        },
        text = {
            Text(
                text = stringResource(R.string.allow_notifications),
            )
        },
        onRequestClick = onRequestClick,
        modifier = modifier,
    )
}

@Preview
@Composable
private fun NotificationsBannerPreview() {
    VoximplantTheme {
        NotificationsBanner(
            onRequestClick = {}
        )
    }
}
