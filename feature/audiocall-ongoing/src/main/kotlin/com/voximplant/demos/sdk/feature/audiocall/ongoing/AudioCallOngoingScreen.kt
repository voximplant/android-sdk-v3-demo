/*
 * Copyright (c) 2011 - 2023, Zingaya, Inc. All rights reserved.
 */

package com.voximplant.demos.sdk.feature.audiocall.ongoing

import androidx.activity.compose.BackHandler
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.voximplant.demos.sdk.core.designsystem.icon.Icons
import com.voximplant.demos.sdk.core.designsystem.theme.Gray10
import com.voximplant.demos.sdk.core.designsystem.theme.Typography
import com.voximplant.demos.sdk.core.designsystem.theme.VoximplantTheme
import com.voximplant.demos.sdk.core.model.data.AudioDevice
import com.voximplant.demos.sdk.core.model.data.CallState
import com.voximplant.demos.sdk.core.resources.R
import com.voximplant.demos.sdk.core.ui.CallActionButton
import com.voximplant.demos.sdk.core.ui.CallFailedDialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AudioCallOngoingRoute(
    viewModel: AudioCallOngoingViewModel = hiltViewModel(),
    onCallEnded: () -> Unit,
) {
    val callOngoingUiState by viewModel.callOngoingUiState.collectAsStateWithLifecycle()

    var callFailedDescription: String? by rememberSaveable { mutableStateOf(null) }

    var showAudioDevices by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(callOngoingUiState) {
        when (callOngoingUiState.state) {
            is CallState.Disconnected -> onCallEnded()
            is CallState.Failed -> callFailedDescription = (callOngoingUiState.state as CallState.Failed).description
            else -> {}
        }
    }

    BackHandler {}

    callFailedDescription?.let { description ->
        CallFailedDialog(
            onConfirm = {
                callFailedDescription = null
                onCallEnded()
            },
            description = description,
        )
    }

    if (showAudioDevices) {
        AlertDialog(onDismissRequest = { showAudioDevices = false }) {
            Card {
                Column {
                    Box(Modifier.padding(start = 24.dp, top = 24.dp, end = 24.dp, bottom = 16.dp)) {
                        Text(text = stringResource(R.string.audio_devices))
                    }
                    LazyColumn(
                        Modifier.weight(1f, fill = false), contentPadding = PaddingValues(bottom = 24.dp)
                    ) {
                        items(callOngoingUiState.audioDevices) { audioDevice ->
                            ListItem(
                                headlineContent = {
                                    Text(text = audioDevice.name)
                                },
                                modifier = Modifier.clickable {
                                    showAudioDevices = false
                                    viewModel.selectAudioDevice(audioDevice)
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
                                    Icon(painter = painterResource(id = icon), contentDescription = null)
                                },
                                trailingContent = {
                                    if (callOngoingUiState.audioDevice == audioDevice) {
                                        Icon(painter = painterResource(id = Icons.Check), contentDescription = null)
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

    Scaffold { paddingValues ->
        AudioCallOngoingScreen(
            modifier = Modifier.padding(paddingValues),
            callOngoingUiState = callOngoingUiState,
            onMuteClick = viewModel::toggleMute,
            onHoldClick = viewModel::toggleHold,
            onAudioDevicesClick = { showAudioDevices = true },
            onHangUpClick = viewModel::hangUp,
        )
    }
}

@Composable
fun AudioCallOngoingScreen(
    modifier: Modifier = Modifier,
    callOngoingUiState: CallOngoingUiState,
    onAudioDevicesClick: () -> Unit,
    onMuteClick: () -> Unit,
    onHoldClick: () -> Unit,
    onHangUpClick: () -> Unit,
) {
    val duration: Long by remember(callOngoingUiState.call) {
        mutableLongStateOf(callOngoingUiState.call?.duration ?: 0L)
    }

    Surface(
        modifier = modifier.fillMaxSize(),
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(64.dp)
                    .weight(1f), contentAlignment = Alignment.TopCenter
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Surface(
                        modifier = Modifier.size(96.dp),
                        shape = CircleShape,
                        color = Color(0xFFF0F0F0),
                    ) {
                        Box(
                            modifier = Modifier.size(96.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                painter = painterResource(id = Icons.Person),
                                contentDescription = null,
                                modifier = Modifier.size(36.dp),
                            )
                        }
                    }
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        ProvideTextStyle(
                            value = Typography.titleMedium.copy(
                                color = Gray10,
                                fontWeight = FontWeight.SemiBold,
                            ),
                        ) {
                            Text(text = callOngoingUiState.call?.remoteDisplayName ?: callOngoingUiState.displayName ?: stringResource(R.string.unknown_user))
                        }
                        ProvideTextStyle(value = Typography.bodySmall.copy(color = Gray10)) {
                            val stateText = when (callOngoingUiState.state) {
                                is CallState.Created, is CallState.Connected -> stringResource(R.string.call_state_connected)
                                is CallState.Connecting -> stringResource(R.string.call_state_connecting)
                                is CallState.Disconnected -> stringResource(R.string.call_state_disconnected)
                                is CallState.Disconnecting -> stringResource(R.string.call_state_disconnecting)
                                is CallState.Failed -> stringResource(R.string.call_state_failed)
                                is CallState.Reconnecting -> stringResource(R.string.call_state_reconnecting)
                            }

                            if (callOngoingUiState.state is CallState.Connected && duration != 0L) {
                                Text(text = formatDuration(duration))
                            } else {
                                Text(text = stateText)
                            }
                        }
                    }
                }
            }
            Box(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp, vertical = 8.dp), horizontalArrangement = Arrangement.spacedBy(24.dp, Alignment.CenterHorizontally)
                    ) {
                        CallActionButton(
                            icon = {
                                if (callOngoingUiState.isMuted) {
                                    Icon(painter = painterResource(id = Icons.MicrophoneMuted), contentDescription = null, tint = Color(0xFFDADAE6))
                                } else {
                                    Icon(painter = painterResource(id = Icons.Microphone), contentDescription = null, tint = Color(0xFF1F1C28))
                                }
                            },
                            text = {
                                Text(text = "Mute")
                            },
                            onClick = onMuteClick,
                            enabled = callOngoingUiState !is CallOngoingUiState.Inactive,
                            color = animateColorAsState(if (callOngoingUiState.isMuted) Color(0xFF1F1C28) else Color(0xFFDADAE6), label = "ActiveAnimation").value,
                        )
                        CallActionButton(
                            icon = {
                                Icon(painter = painterResource(id = Icons.Hold), contentDescription = null, tint = if (callOngoingUiState.isOnHold) Color(0xFFDADAE6) else Color(0xFF1F1C28))
                            },
                            text = {
                                Text(text = "Hold")
                            },
                            onClick = onHoldClick,
                            enabled = callOngoingUiState is CallOngoingUiState.Active,
                            color = animateColorAsState(if (callOngoingUiState.isOnHold) Color(0xFF1F1C28) else Color(0xFFDADAE6), label = "ActiveAnimation").value,
                        )
                        CallActionButton(
                            icon = {
                                val icon = when (callOngoingUiState.audioDevice?.type) {
                                    AudioDevice.Type.EARPIECE -> Icons.Earpiece
                                    AudioDevice.Type.SPEAKER -> Icons.Speaker
                                    AudioDevice.Type.WIRED_HEADSET -> Icons.WiredHeadset
                                    AudioDevice.Type.BLUETOOTH -> Icons.Bluetooth
                                    AudioDevice.Type.USB -> Icons.Usb
                                    AudioDevice.Type.UNKNOWN, null -> Icons.AudioOff
                                }
                                Icon(painter = painterResource(id = icon), contentDescription = null)
                            },
                            text = {
                                Text(text = callOngoingUiState.audioDevice?.name.orEmpty())
                            },
                            onClick = onAudioDevicesClick,
                            enabled = callOngoingUiState !is CallOngoingUiState.Inactive,
                        )
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(24.dp, Alignment.CenterHorizontally),
                    ) {
                        CallActionButton(
                            icon = {
                                Icon(
                                    painter = painterResource(id = Icons.Hangup),
                                    contentDescription = null,
                                    tint = Color.White,
                                )
                            },
                            color = Color(0xFFF5222D),
                            onClick = onHangUpClick,
                        )
                    }
                }
            }
        }
    }
}

private fun formatDuration(durationInMillis: Long): String {
    val minutes = (durationInMillis / 1000) / 60
    val seconds = (durationInMillis / 1000) % 60
    return String.format("%02d:%02d", minutes, seconds)
}

@Preview
@Composable
fun PreviewAudioCallScreen() {
    var isMuted by remember { mutableStateOf(false) }

    val callOngoingUiState by remember(isMuted) {
        mutableStateOf(
            CallOngoingUiState.Inactive(
                state = CallState.Connecting,
                displayName = "Display Name",
                isMuted = isMuted,
                audioDevices = emptyList(),
                audioDevice = AudioDevice(true, id = null, name = "Speaker", type = AudioDevice.Type.SPEAKER),
                call = null,
            )
        )
    }

    VoximplantTheme {
        AudioCallOngoingScreen(
            callOngoingUiState = callOngoingUiState,
            onAudioDevicesClick = {},
            onMuteClick = { isMuted = !isMuted },
            onHoldClick = { },
            onHangUpClick = {},
        )
    }
}
