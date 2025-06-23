/*
 * Copyright (c) 2011 - 2024, Zingaya, Inc. All rights reserved.
 */

package com.voximplant.demos.sdk.feature.videocall.ongoing


import android.app.PendingIntent
import android.app.PictureInPictureParams
import android.app.RemoteAction
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.os.Build
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.PictureInPictureModeChangedInfo
import androidx.core.util.Consumer
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.voximplant.android.sdk.calls.RenderScaleType
import com.voximplant.android.sdk.renderer.compose.VideoRenderer
import com.voximplant.demos.sdk.core.common.VoxBroadcastReceiver
import com.voximplant.demos.sdk.core.designsystem.icon.Icons
import com.voximplant.demos.sdk.core.designsystem.theme.Gray15
import com.voximplant.demos.sdk.core.designsystem.theme.Gray20
import com.voximplant.demos.sdk.core.designsystem.theme.Gray25
import com.voximplant.demos.sdk.core.designsystem.theme.Gray30
import com.voximplant.demos.sdk.core.designsystem.theme.Gray70
import com.voximplant.demos.sdk.core.designsystem.theme.VoximplantTheme
import com.voximplant.demos.sdk.core.model.data.AudioDevice
import com.voximplant.demos.sdk.core.model.data.Call
import com.voximplant.demos.sdk.core.model.data.CallDirection
import com.voximplant.demos.sdk.core.model.data.CallState
import com.voximplant.demos.sdk.core.model.data.CallType
import com.voximplant.demos.sdk.core.ui.AudioDevicesDialog
import com.voximplant.demos.sdk.core.ui.CallFailedDialog
import com.voximplant.demos.sdk.core.ui.LocalVideoRenderer
import com.voximplant.demos.sdk.core.ui.VideoCallControlPanel
import kotlinx.coroutines.launch

