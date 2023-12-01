package com.voximplant.sdk3demo.feature.catalog.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.voximplant.sdk3demo.core.designsystem.icon.Icons
import com.voximplant.sdk3demo.core.designsystem.theme.Gray10
import com.voximplant.sdk3demo.core.designsystem.theme.Typography
import com.voximplant.sdk3demo.core.designsystem.theme.VoximplantTheme
import com.voximplant.sdk3demo.feature.catalog.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationBanner(
    modifier: Modifier = Modifier,
    onRequestClick: () -> Unit,
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
                Icon(
                    painter = painterResource(id = Icons.Notification),
                    contentDescription = null,
                )
                Text(
                    text = stringResource(R.string.receive_notifications),
                    color = Gray10,
                    style = Typography.titleMedium,
                )
            }
        }
    }
}

@Preview
@Composable
fun NotificationBannerPreview() {
    VoximplantTheme {
        NotificationBanner(
            onRequestClick = {}
        )
    }
}
