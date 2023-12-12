package com.voximplant.sdk3demo.feature.audiocall.incoming

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.voximplant.sdk3demo.core.designsystem.icon.Icons
import com.voximplant.sdk3demo.core.designsystem.theme.Gray10
import com.voximplant.sdk3demo.core.designsystem.theme.Typography
import com.voximplant.sdk3demo.core.designsystem.theme.VoximplantTheme
import com.voximplant.sdk3demo.core.ui.CallActionButton
import com.voximplant.sdk3demo.core.ui.CallFailedDialog

@Composable
fun AudioCallIncomingRoute(
    viewModel: AudioCallIncomingViewModel = hiltViewModel(),
    onCallEnded: () -> Unit,
    onCallAnswered: (String) -> Unit,
) {
    val audioCallIncomingUiState by viewModel.callIncomingUiState.collectAsStateWithLifecycle()

    var showCallFailedDialog by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(audioCallIncomingUiState) {
        if (audioCallIncomingUiState.state is CallState.Disconnected) {
            onCallEnded()
        } else if (audioCallIncomingUiState.state is CallState.Failed) {
            showCallFailedDialog = true
        }
    }

    BackHandler {}

    if (showCallFailedDialog) {
        CallFailedDialog(
            onConfirm = {
                showCallFailedDialog = false
                onCallEnded()
            },
        )
    }

    Scaffold { paddingValues ->
        AudioCallIncomingScreen(
            modifier = Modifier.padding(paddingValues),
            audioCallIncomingUiState = audioCallIncomingUiState,
            onRejectClick = {
                viewModel.reject()
            },
            onAnswerClick = {
                onCallAnswered(viewModel.id)
            },
        )
    }
}

@Composable
fun AudioCallIncomingScreen(
    modifier: Modifier = Modifier,
    audioCallIncomingUiState: AudioCallIncomingUiState,
    onRejectClick: () -> Unit,
    onAnswerClick: () -> Unit,
) {
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
                            Text(text = audioCallIncomingUiState.displayName ?: stringResource(com.voximplant.sdk3demo.core.resource.R.string.unknown_user))
                        }
                        ProvideTextStyle(value = Typography.bodySmall.copy(color = Gray10)) {
                        }
                    }
                }
            }
            Box(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 8.dp), horizontalArrangement = Arrangement.spacedBy(24.dp, Alignment.CenterHorizontally)
                ) {
                    CallActionButton(
                        icon = {
                            Icon(painter = painterResource(id = Icons.Hangup), contentDescription = null, tint = Color.White)
                        },
                        text = {
                            Text(text = stringResource(com.voximplant.sdk3demo.core.resource.R.string.reject))
                        },
                        color = Color(0xFFF74E57),
                        onClick = onRejectClick,
                    )
                    CallActionButton(
                        icon = {
                            Icon(painter = painterResource(id = Icons.Call), contentDescription = null, tint = Color.White)
                        },
                        text = {
                            Text(text = stringResource(com.voximplant.sdk3demo.core.resource.R.string.answer))
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
private fun PreviewAudioCallIncomingScreen() {
    val callIncomingUiState by remember {
        mutableStateOf(
            AudioCallIncomingUiState(
                displayName = "Display Name",
                state = CallState.Connecting,
            ),
        )
    }

    VoximplantTheme {
        AudioCallIncomingScreen(
            audioCallIncomingUiState = callIncomingUiState,
            onRejectClick = {},
            onAnswerClick = {},
        )
    }
}