@Composable
fun VideoCallOngoingRoute(
    viewModel: VideoCallOngoingViewModel = hiltViewModel(),
    onCallEnded: () -> Unit,
) {
    val videoCallOngoingUiState by viewModel.videoCallOngoingUiState.collectAsStateWithLifecycle()

    var videoCallFailedDescription: String? by rememberSaveable { mutableStateOf(null) }

    var showAudioDevices by rememberSaveable { mutableStateOf(false) }

    var onResumeCameraEnabled by rememberSaveable { mutableStateOf(false) }
    val inPipMode = rememberIsInPipMode()

    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val scope = rememberCoroutineScope()

    LaunchedEffect(videoCallOngoingUiState.call) {
        if (videoCallOngoingUiState is VideoCallOngoingUiState.Failed) {
            videoCallFailedDescription = (videoCallOngoingUiState as VideoCallOngoingUiState.Failed).reason
        } else if (videoCallOngoingUiState.call?.state is CallState.Disconnected) {
            onCallEnded()
        }
    }

    LaunchedEffect(videoCallOngoingUiState.call?.state) {
        if (videoCallOngoingUiState.call?.state is CallState.Disconnected || videoCallOngoingUiState.call?.state is CallState.Failed) {
            if (inPipMode && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.findActivity().moveTaskToBack(true)
            }
        }
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_STOP) {
                if (videoCallOngoingUiState is VideoCallOngoingUiState.Active && videoCallOngoingUiState.cameraEnabled && videoCallOngoingUiState.call != null) {
                    viewModel.stopSendingLocalVideo()
                }
                scope.launch {
                    viewModel.videoCallOngoingUiState.collect { state ->
                        if (state is VideoCallOngoingUiState.Active && state.call.duration == 0L) {
                            viewModel.stopSendingLocalVideo()
                            return@collect
                        }
                    }
                }
            }

            if (event == Lifecycle.Event.ON_RESUME) {
                if (videoCallOngoingUiState is VideoCallOngoingUiState.Active && videoCallOngoingUiState.cameraEnabled && videoCallOngoingUiState.call?.direction == CallDirection.OUTGOING) {
                    viewModel.startSendingLocalVideo()
                }

                if ((videoCallOngoingUiState is VideoCallOngoingUiState.Active || videoCallOngoingUiState is VideoCallOngoingUiState.Connecting) && videoCallOngoingUiState.cameraEnabled && videoCallOngoingUiState.call?.direction == CallDirection.INCOMING) {
                    if (onResumeCameraEnabled) {
                        viewModel.startSendingLocalVideo()
                    }
                    onResumeCameraEnabled = true
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && context.packageManager.hasSystemFeature(PackageManager.FEATURE_PICTURE_IN_PICTURE)) {
        DisposableEffect(lifecycleOwner, videoCallOngoingUiState.isMicrophoneMuted, videoCallOngoingUiState.cameraEnabled) {
            context.findActivity().setPictureInPictureParams(PictureInPictureParams.Builder()
                .setActions(getActions(context, videoCallOngoingUiState.isMicrophoneMuted, videoCallOngoingUiState.cameraEnabled))
                .setAutoEnterEnabled(true)
                .build()
            )
            onDispose {
                context.findActivity().setPictureInPictureParams(PictureInPictureParams.Builder()
                    .setAutoEnterEnabled(false)
                    .build()
                )
            }
        }
    } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && Build.VERSION.SDK_INT < Build.VERSION_CODES.S && context.packageManager.hasSystemFeature(PackageManager.FEATURE_PICTURE_IN_PICTURE)) {
        DisposableEffect(context) {
            val onUserLeaveBehavior = Runnable {
                context.findActivity().enterPictureInPictureMode(PictureInPictureParams.Builder()
                    .setActions(getActions(context, videoCallOngoingUiState.isMicrophoneMuted, videoCallOngoingUiState.cameraEnabled))
                    .build()
                )
            }
            context.findActivity().addOnUserLeaveHintListener(
                onUserLeaveBehavior
            )
            onDispose {
                context.findActivity().removeOnUserLeaveHintListener(
                    onUserLeaveBehavior
                )
            }
        }
        LaunchedEffect(videoCallOngoingUiState.isMicrophoneMuted, videoCallOngoingUiState.cameraEnabled) {
            if (inPipMode) {
                context.findActivity().setPictureInPictureParams(PictureInPictureParams.Builder()
                    .setActions(getActions(context, videoCallOngoingUiState.isMicrophoneMuted, videoCallOngoingUiState.cameraEnabled))
                    .build()
                )
            }
        }
    }

    BackHandler {}

    videoCallFailedDescription?.let { description ->
        CallFailedDialog(
            onConfirm = {
                videoCallFailedDescription = null
                onCallEnded()
            },
            description = description,
        )
    }

    if (showAudioDevices) {
        AudioDevicesDialog(
            audioDevices = videoCallOngoingUiState.audioDevices,
            selectedAudioDevice = videoCallOngoingUiState.audioDevice,
            onDismissRequest = {
                showAudioDevices = false
            },
            onAudioDeviceClick = { audioDevice ->
                showAudioDevices = false
                viewModel.selectAudioDevice(audioDevice)
            },
        )
    }

    if (!inPipMode) {
        Scaffold(
            contentWindowInsets = WindowInsets(0.dp),
        ) { paddingValues ->
            VideoCallOngoingScreen(
                modifier = Modifier.padding(paddingValues),
                videoCallOngoingUiState = videoCallOngoingUiState,
                isMicrophoneMuted = videoCallOngoingUiState.isMicrophoneMuted,
                cameraEnabled = videoCallOngoingUiState.cameraEnabled,
                localConfiguration = configuration,
                onSwitchCameraClick = viewModel::switchCamera,
                onHangUpClick = viewModel::hangUpCall,
                onMicrophoneClick = viewModel::toggleMute,
                onCameraClick = viewModel::toggleCameraEnabled,
                onSpeakerClick = {
                    showAudioDevices = true
                },
            )
        }
    } else {
        VideoCallOngoingPiPScreen(videoCallOngoingUiState = videoCallOngoingUiState)
    }
}

