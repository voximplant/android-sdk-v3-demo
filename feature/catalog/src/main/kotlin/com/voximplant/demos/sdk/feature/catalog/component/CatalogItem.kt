/*
 * Copyright (c) 2011 - 2023, Zingaya, Inc. All rights reserved.
 */

package com.voximplant.demos.sdk.feature.catalog.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.voximplant.demos.sdk.core.designsystem.icon.Icons
import com.voximplant.demos.sdk.core.designsystem.theme.Gray10
import com.voximplant.demos.sdk.core.designsystem.theme.Typography
import com.voximplant.demos.sdk.core.designsystem.theme.VoximplantTheme

@Composable
fun CatalogItem(
    title: String,
    description: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    image: Painter? = null,
) {
    Surface(
        onClick = { onClick() },
        modifier = modifier.requiredHeight(120.dp),
        color = Color(0xFFF2F3F6),
        shape = RoundedCornerShape(16.dp),
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                modifier = Modifier
                    .weight(1f),
                verticalArrangement = Arrangement.Center,
            ) {
                Text(
                    text = title,
                    color = Gray10,
                    style = Typography.titleMedium,
                )
                if (description != null) {
                    Text(
                        text = description,
                        color = Gray10,
                        style = Typography.bodyMedium,
                    )
                }
            }
            if (image != null) {
                Image(painter = image, contentDescription = null)
            }
        }
    }
}

@Preview
@Composable
private fun CatalogItemPreview() {
    VoximplantTheme {
        CatalogItem(
            title = "Title",
            description = "Description",
            onClick = {},
            image = painterResource(id = Icons.Call)
        )
    }
}

@Preview
@Composable
private fun CatalogItemTitlePreview() {
    VoximplantTheme {
        CatalogItem(
            title = "Title",
            description = null,
            onClick = {},
            image = painterResource(id = Icons.Call)
        )
    }
}

@Preview
@Composable
private fun CatalogItemLongTitlePreview() {
    VoximplantTheme {
        CatalogItem(
            title = "Very very very very very very very very very long title",
            description = "Description",
            onClick = {},
            image = painterResource(id = Icons.Call)
        )
    }
}

@Preview
@Composable
private fun CatalogItemLongDescriptionPreview() {
    VoximplantTheme {
        CatalogItem(
            title = "Title",
            description = "Very very very very very very very very very long description",
            onClick = {},
            image = painterResource(id = Icons.Call)
        )
    }
}
