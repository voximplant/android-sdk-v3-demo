/*
 * Copyright (c) 2011 - 2024, Zingaya, Inc. All rights reserved.
 */

package com.voximplant.demos.sdk.feature.audiocall

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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.voximplant.demos.sdk.core.designsystem.icon.Icons
import com.voximplant.demos.sdk.core.designsystem.theme.VoximplantTheme
import com.voximplant.demos.sdk.core.model.data.isNotEmpty
import com.voximplant.demos.sdk.core.permissions.MicrophonePermissionEffect
import com.voximplant.demos.sdk.core.permissions.NotificationsPermissionEffect
import com.voximplant.demos.sdk.core.resources.R
import com.voximplant.demos.sdk.core.ui.CallFailedDialog
import com.voximplant.demos.sdk.core.ui.LoginRequiredDialog
import com.voximplant.demos.sdk.core.ui.MicrophoneBanner
import com.voximplant.demos.sdk.core.ui.NotificationsBanner
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AudioCallRoute(
    viewModel: AudioCallViewModel = hiltViewModel(),
    onBackClick: () -> Unit,
    onLoginClick: () -> Unit,
    onCallCreated: (String, String?) -> Unit,
) {
    val scope = rememberCoroutineScope()

    val audioCallUiState by viewModel.audioCallUiState.collectAsStateWithLifecycle()

    var notificationsPermissionGranted by rememberSaveable { mutableStateOf(true) }
    var showNotificationsRationale by rememberSaveable { mutableStateOf(false) }
    var microphonePermissionGranted by rememberSaveable { mutableStateOf(true) }
    var showMicrophoneRationale by rememberSaveable { mutableStateOf(false) }

    var showLoginRequiredDialog by rememberSaveable { mutableStateOf(false) }
    var createCallFailedDescription: String? by rememberSaveable { mutableStateOf(null) }

    var createCallInProgress by rememberSaveable { mutableStateOf(false) }

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
                title = { Text(text = stringResource(id = R.string.audio_call)) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(painterResource(id = Icons.ArrowBack), contentDescription = null)
                    }
                },
            )
        },
    ) { paddingValues ->
        AudioCallScreen(
            modifier = Modifier.padding(paddingValues),
            showNotificationsBanner = !notificationsPermissionGranted,
            showMicrophoneBanner = !microphonePermissionGranted,
            createCallInProgress = createCallInProgress,
            onNotificationsRequestClick = {
                showNotificationsRationale = true
            },
            onMicrophoneRequestClick = {
                showMicrophoneRationale = true
            },
            onCallClick = { username ->
                scope.launch {
                    if (audioCallUiState.user.isNotEmpty()) {
                        if (microphonePermissionGranted) {
                            createCallInProgress = true
                            viewModel.createCall(username).fold(
                                onSuccess = { call ->
                                    createCallInProgress = false
                                    onCallCreated(call.id, username)
                                },
                                onFailure = { throwable ->
                                    createCallInProgress = false
                                    createCallFailedDescription = throwable.toString()
                                },
                            )
                        } else {
                            showMicrophoneRationale = true
                        }
                    } else {
                        showLoginRequiredDialog = true
                    }
                }
            }
        )
    }

    NotificationsPermissionEffect(
        showRationale = showNotificationsRationale || (audioCallUiState.user.isNotEmpty() && ((audioCallUiState as? AudioCallUiState.Inactive)?.shouldShowNotificationPermissionRequest == true)),
        onHideDialog = {
            viewModel.dismissNotificationPermissionRequest()
            showNotificationsRationale = false
        },
        onPermissionGranted = { value ->
            notificationsPermissionGranted = value
            showNotificationsRationale = false
        },
    )

    MicrophonePermissionEffect(
        showRationale = showMicrophoneRationale || (audioCallUiState.user.isNotEmpty() && ((audioCallUiState as? AudioCallUiState.Inactive)?.shouldShowMicrophonePermissionRequest == true)),
        onHideDialog = {
            viewModel.dismissMicrophonePermissionRequest()
            showMicrophoneRationale = false
        },
        onPermissionGranted = { value ->
            microphonePermissionGranted = value
            showMicrophoneRationale = false
        },
    )
}

@Composable
fun AudioCallScreen(
    modifier: Modifier = Modifier,
    showNotificationsBanner: Boolean,
    showMicrophoneBanner: Boolean,
    createCallInProgress: Boolean,
    onNotificationsRequestClick: () -> Unit,
    onMicrophoneRequestClick: () -> Unit,
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
                    Icon(painter = painterResource(id = Icons.Call), contentDescription = null)
                    Text(text = stringResource(id = R.string.make_call))
                }
            }
        }
    }
}

@Preview
@Composable
fun PreviewAudioCallScreen() {
    VoximplantTheme {
        AudioCallScreen(
            showNotificationsBanner = true,
            showMicrophoneBanner = true,
            createCallInProgress = false,
            onNotificationsRequestClick = {},
            onMicrophoneRequestClick = {},
            onCallClick = {},
        )
    }
}
