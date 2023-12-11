package com.voximplant.sdk3demo.feature.audiocall

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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.voximplant.sdk3demo.core.designsystem.icon.Icons
import com.voximplant.sdk3demo.core.designsystem.theme.VoximplantTheme
import com.voximplant.sdk3demo.core.permissions.MicrophonePermissionEffect
import com.voximplant.sdk3demo.core.permissions.NotificationsPermissionEffect
import com.voximplant.sdk3demo.core.ui.MicrophoneBanner
import com.voximplant.sdk3demo.core.ui.NotificationsBanner

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AudioCallRoute(
    onBackClick: () -> Unit,
    onCallClick: (String) -> Unit,
) {
    var notificationsPermissionGranted by rememberSaveable { mutableStateOf(false) }
    var showNotificationsRationale by rememberSaveable { mutableStateOf(false) }
    var microphonePermissionGranted by rememberSaveable { mutableStateOf(false) }
    var showMicrophoneRationale by rememberSaveable { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(id = com.voximplant.sdk3demo.core.resource.R.string.audio_call)) },
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
                    onCallClick(username)
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
                label = { Text(text = stringResource(id = com.voximplant.sdk3demo.core.resource.R.string.call_to_user)) },
                supportingText = {
                    if (isError) {
                        Text(
                            modifier = Modifier.fillMaxWidth(),
                            text = stringResource(id = com.voximplant.sdk3demo.core.resource.R.string.empty_username_error),
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
                    Text(text = stringResource(id = com.voximplant.sdk3demo.core.resource.R.string.make_call))
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
