/*
 * Copyright (c) 2011 - 2024, Zingaya, Inc. All rights reserved.
 */

package com.voximplant.demos.sdk.feature.audiocall.ongoing

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.filled.Dialpad
import androidx.compose.material3.Icon
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
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.voximplant.demos.sdk.core.designsystem.icon.Icons
import com.voximplant.demos.sdk.core.designsystem.theme.Gray10
import com.voximplant.demos.sdk.core.designsystem.theme.Typography
import com.voximplant.demos.sdk.core.designsystem.theme.VoximplantTheme
import com.voximplant.demos.sdk.core.model.data.AudioDevice
import com.voximplant.demos.sdk.core.model.data.Call
import com.voximplant.demos.sdk.core.model.data.CallDirection
import com.voximplant.demos.sdk.core.model.data.CallState
import com.voximplant.demos.sdk.core.model.data.CallType
import com.voximplant.demos.sdk.core.resources.R
import com.voximplant.demos.sdk.core.ui.AudioDevicesDialog
import com.voximplant.demos.sdk.core.ui.CallActionButton
import com.voximplant.demos.sdk.core.ui.CallFailedDialog
import com.voximplant.demos.sdk.core.ui.Dialpad
import com.voximplant.demos.sdk.core.ui.util.formatDuration

@Composable
fun AudioCallOngoingRoute(
    viewModel: AudioCallOngoingViewModel = hiltViewModel(),
    onCallEnded: () -> Unit,
) {
    val callOngoingUiState by viewModel.callOngoingUiState.collectAsStateWithLifecycle()

    var callFailedDescription: String? by rememberSaveable { mutableStateOf(null) }

    var showAudioDevices by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(callOngoingUiState.call) {
        if (callOngoingUiState is CallOngoingUiState.Failed) {
            callFailedDescription = (callOngoingUiState as CallOngoingUiState.Failed).reason
        } else if (callOngoingUiState.call?.state is CallState.Disconnected) {
            onCallEnded()
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
        AudioDevicesDialog(
            audioDevices = callOngoingUiState.audioDevices,
            selectedAudioDevice = callOngoingUiState.audioDevice,
            onDismissRequest = {
                showAudioDevices = false
            },
            onAudioDeviceClick = { audioDevice ->
                showAudioDevices = false
                viewModel.selectAudioDevice(audioDevice)
            },
        )
    }

    Scaffold { paddingValues ->
        AudioCallOngoingScreen(
            modifier = Modifier.padding(paddingValues),
            callOngoingUiState = callOngoingUiState,
            onMuteClick = viewModel::toggleMute,
            onHoldClick = viewModel::toggleHold,
            onAudioDevicesClick = { showAudioDevices = true },
            onDialKeyClick = viewModel::sendDTMF,
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
    onDialKeyClick: (String) -> Unit,
    onHangUpClick: () -> Unit,
) {
    val duration: Long by remember(callOngoingUiState.call) {
        mutableLongStateOf(callOngoingUiState.call?.duration ?: 0L)
    }

    var showDialpad: Boolean by rememberSaveable { mutableStateOf(false) }

    Surface(
        modifier = modifier.fillMaxSize(),
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(start = 64.dp, top = 64.dp, end = 64.dp)
                    .weight(1f),
                contentAlignment = Alignment.TopCenter,
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
                            val stateText = when (callOngoingUiState) {
                                is CallOngoingUiState.Connecting -> stringResource(R.string.call_state_connecting)
                                is CallOngoingUiState.Failed -> stringResource(R.string.call_state_failed)
                                else -> when (callOngoingUiState.call?.state) {
                                    is CallState.Created, is CallState.Connected -> stringResource(R.string.call_state_connected)
                                    is CallState.Connecting -> stringResource(R.string.call_state_connecting)
                                    is CallState.Disconnected -> stringResource(R.string.call_state_disconnected)
                                    is CallState.Disconnecting -> stringResource(R.string.call_state_disconnecting)
                                    is CallState.Reconnecting -> stringResource(R.string.call_state_reconnecting)
                                    is CallState.Failed, null -> stringResource(R.string.call_state_failed)
                                }
                            }

                            if (callOngoingUiState.call?.state is CallState.Connected && duration != 0L) {
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
                    AnimatedContent(
                        targetState = showDialpad, label = "KeypadAnimation",
                        transitionSpec = {
                            fadeIn(animationSpec = tween(150, 150)) togetherWith fadeOut(animationSpec = tween(150)) using SizeTransform { initialSize, targetSize ->
                                if (targetState) {
                                    keyframes {
                                        IntSize(targetSize.width, initialSize.height) at 150
                                    }
                                } else {
                                    keyframes {
                                        IntSize(initialSize.width, targetSize.height) at 150
                                    }
                                }
                            }
                        },
                    ) { value ->
                        if (!value) {
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
                                        Icon(painter = painterResource(id = icon), contentDescription = null, tint = Color(0xFF1F1C28))
                                    },
                                    text = {
                                        Text(text = callOngoingUiState.audioDevice?.name.orEmpty())
                                    },
                                    onClick = onAudioDevicesClick,
                                    enabled = callOngoingUiState !is CallOngoingUiState.Inactive,
                                )
                            }
                        } else {
                            Dialpad(
                                onKeyClick = onDialKeyClick,
                                modifier = Modifier.fillMaxWidth(),
                                enabled = callOngoingUiState is CallOngoingUiState.Active,
                            )
                        }
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp, vertical = 8.dp), horizontalArrangement = Arrangement.spacedBy(24.dp, Alignment.CenterHorizontally)
                    ) {
                        CallActionButton(
                            icon = {
                                Icon(imageVector = androidx.compose.material.icons.Icons.Default.Dialpad, contentDescription = null, tint = if (showDialpad) Color(0xFFDADAE6) else Color(0xFF1F1C28))
                            },
                            text = {
                                Text(text = "Keypad")
                            },
                            onClick = { showDialpad = showDialpad.not() },
                            enabled = callOngoingUiState !is CallOngoingUiState.Inactive,
                            color = animateColorAsState(if (showDialpad) Color(0xFF1F1C28) else Color(0xFFDADAE6), label = "ActiveAnimation").value,
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

@Preview
@Composable
fun PreviewAudioCallScreen() {
    var isMuted by remember { mutableStateOf(false) }

    val call = Call("", CallState.Connected, direction = CallDirection.OUTGOING, duration = 0L, remoteDisplayName = null, remoteSipUri = null, CallType.AudioCall)

    val callOngoingUiState by remember(isMuted) {
        mutableStateOf(
            CallOngoingUiState.Active(
                displayName = "Display Name",
                isMuted = isMuted,
                isOnHold = false,
                audioDevices = emptyList(),
                audioDevice = AudioDevice(true, id = null, name = "Speaker", type = AudioDevice.Type.SPEAKER),
                call = call,
            )
        )
    }

    VoximplantTheme {
        AudioCallOngoingScreen(
            callOngoingUiState = callOngoingUiState,
            onAudioDevicesClick = {},
            onMuteClick = { isMuted = !isMuted },
            onHoldClick = { },
            onDialKeyClick = {},
            onHangUpClick = {},
        )
    }
}