@Composable
fun VideoCallOngoingScreen(
    modifier: Modifier = Modifier,
    videoCallOngoingUiState: VideoCallOngoingUiState,
    isMicrophoneMuted: Boolean,
    cameraEnabled: Boolean,
    localConfiguration: Configuration,
    onSwitchCameraClick: () -> Unit,
    onHangUpClick: () -> Unit,
    onMicrophoneClick: () -> Unit,
    onCameraClick: () -> Unit,
    onSpeakerClick: () -> Unit,
) {
    val duration: Long by remember(videoCallOngoingUiState.call) {
        mutableLongStateOf(videoCallOngoingUiState.call?.duration ?: 0L)
    }

    Column(modifier = modifier) {
        Surface(modifier = modifier.fillMaxSize()) {
            if (videoCallOngoingUiState.remoteVideoStream != null) {
                VideoRenderer(
                    modifier = Modifier.wrapContentSize(),
                    videoStream = videoCallOngoingUiState.remoteVideoStream,
                    renderScaleType = RenderScaleType.Balanced
                )
            } else {
                CameraOff()
            }
            Column(
                modifier = Modifier.fillMaxSize().systemBarsPadding(),
                verticalArrangement = Arrangement.Bottom,
            ) {
                if (cameraEnabled) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                    ) {
                        LocalVideoRenderer(
                            modifier = Modifier.padding(8.dp),
                            configuration = localConfiguration.orientation,
                            onSwitchCameraClick = onSwitchCameraClick,
                            content = {
                                VideoRenderer(
                                    modifier = Modifier.fillMaxSize(),
                                    videoStream = videoCallOngoingUiState.localVideoStream,
                                    renderScaleType = RenderScaleType.Fit
                                )
                            }
                        )
                    }
                }

                VideoCallControlPanel(
                    modifier = Modifier.padding(8.dp),
                    callState = videoCallOngoingUiState.call?.state,
                    remoteDisplayName = videoCallOngoingUiState.displayName.toString(),
                    duration = duration,
                    muteEnabled = isMicrophoneMuted,
                    cameraEnabled = cameraEnabled,
                    activeAudioDevice = videoCallOngoingUiState.audioDevice,
                    microphoneButtonEnabled = videoCallOngoingUiState !is VideoCallOngoingUiState.Inactive,
                    cameraButtonEnabled = videoCallOngoingUiState is VideoCallOngoingUiState.Active,
                    speakerButtonEnabled = videoCallOngoingUiState !is VideoCallOngoingUiState.Inactive,
                    onHangUpClick = onHangUpClick,
                    onMicrophoneClick = onMicrophoneClick,
                    onCameraClick = onCameraClick,
                    onSpeakerClick = onSpeakerClick,
                )
            }
        }
    }
}

@Composable
private fun VideoCallOngoingPiPScreen(videoCallOngoingUiState: VideoCallOngoingUiState) {
    Row(
        modifier = Modifier
            .background(Gray20)
            .fillMaxSize()
    ) {
        Box(modifier = Modifier.weight(1f)) {
            if (videoCallOngoingUiState.localVideoStream != null) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    VideoRenderer(
                        videoStream = videoCallOngoingUiState.localVideoStream,
                        renderScaleType = RenderScaleType.Fit,
                    )
                }
                Tag(text = stringResource(com.voximplant.demos.sdk.core.resources.R.string.you))
            } else {
                CameraOffPiPMode(userName = stringResource(com.voximplant.demos.sdk.core.resources.R.string.you))
            }
        }
        Box(modifier = Modifier.weight(1f)) {
            if (videoCallOngoingUiState.remoteVideoStream != null) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    VideoRenderer(
                        videoStream = videoCallOngoingUiState.remoteVideoStream,
                        renderScaleType = RenderScaleType.Fill,
                    )
                }
                if (!videoCallOngoingUiState.call?.remoteDisplayName.isNullOrEmpty()) {
                    Tag(text = videoCallOngoingUiState.call?.remoteDisplayName.toString())
                }
            } else {
                CameraOffPiPMode(userName = videoCallOngoingUiState.call?.remoteDisplayName)
            }
        }
    }
}

