/*
 * Copyright (c) 2011 - 2024, Zingaya, Inc. All rights reserved.
 */

package com.voximplant.demos.sdk.core.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.voximplant.demos.sdk.core.designsystem.icon.Icons
import com.voximplant.demos.sdk.core.designsystem.theme.Gray15
import com.voximplant.demos.sdk.core.designsystem.theme.Gray25
import com.voximplant.demos.sdk.core.designsystem.theme.Gray5
import com.voximplant.demos.sdk.core.designsystem.theme.Gray80
import com.voximplant.demos.sdk.core.designsystem.theme.Red40
import com.voximplant.demos.sdk.core.designsystem.theme.VoximplantTheme
import com.voximplant.demos.sdk.core.model.data.AudioDevice
import com.voximplant.demos.sdk.core.model.data.CallState
import com.voximplant.demos.sdk.core.resources.R
import com.voximplant.demos.sdk.core.ui.util.formatDuration

@Composable
fun VideoCallControlPanel(
    modifier: Modifier = Modifier,
    callState: CallState?,
    remoteDisplayName: String,
    duration: Long,
    muteEnabled: Boolean,
    cameraEnabled: Boolean,
    activeAudioDevice: AudioDevice?,
    microphoneButtonEnabled: Boolean,
    cameraButtonEnabled: Boolean,
    speakerButtonEnabled: Boolean,
    onHangUpClick: () -> Unit,
    onMicrophoneClick: () -> Unit,
    onCameraClick: () -> Unit,
    onSpeakerClick: () -> Unit,
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors().copy(containerColor = Gray15),
    ) {
        Row(
            modifier = Modifier.padding(top = 12.dp, start = 12.dp, end = 4.dp, bottom = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Gray25)
                    .padding(8.dp),
                painter = painterResource(Icons.Person),
                tint = Color.White,
                contentDescription = null,
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center,
            ) {
                Text(
                    text = remoteDisplayName,
                    fontSize = 16.sp,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )

                val stateText = when (callState) {
                    is CallState.Created, is CallState.Connected -> stringResource(R.string.call_state_connected)
                    is CallState.Connecting -> stringResource(R.string.call_state_connecting)
                    is CallState.Disconnected -> stringResource(R.string.call_state_disconnected)
                    is CallState.Disconnecting -> stringResource(R.string.call_state_disconnecting)
                    is CallState.Reconnecting -> stringResource(R.string.call_state_reconnecting)
                    is CallState.Failed, null -> stringResource(R.string.call_state_failed)
                }

                if (callState is CallState.Connected && duration != 0L) {
                    Text(text = formatDuration(duration), fontSize = 12.sp, color = Gray80)
                } else {
                    Text(text = stateText, fontSize = 12.sp, color = Gray80)
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            CallActionButtonSmall(
                icon = {
                    Icon(
                        painter = painterResource(Icons.Hangup),
                        tint = Color.White,
                        contentDescription = null,
                    )
                },
                color = Red40,
                enabled = true,
                onClick = onHangUpClick,
            )
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 4.dp),
            horizontalArrangement = Arrangement.Center,
        ) {
            CallActionButtonSmall(
                icon = {
                    Icon(
                        painter = painterResource(Icons.MicrophoneMuted),
                        tint = if (muteEnabled) Color.Black else Color.White,
                        contentDescription = null
                    )
                },
                color = if (muteEnabled) Color.White else Gray5,
                enabled = microphoneButtonEnabled,
                onClick = onMicrophoneClick,
            )
            CallActionButtonSmall(
                icon = {
                    Icon(
                        painter = painterResource(Icons.CameraOff),
                        tint = if (cameraEnabled || callState is CallState.Disconnected) Color.White else Color.Black,
                        contentDescription = null
                    )
                },
                color = if (cameraEnabled) Gray5 else Color.White,
                enabled = cameraButtonEnabled,
                onClick = onCameraClick,
            )
            CallActionButtonSmall(
                icon = {
                    val icon = when (activeAudioDevice?.type) {
                        AudioDevice.Type.EARPIECE -> Icons.Earpiece
                        AudioDevice.Type.SPEAKER -> Icons.Speaker
                        AudioDevice.Type.WIRED_HEADSET -> Icons.WiredHeadset
                        AudioDevice.Type.BLUETOOTH -> Icons.Bluetooth
                        AudioDevice.Type.USB -> Icons.Usb
                        AudioDevice.Type.UNKNOWN, null -> Icons.AudioOff
                    }
                    Icon(
                        painter = painterResource(icon),
                        tint = Color.White,
                        contentDescription = null
                    )
                },
                color = Gray5,
                enabled = speakerButtonEnabled,
                onClick = onSpeakerClick,
            )
        }
    }
}

