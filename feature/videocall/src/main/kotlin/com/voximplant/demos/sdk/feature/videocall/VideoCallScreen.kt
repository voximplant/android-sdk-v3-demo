/*
 * Copyright (c) 2011 - 2024, Zingaya, Inc. All rights reserved.
 */

package com.voximplant.demos.sdk.feature.videocall

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.voximplant.demos.sdk.core.designsystem.icon.Icons
import com.voximplant.demos.sdk.core.designsystem.theme.VoximplantTheme
import com.voximplant.demos.sdk.core.model.data.isNotEmpty
import com.voximplant.demos.sdk.core.permissions.MicrophoneAndCameraPermissionEffect
import com.voximplant.demos.sdk.core.permissions.NotificationsPermissionEffect
import com.voximplant.demos.sdk.core.resources.R
import com.voximplant.demos.sdk.core.ui.CallFailedDialog
import com.voximplant.demos.sdk.core.ui.CameraBanner
import com.voximplant.demos.sdk.core.ui.LoginRequiredDialog
import com.voximplant.demos.sdk.core.ui.MicrophoneBanner
import com.voximplant.demos.sdk.core.ui.NotificationsBanner
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VideoCallRoute(
    viewModel: VideoCallViewModel = hiltViewModel(),
    onBackClick: () -> Unit,
    onLoginClick: () -> Unit,
    onCallCreated: (String, String?) -> Unit,
) {
    val scope = rememberCoroutineScope()
    val lifecycleOwner = LocalLifecycleOwner.current

    val videoCallUiState by viewModel.videoCallUiState.collectAsStateWithLifecycle()

    var notificationsPermissionGranted by rememberSaveable { mutableStateOf(true) }
    var showNotificationsRationale by rememberSaveable { mutableStateOf(false) }
    var microphonePermissionGranted by rememberSaveable { mutableStateOf(true) }
    var showMicrophoneRationale by rememberSaveable { mutableStateOf(false) }
    var cameraPermissionGranted by rememberSaveable { mutableStateOf(true) }
    var showCameraRationale by rememberSaveable { mutableStateOf(false) }
    var microphoneAndCameraRationale by rememberSaveable { mutableStateOf(false) }

    var showLoginRequiredDialog by rememberSaveable { mutableStateOf(false) }
    var createCallFailedDescription: String? by rememberSaveable { mutableStateOf(null) }

    var createCallInProgress by rememberSaveable { mutableStateOf(false) }
    var startCallWithUser by rememberSaveable { mutableStateOf("") }

    if (showLoginRequiredDialog) {
        LoginRequiredDialog(
            onDismiss = {
                showLoginRequiredDialog = false
            },
            onConfirm = {
                showLoginRequiredDialog = false
                onLoginClick()
            },
        )
    }

    createCallFailedDescription?.let { description ->
        CallFailedDialog(
            onConfirm = {
                createCallFailedDescription = null
            },
            description = description,
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(id = R.string.video_call)) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(painterResource(id = Icons.ArrowBack), contentDescription = null)
                    }
                },
            )
        },
    ) { paddingValues ->
        VideoCallScreen(
            modifier = Modifier.padding(paddingValues),
            showNotificationsBanner = !notificationsPermissionGranted,
            showMicrophoneBanner = !microphonePermissionGranted,
            showCameraBanner = !cameraPermissionGranted,
            createCallInProgress = createCallInProgress,
            onNotificationsRequestClick = {
                showNotificationsRationale = true
            },
            onMicrophoneRequestClick = {
                showMicrophoneRationale = true
            },
            onCameraRequestClick = {
                showCameraRationale = true
            },
            onCallClick = { username ->
                scope.launch {
                    if (videoCallUiState.user.isNotEmpty()) {
                        startCallWithUser = username
                        createCallInProgress = true
                    } else {
                        showLoginRequiredDialog = true
                    }
                }
            }
        )
    }

    DisposableEffect(lifecycleOwner, startCallWithUser, createCallInProgress) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                scope.launch {
                    if (microphonePermissionGranted && cameraPermissionGranted && startCallWithUser.isNotEmpty()) {
                        viewModel.createCall(startCallWithUser).fold(
                            onSuccess = { call ->
                                onCallCreated(call.id, startCallWithUser)
                                createCallInProgress = false
                                startCallWithUser = ""
                            },
                            onFailure = { throwable ->
                                createCallFailedDescription = throwable.toString()
                                createCallInProgress = false
                                startCallWithUser = ""
                            },
                        )
                    } else {
                        if (startCallWithUser.isNotEmpty()) {
                            if (!microphonePermissionGranted && !cameraPermissionGranted) {
                                microphoneAndCameraRationale = true
                                createCallInProgress = false
                            } else if (microphonePermissionGranted) {
                                showCameraRationale = true
                                createCallInProgress = false
                            } else {
                                showMicrophoneRationale = true
                                createCallInProgress = false
                            }
                        }
                    }
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    MicrophoneAndCameraPermissionEffect(
        showMicrophoneAndCameraRationale = microphoneAndCameraRationale || (videoCallUiState.user.isNotEmpty() && ((videoCallUiState as? VideoCallUiState.Inactive)?.shouldShowMicrophonePermissionRequest == true) && ((videoCallUiState as? VideoCallUiState.Inactive)?.shouldShowCameraPermissionRequest == true)),
        showMicrophoneRationale = showMicrophoneRationale,
        showCameraRationale = showCameraRationale,
        onHideMicrophoneAndCameraDialog = {
            viewModel.dismissMicrophonePermissionRequest()
            viewModel.dismissCameraPermissionRequest()
            microphoneAndCameraRationale = false
        },
        onHideMicrophoneDialog = {
            viewModel.dismissMicrophonePermissionRequest()
            showMicrophoneRationale = false
        },
        onHideCameraDialog = {
            viewModel.dismissCameraPermissionRequest()
            showCameraRationale = false
        },
        onMicrophonePermissionGranted = { value ->
            microphonePermissionGranted = value
            showMicrophoneRationale = false
        },
        onCameraPermissionGranted = { value ->
            cameraPermissionGranted = value
            showCameraRationale = false
        },
    )

    NotificationsPermissionEffect(
        showRationale = showNotificationsRationale || (videoCallUiState.user.isNotEmpty() && ((videoCallUiState as? VideoCallUiState.Inactive)?.shouldShowNotificationPermissionRequest == true)),
        onHideDialog = {
            viewModel.dismissNotificationPermissionRequest()
            showNotificationsRationale = false
        },
        onPermissionGranted = { value ->
            notificationsPermissionGranted = value
            showNotificationsRationale = false
        },
    )
}

