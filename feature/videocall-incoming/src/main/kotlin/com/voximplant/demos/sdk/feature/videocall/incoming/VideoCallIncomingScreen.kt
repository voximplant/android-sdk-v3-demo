/*
 * Copyright (c) 2011 - 2024, Zingaya, Inc. All rights reserved.
 */

package com.voximplant.demos.sdk.feature.videocall.incoming

import android.app.Activity
import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
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
import com.voximplant.demos.sdk.core.model.data.CallState
import com.voximplant.demos.sdk.core.permissions.CameraPermissionEffect
import com.voximplant.demos.sdk.core.permissions.MicrophonePermissionEffect
import com.voximplant.demos.sdk.core.resources.R
import com.voximplant.demos.sdk.core.ui.CallActionButton
import com.voximplant.demos.sdk.core.ui.CallFailedDialog

@Composable
fun VideoCallIncomingRoute(
    viewModel: VideoCallIncomingViewModel = hiltViewModel(),
    onCallEnded: () -> Unit,
    onCallAnswered: (String, String?) -> Unit,
) {
    val context = LocalContext.current
    val action = (context as Activity).intent.action

    val videoCallIncomingUiState by viewModel.callIncomingUiState.collectAsStateWithLifecycle()

    var microphonePermissionGranted by rememberSaveable { mutableStateOf(false) }
    var showMicrophoneRationale by rememberSaveable { mutableStateOf(false) }
    var cameraPermissionGranted by rememberSaveable { mutableStateOf(false) }
    var showCameraRationale by rememberSaveable { mutableStateOf(false) }

    var callFailedDescription: String? by rememberSaveable { mutableStateOf(null) }

    LaunchedEffect(videoCallIncomingUiState) {
        when (val state = videoCallIncomingUiState.call?.state) {
            is CallState.Connecting, is CallState.Connected -> onCallAnswered(viewModel.id, videoCallIncomingUiState.displayName)
            is CallState.Disconnecting, is CallState.Disconnected -> onCallEnded()
            is CallState.Failed -> callFailedDescription = state.description
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

    Scaffold { paddingValues ->
        VideoCallIncomingScreen(
            modifier = Modifier.padding(paddingValues),
            videoCallIncomingUiState = videoCallIncomingUiState,
            onRejectClick = {
                viewModel.reject()
                onCallEnded()
            },
            onAnswerClick = {
                if (microphonePermissionGranted || cameraPermissionGranted) {
                    onCallAnswered(viewModel.id, videoCallIncomingUiState.displayName)
                } else {
                    showMicrophoneRationale = true
                    showCameraRationale = true
                }
            },
        )
    }

    MicrophonePermissionEffect(
        showRationale = showMicrophoneRationale,
        onPermissionGranted = { value ->
            microphonePermissionGranted = value
            showMicrophoneRationale = false
        },
    )

    CameraPermissionEffect(
        showRationale = showCameraRationale,
        onPermissionGranted = { value ->
            cameraPermissionGranted = value
            showCameraRationale = false
        }
    )

    LaunchedEffect(Unit) {
        context.intent.action = null
        if (action == Intent.ACTION_ANSWER) {
            if (microphonePermissionGranted || cameraPermissionGranted) {
                onCallAnswered(viewModel.id, videoCallIncomingUiState.displayName)
            } else {
                showMicrophoneRationale = true
                showCameraRationale = true
            }
        }
    }
}

@Composable
fun VideoCallIncomingScreen(
    modifier: Modifier = Modifier,
    videoCallIncomingUiState: VideoCallIncomingUiState,
    onRejectClick: () -> Unit,
    onAnswerClick: () -> Unit,
) {
    Surface(
        modifier = modifier.fillMaxSize(),
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp),
                contentAlignment = Alignment.Center,
            ) {
                ProvideTextStyle(value = Typography.titleLarge.copy(color = Gray10)) {
                    Text(text = stringResource(R.string.incoming_video_call))
                }
            }
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 64.dp)
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
                            Text(text = videoCallIncomingUiState.displayName ?: stringResource(R.string.unknown_user))
                        }
                    }
                }
            }
            Box(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 8.dp)
                        .padding(bottom = 48.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                ) {
                    CallActionButton(
                        icon = {
                            Icon(painter = painterResource(id = Icons.Hangup), contentDescription = null, tint = Color.White)
                        },
                        text = {
                            Text(text = stringResource(R.string.reject))
                        },
                        color = Color(0xFFF74E57),
                        onClick = onRejectClick,
                    )
                    CallActionButton(
                        icon = {
                            Icon(painter = painterResource(id = Icons.Camera), contentDescription = null, tint = Color.White)
                        },
                        text = {
                            Text(text = stringResource(R.string.answer))
                        },
                        color = Color(0xFF5AD677),
                        onClick = onAnswerClick,
                    )
                }
            }
        }
    }
}

@Preview
@Composable
private fun PreviewVideoCallIncomingScreen() {
    val callIncomingUiState by remember {
        mutableStateOf(
            VideoCallIncomingUiState(
                displayName = "Display Name",
                call = null,
            ),
        )
    }

    VoximplantTheme {
        VideoCallIncomingScreen(
            videoCallIncomingUiState = callIncomingUiState,
            onRejectClick = {},
            onAnswerClick = {},
        )
    }
}