@Preview
@Composable
private fun VideoCallControlPanelPreview() {
    VoximplantTheme {
        VideoCallControlPanel(
            modifier = Modifier,
            callState = CallState.Connected,
            remoteDisplayName = "Operator",
            duration = 754000,
            onHangUpClick = {},
            onMicrophoneClick = {},
            onCameraClick = {},
            onSpeakerClick = {},
            cameraEnabled = true,
            muteEnabled = false,
            microphoneButtonEnabled = true,
            cameraButtonEnabled = true,
            speakerButtonEnabled = true,
            activeAudioDevice = AudioDevice(
                hasMic = false,
                id = 0,
                name = "Speaker",
                type = AudioDevice.Type.SPEAKER
            )
        )
    }
}

@Preview
@Composable
private fun VideoCallControlPanelMuteEnabledPreview() {
    VoximplantTheme {
        VideoCallControlPanel(
            modifier = Modifier,
            remoteDisplayName = "Operator",
            callState = CallState.Connected,
            duration = 754000,
            onHangUpClick = {},
            onMicrophoneClick = {},
            onCameraClick = {},
            onSpeakerClick = {},
            cameraEnabled = true,
            muteEnabled = true,
            microphoneButtonEnabled = true,
            cameraButtonEnabled = true,
            speakerButtonEnabled = true,
            activeAudioDevice = AudioDevice(
                hasMic = false,
                id = 0,
                name = "Speaker",
                type = AudioDevice.Type.SPEAKER
            )
        )
    }
}

@Preview
@Composable
private fun VideoCallControlPanelCameraOffPreview() {
    VoximplantTheme {
        VideoCallControlPanel(
            modifier = Modifier,
            remoteDisplayName = "Operator",
            callState = CallState.Connected,
            duration = 754000,
            onHangUpClick = {},
            onMicrophoneClick = {},
            onCameraClick = {},
            onSpeakerClick = {},
            cameraEnabled = false,
            muteEnabled = false,
            microphoneButtonEnabled = true,
            cameraButtonEnabled = true,
            speakerButtonEnabled = true,
            activeAudioDevice = AudioDevice(
                hasMic = false,
                id = 0,
                name = "Speaker",
                type = AudioDevice.Type.SPEAKER
            )
        )
    }
}

@Preview
@Composable
private fun VideoCallControlPanelLongDisplayNamePreview() {
    VoximplantTheme {
        VideoCallControlPanel(
            modifier = Modifier,
            remoteDisplayName = "Operator Operator Operator Operator Operator Operator",
            callState = CallState.Connected,
            duration = 754000,
            onHangUpClick = {},
            onMicrophoneClick = {},
            onCameraClick = {},
            onSpeakerClick = {},
            cameraEnabled = true,
            muteEnabled = false,
            microphoneButtonEnabled = true,
            cameraButtonEnabled = true,
            speakerButtonEnabled = true,
            activeAudioDevice = AudioDevice(
                hasMic = false,
                id = 0,
                name = "Speaker",
                type = AudioDevice.Type.SPEAKER
            )
        )
    }
}

@Preview
@Composable
private fun VideoCallControlPanelAudioDeviceBluetoothPreview() {
    VoximplantTheme {
        VideoCallControlPanel(
            modifier = Modifier,
            remoteDisplayName = "Operator",
            callState = CallState.Connected,
            duration = 754000,
            onHangUpClick = {},
            onMicrophoneClick = {},
            onCameraClick = {},
            onSpeakerClick = {},
            cameraEnabled = true,
            muteEnabled = false,
            microphoneButtonEnabled = true,
            cameraButtonEnabled = true,
            speakerButtonEnabled = true,
            activeAudioDevice = AudioDevice(
                hasMic = true,
                id = 0,
                name = "Bluetooth",
                type = AudioDevice.Type.BLUETOOTH
            )
        )
    }
}