@Composable
fun VideoCallScreen(
    modifier: Modifier = Modifier,
    showNotificationsBanner: Boolean,
    showMicrophoneBanner: Boolean,
    showCameraBanner: Boolean,
    createCallInProgress: Boolean,
    onNotificationsRequestClick: () -> Unit,
    onMicrophoneRequestClick: () -> Unit,
    onCameraRequestClick: () -> Unit,
    onCallClick: (String) -> Unit,
) {
    var username by rememberSaveable { mutableStateOf("") }
    var isError by rememberSaveable { mutableStateOf(false) }

    fun validate(text: String): Boolean {
        isError = text.isBlank()
        return !isError
    }

    Surface(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.padding(start = 12.dp, end = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            AnimatedVisibility(visible = showNotificationsBanner) {
                NotificationsBanner(
                    modifier = Modifier.fillMaxWidth(),
                    onRequestClick = onNotificationsRequestClick,
                )
            }

            AnimatedVisibility(visible = showMicrophoneBanner) {
                MicrophoneBanner(
                    modifier = Modifier.fillMaxWidth(),
                    onRequestClick = onMicrophoneRequestClick,
                )
            }

            AnimatedVisibility(visible = showCameraBanner) {
                CameraBanner(
                    modifier = Modifier.fillMaxWidth(),
                    onRequestClick = onCameraRequestClick,
                )
            }

            OutlinedTextField(
                value = username,
                onValueChange = {
                    username = it
                    isError = false
                },
                modifier = Modifier.fillMaxWidth(),
                label = { Text(text = stringResource(id = R.string.call_to_user)) },
                supportingText = {
                    if (isError) {
                        Text(
                            modifier = Modifier.fillMaxWidth(),
                            text = stringResource(id = R.string.empty_username_error),
                        )
                    }
                },
                isError = isError,
                singleLine = true,
                keyboardActions = KeyboardActions { validate(username) },
                shape = RoundedCornerShape(32.dp),
            )
            Button(
                onClick = {
                    if (validate(username)) {
                        onCallClick(username)
                    }
                },
                modifier = Modifier
                    .padding(horizontal = 12.dp)
                    .align(Alignment.End),
                enabled = !createCallInProgress,
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(painter = painterResource(id = Icons.Camera), contentDescription = null)
                    Text(text = stringResource(id = R.string.make_call))
                }
            }
        }
    }
}

@Preview
@Composable
fun PreviewVideoCallScreen() {
    VoximplantTheme {
        VideoCallScreen(
            showNotificationsBanner = true,
            showMicrophoneBanner = true,
            showCameraBanner = true,
            createCallInProgress = false,
            onNotificationsRequestClick = {},
            onMicrophoneRequestClick = {},
            onCameraRequestClick = {},
            onCallClick = {},
        )
    }
}
