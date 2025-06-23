/*
 * Copyright (c) 2011 - 2024, Zingaya, Inc. All rights reserved.
 */

package com.voximplant.demos.sdk.core.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.voximplant.demos.sdk.core.designsystem.icon.Icons
import com.voximplant.demos.sdk.core.model.data.AudioDevice
import com.voximplant.demos.sdk.core.resources.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AudioDevicesDialog(
    audioDevices: List<AudioDevice>,
    selectedAudioDevice: AudioDevice?,
    onDismissRequest: () -> Unit,
    onAudioDeviceClick: (AudioDevice) -> Unit,
) {
    BasicAlertDialog(onDismissRequest = onDismissRequest) {
        Card {
            Column {
                Box(Modifier.padding(start = 24.dp, top = 24.dp, end = 24.dp, bottom = 16.dp)) {
                    Text(text = stringResource(R.string.audio_devices))
                }
                LazyColumn(
                    Modifier.weight(1f, fill = false),
                    contentPadding = PaddingValues(bottom = 24.dp)
                ) {
                    items(audioDevices) { audioDevice ->
                        ListItem(
                            headlineContent = {
                                Text(text = audioDevice.name)
                            },
                            modifier = Modifier.clickable {
                                onAudioDeviceClick(audioDevice)
                            },
                            leadingContent = {
                                val icon = when (audioDevice.type) {
                                    AudioDevice.Type.EARPIECE -> Icons.Earpiece
                                    AudioDevice.Type.SPEAKER -> Icons.Speaker
                                    AudioDevice.Type.WIRED_HEADSET -> Icons.WiredHeadset
                                    AudioDevice.Type.BLUETOOTH -> Icons.Bluetooth
                                    AudioDevice.Type.USB -> Icons.Usb
                                    AudioDevice.Type.UNKNOWN -> Icons.AudioOff
                                }
                                Icon(
                                    painter = painterResource(id = icon),
                                    contentDescription = null
                                )
                            },
                            trailingContent = {
                                if (selectedAudioDevice == audioDevice) {
                                    Icon(
                                        painter = painterResource(id = Icons.Check),
                                        contentDescription = null
                                    )
                                }
                            },
                            colors = ListItemDefaults.colors(MaterialTheme.colorScheme.surfaceVariant),
                        )
                    }
                }
            }
        }
    }
}

@Preview
@Composable
private fun AudioDevicesDialogPreview() {
    val audioDevices = listOf(
        AudioDevice(
            hasMic = false,
            id = 0,
            name = "Speaker",
            type = AudioDevice.Type.SPEAKER
        ),
        AudioDevice(
            hasMic = false,
            id = 0,
            name = "Earpiece",
            type = AudioDevice.Type.EARPIECE
        ),
        AudioDevice(
            hasMic = false,
            id = 0,
            name = "OnePlus Buds 3",
            type = AudioDevice.Type.BLUETOOTH
        ),
        AudioDevice(
            hasMic = false,
            id = 0,
            name = "Wired headset",
            type = AudioDevice.Type.WIRED_HEADSET
        ),
    )

    AudioDevicesDialog(
        audioDevices = audioDevices,
        selectedAudioDevice = audioDevices[0],
        onDismissRequest = {},
        onAudioDeviceClick = {},
    )
}