@Preview
@Composable
private fun VideoCallControlPanelCallConnectingPreview() {
    VoximplantTheme {
        VideoCallControlPanel(
            modifier = Modifier,
            callState = CallState.Connecting,
            remoteDisplayName = "Operator",
            duration = 754000,
            onHangUpClick = {},
            onMicrophoneClick = {},
            onCameraClick = {},
            onSpeakerClick = {},
            cameraEnabled = true,
            muteEnabled = false,
            microphoneButtonEnabled = true,
            cameraButtonEnabled = true,
            speakerButtonEnabled = true,
            activeAudioDevice = AudioDevice(
                hasMic = false,
                id = 0,
                name = "Speaker",
                type = AudioDevice.Type.SPEAKER
            )
        )
    }
}

@Preview
@Composable
private fun VideoCallControlPanelCallDisconnectingPreview() {
    VoximplantTheme {
        VideoCallControlPanel(
            modifier = Modifier,
            callState = CallState.Disconnecting,
            remoteDisplayName = "Operator",
            duration = 754000,
            onHangUpClick = {},
            onMicrophoneClick = {},
            onCameraClick = {},
            onSpeakerClick = {},
            cameraEnabled = true,
            muteEnabled = false,
            microphoneButtonEnabled = true,
            cameraButtonEnabled = true,
            speakerButtonEnabled = true,
            activeAudioDevice = AudioDevice(
                hasMic = false,
                id = 0,
                name = "Speaker",
                type = AudioDevice.Type.SPEAKER
            )
        )
    }
}

@Preview
@Composable
private fun VideoCallControlPanelCallDisconnectedPreview() {
    VoximplantTheme {
        VideoCallControlPanel(
            modifier = Modifier,
            callState = CallState.Disconnected,
            remoteDisplayName = "Operator",
            duration = 754000,
            onHangUpClick = {},
            onMicrophoneClick = {},
            onCameraClick = {},
            onSpeakerClick = {},
            cameraEnabled = true,
            muteEnabled = false,
            microphoneButtonEnabled = true,
            cameraButtonEnabled = true,
            speakerButtonEnabled = true,
            activeAudioDevice = AudioDevice(
                hasMic = false,
                id = 0,
                name = "Speaker",
                type = AudioDevice.Type.SPEAKER
            )
        )
    }
}

@Preview
@Composable
private fun VideoCallControlPanelCallReconnectingPreview() {
    VoximplantTheme {
        VideoCallControlPanel(
            modifier = Modifier,
            callState = CallState.Reconnecting,
            remoteDisplayName = "Operator",
            duration = 754000,
            onHangUpClick = {},
            onMicrophoneClick = {},
            onCameraClick = {},
            onSpeakerClick = {},
            cameraEnabled = true,
            muteEnabled = false,
            microphoneButtonEnabled = true,
            cameraButtonEnabled = true,
            speakerButtonEnabled = true,
            activeAudioDevice = AudioDevice(
                hasMic = false,
                id = 0,
                name = "Speaker",
                type = AudioDevice.Type.SPEAKER
            )
        )
    }
}

@Preview
@Composable
private fun VideoCallControlPanelCallFailedPreview() {
    VoximplantTheme {
        VideoCallControlPanel(
            modifier = Modifier,
            callState = CallState.Failed(""),
            remoteDisplayName = "Operator",
            duration = 754000,
            onHangUpClick = {},
            onMicrophoneClick = {},
            onCameraClick = {},
            onSpeakerClick = {},
            cameraEnabled = true,
            muteEnabled = false,
            microphoneButtonEnabled = true,
            cameraButtonEnabled = true,
            speakerButtonEnabled = true,
            activeAudioDevice = AudioDevice(
                hasMic = false,
                id = 0,
                name = "Speaker",
                type = AudioDevice.Type.SPEAKER
            )
        )
    }
}