@Composable
private fun CameraOffPiPMode(userName: String?) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(color = Gray20),
    ) {
        Box(
            modifier = Modifier
                .clip(CircleShape)
                .background(Gray30)
                .align(Alignment.Center),
        ) {
            Icon(
                modifier = Modifier
                    .padding(16.dp)
                    .size(24.dp),
                painter = painterResource(Icons.Person),
                tint = Gray70,
                contentDescription = null,
            )
        }
        if (!userName.isNullOrEmpty()) {
            Tag(text = userName)
        }
    }
}

@Composable
private fun BoxScope.Tag(text: String) {
    Text(
        modifier = Modifier
            .padding(4.dp)
            .drawBehind {
                drawRoundRect(
                    color = Gray15,
                    alpha = 0.6f,
                    cornerRadius = CornerRadius(4.dp.toPx()),
                )
            }
            .align(Alignment.BottomStart)
            .padding(horizontal = 4.dp),
        text = text.split(" ").firstOrNull().orEmpty(),
        maxLines = 1,
        color = Color.White,
        overflow = TextOverflow.Ellipsis,
        fontSize = 12.sp,
        style = MaterialTheme.typography.bodyLarge,
    )
}

@Composable
private fun CameraOff() {
    Column(
        modifier = Modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(Gray25),
            ) {
                Icon(
                    modifier = Modifier
                        .padding(24.dp)
                        .size(84.dp),
                    painter = painterResource(Icons.Person),
                    tint = Color.White,
                    contentDescription = null,
                )
            }
        }
        Spacer(modifier = Modifier.weight(1f))
    }
}

@Composable
fun rememberIsInPipMode(): Boolean {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val activity = LocalContext.current.findActivity()
        var pipMode by remember { mutableStateOf(activity.isInPictureInPictureMode) }
        DisposableEffect(activity) {
            val observer = Consumer<PictureInPictureModeChangedInfo> { info ->
                pipMode = info.isInPictureInPictureMode
            }
            activity.addOnPictureInPictureModeChangedListener(
                observer
            )
            onDispose { activity.removeOnPictureInPictureModeChangedListener(observer) }
        }
        return pipMode
    } else {
        return false
    }
}

fun Context.findActivity(): ComponentActivity {
    var context = this
    while (context is ContextWrapper) {
        if (context is ComponentActivity) return context
        context = context.baseContext
    }
    throw IllegalStateException("Picture in picture should be called in the context of an Activity")
}

