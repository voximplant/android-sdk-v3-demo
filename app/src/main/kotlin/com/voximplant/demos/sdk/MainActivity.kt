/*
 * Copyright (c) 2011 - 2025, Zingaya, Inc. All rights reserved.
 */

package com.voximplant.demos.sdk

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.voximplant.demos.sdk.MainActivityUiState.Loading
import com.voximplant.demos.sdk.MainActivityUiState.Success
import com.voximplant.demos.sdk.core.designsystem.theme.VoximplantTheme
import com.voximplant.demos.sdk.core.model.data.CallDirection
import com.voximplant.demos.sdk.core.model.data.CallState
import com.voximplant.demos.sdk.core.model.data.CallType
import com.voximplant.demos.sdk.core.notifications.ACTION_NAVIGATE_TO_INCOMING_CALL
import com.voximplant.demos.sdk.feature.audiocall.incoming.navigation.navigateToAudioCallIncoming
import com.voximplant.demos.sdk.feature.audiocall.ongoing.navigation.audioCallOngoingRoute
import com.voximplant.demos.sdk.feature.audiocall.ongoing.navigation.navigateToAudioCallOngoing
import com.voximplant.demos.sdk.feature.videocall.incoming.navigation.navigateToVideoCallIncoming
import com.voximplant.demos.sdk.feature.videocall.incoming.navigation.videoCallIncomingRoute
import com.voximplant.demos.sdk.feature.videocall.ongoing.navigation.navigateToVideoCallOngoing
import com.voximplant.demos.sdk.feature.videocall.ongoing.navigation.videoCallOngoingRoute
import com.voximplant.demos.sdk.ui.VoxApp
import com.voximplant.demos.sdk.ui.VoxAppState
import com.voximplant.demos.sdk.ui.rememberVoxAppState
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val viewModel: MainActivityViewModel by viewModels()
    private lateinit var voxAppState: VoxAppState

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        var uiState: MainActivityUiState by mutableStateOf(Loading)

        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState
                    .onEach {
                        uiState = it
                    }
                    .collect()
            }
        }

        lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                if (state is Success) {
                    val ignoreLockScreen = state.call?.direction == CallDirection.INCOMING && state.call.state !is CallState.Disconnected && state.call.state !is CallState.Failed

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
                        setShowWhenLocked(ignoreLockScreen)
                        setTurnScreenOn(ignoreLockScreen)
                    } else {
                        @Suppress("DEPRECATION")
                        if (ignoreLockScreen) {
                            window.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON)
                        } else {
                            window.clearFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON)
                        }
                    }
                }
            }
        }

        splashScreen.setKeepOnScreenCondition {
            when (uiState) {
                Loading -> true
                is Success -> false
            }
        }

        val requestMicrophonePermission = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                lifecycleScope.launch {
                    val state = viewModel.uiState.first()
                    if (state is Success && state.call != null && voxAppState.navController.currentDestination?.route?.contains(audioCallOngoingRoute) == false) {
                        voxAppState.navController.navigateToAudioCallOngoing(
                            state.call.id,
                            state.call.remoteDisplayName
                        )
                        intent.action = null
                    }
                }
            }
        }

        val requestMicrophoneAndCameraPermission = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            if (permissions[Manifest.permission.RECORD_AUDIO] == true && permissions[Manifest.permission.CAMERA] == true) {
                lifecycleScope.launch {
                    val state = viewModel.uiState.first()
                    if (state is Success && state.call != null && voxAppState.navController.currentDestination?.route?.contains(videoCallOngoingRoute) == false) {
                        voxAppState.navController.navigateToVideoCallOngoing(
                            state.call.id,
                            state.call.remoteDisplayName
                        )
                        intent.action = null
                    }
                }
            }
        }

        enableEdgeToEdge()

        setContent {
            voxAppState = rememberVoxAppState()
            val darkTheme = false // isSystemInDarkTheme()

            val lifecycleOwner = LocalLifecycleOwner.current
            DisposableEffect(lifecycleOwner) {
                val observer = LifecycleEventObserver { _, event ->
                    if (event == Lifecycle.Event.ON_RESUME) {
                        val state = viewModel.uiState.value
                        if (state is Success) {
                            when (state.call?.type) {
                                CallType.AudioCall -> {
                                    if (state.call.state is CallState.Connecting || state.call.state is CallState.Connected) {
                                        requestMicrophonePermission.launch(Manifest.permission.RECORD_AUDIO)
                                    }
                                }

                                CallType.VideoCall -> {
                                    if (state.call.state is CallState.Connecting || state.call.state is CallState.Connected) {
                                        requestMicrophoneAndCameraPermission.launch(
                                            arrayOf(
                                                Manifest.permission.RECORD_AUDIO,
                                                Manifest.permission.CAMERA
                                            )
                                        )
                                    }
                                }
                                null -> {}
                            }
                        }
                    }
                }
                lifecycleOwner.lifecycle.addObserver(observer)

                onDispose {
                    lifecycleOwner.lifecycle.removeObserver(observer)
                }
            }

            LaunchedEffect(Unit) {
                lifecycleScope.launch {
                    viewModel.uiState.collect { state ->
                        if (state is Success) {
                            if (!isNotificationsPermissionGranted()) {
                                if (state.call?.direction == CallDirection.INCOMING && (state.call.state is CallState.Created || state.call.state is CallState.Reconnecting) && state.call.duration == 0L) {
                                    when (state.call.type) {
                                        CallType.AudioCall -> {
                                            voxAppState.navController.navigateToAudioCallIncoming(
                                                state.call.id,
                                                state.call.remoteDisplayName
                                            )
                                        }

                                        CallType.VideoCall -> {
                                            voxAppState.navController.navigateToVideoCallIncoming(
                                                state.call.id,
                                                state.call.remoteDisplayName
                                            )
                                        }
                                    }
                                }
                            } else {
                                when (intent.action) {
                                    ACTION_NAVIGATE_TO_INCOMING_CALL -> {
                                        intent.action = null
                                        when(state.call?.type) {
                                            CallType.AudioCall -> {
                                                if (state.call.direction == CallDirection.INCOMING && (state.call.state is CallState.Created || state.call.state is CallState.Reconnecting)) {
                                                    if (voxAppState.navController.currentDestination?.route?.contains(audioCallOngoingRoute) == false) {
                                                        voxAppState.navController.navigateToAudioCallIncoming(
                                                            state.call.id,
                                                            state.call.remoteDisplayName
                                                        )
                                                        intent.action = null
                                                    }
                                                }
                                            }

                                            CallType.VideoCall -> {
                                                if (state.call.direction == CallDirection.INCOMING && (state.call.state is CallState.Created || state.call.state is CallState.Reconnecting)) {
                                                    if (voxAppState.navController.currentDestination?.route?.contains(videoCallIncomingRoute) == false) {
                                                        voxAppState.navController.navigateToVideoCallIncoming(
                                                            state.call.id,
                                                            state.call.remoteDisplayName
                                                        )
                                                        intent.action = null
                                                    }
                                                }
                                            }
                                            null -> {}
                                        }
                                    }
                                    Intent.ACTION_ANSWER -> {
                                        intent.action = null
                                        when (state.call?.type) {
                                            CallType.AudioCall -> {
                                                requestMicrophonePermission.launch(Manifest.permission.RECORD_AUDIO)
                                            }
                                            CallType.VideoCall -> {
                                                requestMicrophoneAndCameraPermission.launch(arrayOf(Manifest.permission.RECORD_AUDIO, Manifest.permission.CAMERA))
                                            }
                                            null -> {}
                                        }
                                    }
                                }

                                if (state.call?.type == CallType.AudioCall) {
                                    if (state.call.direction == CallDirection.INCOMING && (state.call.state is CallState.Connecting || state.call.state is CallState.Connected)) {
                                        if (voxAppState.navController.currentDestination?.route?.contains(audioCallOngoingRoute) == false) {
                                            voxAppState.navController.navigateToAudioCallOngoing(
                                                state.call.id,
                                                state.call.remoteDisplayName
                                            )
                                            intent.action = null
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            DisposableEffect(darkTheme) {
                enableEdgeToEdge(
                    statusBarStyle = SystemBarStyle.auto(
                        Color.TRANSPARENT,
                        Color.TRANSPARENT,
                    ) { darkTheme },
                    navigationBarStyle = SystemBarStyle.auto(
                        lightScrim,
                        darkScrim,
                    ) { darkTheme },
                )
                onDispose {}
            }
            VoximplantTheme(
                darkTheme = darkTheme,
            ) {
                VoxApp(voxAppState)
            }
        }
    }

    private fun isNotificationsPermissionGranted(): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return true
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED
    }
}

private val lightScrim = Color.argb(0xe6, 0xFF, 0xFF, 0xFF)

private val darkScrim = Color.argb(0x80, 0x1b, 0x1b, 0x1b)
