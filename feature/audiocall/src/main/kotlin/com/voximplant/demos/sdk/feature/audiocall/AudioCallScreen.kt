/*
 * Copyright (c) 2011 - 2023, Zingaya, Inc. All rights reserved.
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import com.voximplant.demos.sdk.core.model.data.Call
import com.voximplant.demos.sdk.core.model.data.CallDirection
import com.voximplant.demos.sdk.core.permissions.MicrophonePermissionEffect
import com.voximplant.demos.sdk.core.permissions.NotificationsPermissionEffect
import com.voximplant.demos.sdk.core.resources.R
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
    onIncomingCall: (String, String?) -> Unit,
    onCallCreated: (String, String?) -> Unit,
) {
    val scope = rememberCoroutineScope()

    val audioCallUiState by viewModel.audioCallUiState.collectAsStateWithLifecycle()
    val rememberCall = remember { (audioCallUiState as? AudioCallUiState.Active)?.call }

    var notificationsPermissionGranted by rememberSaveable { mutableStateOf(true) }
    var showNotificationsRationale by rememberSaveable { mutableStateOf(false) }
    var microphonePermissionGranted by rememberSaveable { mutableStateOf(true) }
    var showMicrophoneRationale by rememberSaveable { mutableStateOf(false) }

    var showLoginRequiredDialog by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(audioCallUiState) {
        if (audioCallUiState is AudioCallUiState.Active) {
            val call = (audioCallUiState as AudioCallUiState.Active).call
            val state = (audioCallUiState as AudioCallUiState.Active).state

            if (call == rememberCall) return@LaunchedEffect

            if (call.direction == CallDirection.INCOMING && state is CallState.Created) {
                onIncomingCall(call.id, call.remoteDisplayName)
            } else if (state is CallState.Connected) {
                onCallCreated(call.id, call.remoteDisplayName)
            }
        }
    }

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
            onNotificationsRequestClick = {
                showNotificationsRationale = true
            },
            onMicrophoneRequestClick = {
                showMicrophoneRationale = true
            },
            onCallClick = { username ->
                if (microphonePermissionGranted) {
                    scope.launch {
                        if (viewModel.user.value != null) {
                            viewModel.createCall(username).let { call: Call? ->
                                if (call != null) {
                                    onCallCreated(call.id, username)
                                } else {
                                    // TODO (Oleg): show error
                                }
                            }
                        } else {
                            showLoginRequiredDialog = true
                        }
                    }
                } else {
                    showMicrophoneRationale = true
                }
            },
        )
    }

    NotificationsPermissionEffect(
        showRationale = showNotificationsRationale,
        onPermissionGranted = { value ->
            notificationsPermissionGranted = value
            showNotificationsRationale = false
        },
    )

    MicrophonePermissionEffect(
        showRationale = showMicrophoneRationale,
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
            modifier = Modifier.padding(vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            AnimatedVisibility(visible = showNotificationsBanner) {
                NotificationsBanner(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 12.dp, top = 16.dp, end = 12.dp),
                    onRequestClick = onNotificationsRequestClick,
                )
            }

            AnimatedVisibility(visible = showMicrophoneBanner) {
                MicrophoneBanner(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 12.dp, top = 16.dp, end = 12.dp),
                    onRequestClick = onMicrophoneRequestClick,
                )
            }

            OutlinedTextField(
                value = username,
                onValueChange = {
                    username = it
                    isError = false
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp),
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
            onNotificationsRequestClick = {},
            onMicrophoneRequestClick = {},
            onCallClick = {},
        )
    }
}