fun getActions(context: Context, isMicrophoneMuted: Boolean, cameraEnabled: Boolean): List<RemoteAction> {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val hangUpCallIntent = PendingIntent.getBroadcast(
            context,
            0,
            Intent(VoxBroadcastReceiver.ACTION_HANG_UP_CALL),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        val muteIntent = PendingIntent.getBroadcast(
            context,
            if (isMicrophoneMuted) 1 else 0,
            Intent(VoxBroadcastReceiver.ACTION_TOGGLE_MUTE),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        val toggleCameraIntent = PendingIntent.getBroadcast(
            context,
            if (cameraEnabled) 1 else 0,
            Intent(VoxBroadcastReceiver.ACTION_TOGGLE_CAMERA),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        val hangUpCallActions = RemoteAction(
            android.graphics.drawable.Icon.createWithResource(context, com.voximplant.demos.sdk.core.designsystem.R.drawable.ic_phone_down),
            "",
            "",
            hangUpCallIntent
        )
        val muteActions = RemoteAction(
            android.graphics.drawable.Icon.createWithResource(
                context,
                if (isMicrophoneMuted) {
                    com.voximplant.demos.sdk.core.designsystem.R.drawable.ic_microphone_off
                } else {
                    com.voximplant.demos.sdk.core.designsystem.R.drawable.ic_microphone
                }
            ),
            "",
            "",
            muteIntent
        )
        val toggleCameraActions = RemoteAction(
            android.graphics.drawable.Icon.createWithResource(
                context,
                if (cameraEnabled) {
                    com.voximplant.demos.sdk.core.designsystem.R.drawable.ic_camera
                } else {
                    com.voximplant.demos.sdk.core.designsystem.R.drawable.ic_camera_off
                }
            ),
            "",
            "",
            toggleCameraIntent
        )

        listOf(toggleCameraActions, hangUpCallActions, muteActions)
    } else {
        listOf()
    }
}

@Preview
@Composable
private fun VideoCallOngoingScreenPreview() {
    var isMuted by remember { mutableStateOf(false) }

    val call = Call(
        "",
        CallState.Connected,
        direction = CallDirection.OUTGOING,
        duration = 0L,
        remoteDisplayName = null,
        remoteSipUri = null,
        CallType.VideoCall,
    )

    val videoCallOngoingUiState by remember(isMuted) {
        mutableStateOf(
            VideoCallOngoingUiState.Active(
                displayName = "Display Name",
                isMicrophoneMuted = isMuted,
                cameraEnabled = false,
                audioDevices = emptyList(),
                audioDevice = AudioDevice(
                    true,
                    id = null,
                    name = "Speaker",
                    type = AudioDevice.Type.SPEAKER
                ),
                call = call,
                localVideoStream = null,
                remoteVideoStream = null,
            )
        )
    }
    VoximplantTheme {
        VideoCallOngoingScreen(
            videoCallOngoingUiState = videoCallOngoingUiState,
            onSwitchCameraClick = {},
            isMicrophoneMuted = isMuted,
            cameraEnabled = true,
            localConfiguration = LocalConfiguration.current,
            onSpeakerClick = {},
            onMicrophoneClick = { isMuted = !isMuted },
            onCameraClick = {},
            onHangUpClick = {},
        )
    }
}

@Preview
@Composable
private fun VideoCallOngoingPiPScreenPreview() {

    val isMuted by remember { mutableStateOf(false) }

    val call = Call(
        "",
        CallState.Connected,
        direction = CallDirection.OUTGOING,
        duration = 0L,
        remoteDisplayName = "Max Max Max Max",
        remoteSipUri = null,
        CallType.VideoCall,
    )

    val videoCallOngoingUiState by remember(isMuted) {
        mutableStateOf(
            VideoCallOngoingUiState.Active(
                displayName = "Display Name",
                isMicrophoneMuted = isMuted,
                cameraEnabled = false,
                audioDevices = emptyList(),
                audioDevice = AudioDevice(
                    true,
                    id = null,
                    name = "Speaker",
                    type = AudioDevice.Type.SPEAKER
                ),
                call = call,
                localVideoStream = null,
                remoteVideoStream = null,
            )
        )
    }

    VoximplantTheme {
        Box(Modifier
            .height(200.dp)
            .width(400.dp)
            .background(Color.White)
        ) {
            VideoCallOngoingPiPScreen(videoCallOngoingUiState = videoCallOngoingUiState)
        }
    }
}

@Preview
@Composable
private fun VideoCallOngoingLongNamePiPScreenPreview() {

    val isMuted by remember { mutableStateOf(false) }

    val call = Call(
        "",
        CallState.Connected,
        direction = CallDirection.OUTGOING,
        duration = 0L,
        remoteDisplayName = "VeryLongNameVeryLongNameVeryLongNameVeryLongName",
        remoteSipUri = null,
        CallType.VideoCall,
    )

    val videoCallOngoingUiState by remember(isMuted) {
        mutableStateOf(
            VideoCallOngoingUiState.Active(
                displayName = "Display Name",
                isMicrophoneMuted = isMuted,
                cameraEnabled = false,
                audioDevices = emptyList(),
                audioDevice = AudioDevice(
                    true,
                    id = null,
                    name = "Speaker",
                    type = AudioDevice.Type.SPEAKER
                ),
                call = call,
                localVideoStream = null,
                remoteVideoStream = null,
            )
        )
    }

    VoximplantTheme {
        Box(Modifier
            .height(200.dp)
            .width(400.dp)
            .background(Color.White)
        ) {
            VideoCallOngoingPiPScreen(videoCallOngoingUiState = videoCallOngoingUiState)
        }
    }
}