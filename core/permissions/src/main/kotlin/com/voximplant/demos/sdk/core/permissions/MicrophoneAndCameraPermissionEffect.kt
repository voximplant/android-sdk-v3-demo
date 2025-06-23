/*
 * Copyright (c) 2011 - 2025, Zingaya, Inc. All rights reserved.
 */

package com.voximplant.demos.sdk.core.permissions

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionStatus
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.voximplant.demos.sdk.core.ui.CameraPermissionDialog
import com.voximplant.demos.sdk.core.ui.CameraSettingsDialog
import com.voximplant.demos.sdk.core.ui.MicrophoneAndCameraPermissionDialog
import com.voximplant.demos.sdk.core.ui.MicrophoneAndCameraSettingsDialog
import com.voximplant.demos.sdk.core.ui.MicrophonePermissionDialog
import com.voximplant.demos.sdk.core.ui.MicrophoneSettingsDialog
import kotlinx.coroutines.launch

@Composable
@OptIn(ExperimentalPermissionsApi::class)
fun MicrophoneAndCameraPermissionEffect(
    showMicrophoneAndCameraRationale: Boolean,
    showMicrophoneRationale: Boolean,
    showCameraRationale: Boolean,
    onHideMicrophoneAndCameraDialog: () -> Unit,
    onHideMicrophoneDialog: () -> Unit,
    onHideCameraDialog: () -> Unit,
    onMicrophonePermissionGranted: (Boolean) -> Unit,
    onCameraPermissionGranted: (Boolean) -> Unit,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val scope = rememberCoroutineScope()

    var showMicrophoneAndCameraRequest by rememberSaveable { mutableStateOf(false) }
    var showMicrophoneAndCameraSettings by rememberSaveable { mutableStateOf(false) }

    var showMicrophoneRequest by rememberSaveable { mutableStateOf(false) }
    var showMicrophoneSettings by rememberSaveable { mutableStateOf(false) }

    var showCameraRequest by rememberSaveable { mutableStateOf(false) }
    var showCameraSettings by rememberSaveable { mutableStateOf(false) }

    if (LocalInspectionMode.current) return

    val permissionsState = rememberMultiplePermissionsState(
        permissions = listOf(
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.CAMERA,
        ),
        onPermissionsResult = { permissions ->
            permissions.forEach { permission ->
                when (permission.key) {
                    Manifest.permission.RECORD_AUDIO -> {
                        onMicrophonePermissionGranted(permission.value)
                    }

                    Manifest.permission.CAMERA -> {
                        onCameraPermissionGranted(permission.value)
                    }
                }
            }
        }
    )

    LaunchedEffect(showMicrophoneAndCameraRationale) {
        if (showMicrophoneAndCameraRationale) {
            var shouldShowMicrophoneAndCameraRequest = false
            var shouldShowMicrophoneAndCameraSettings = false

            permissionsState.permissions.forEach { permission ->
                when (permission.permission) {
                    Manifest.permission.RECORD_AUDIO -> {
                        val status = permission.status
                        if (status is PermissionStatus.Denied) {
                            if (!status.shouldShowRationale) {
                                shouldShowMicrophoneAndCameraRequest = true
                            } else {
                                shouldShowMicrophoneAndCameraSettings = true
                            }
                        }
                    }

                    Manifest.permission.CAMERA -> {
                        val status = permission.status
                        if (status is PermissionStatus.Denied) {
                            if (!status.shouldShowRationale) {
                                shouldShowMicrophoneAndCameraRequest = true
                            } else {
                                shouldShowMicrophoneAndCameraSettings = true
                            }
                        }
                    }
                }
            }
            if (shouldShowMicrophoneAndCameraRequest) {
                showMicrophoneAndCameraRequest = true
            } else if (shouldShowMicrophoneAndCameraSettings) {
                showMicrophoneAndCameraSettings = true
            }
        }
    }

    LaunchedEffect(showMicrophoneRationale) {
        if (showMicrophoneRationale) {
            permissionsState.permissions.forEach { permission ->
                if (permission.permission == Manifest.permission.RECORD_AUDIO) {
                    val status = permission.status
                    if (status is PermissionStatus.Denied) {
                        if (!status.shouldShowRationale) {
                            showMicrophoneRequest = true
                        } else {
                            showMicrophoneSettings = true
                        }
                    }
                }
            }
        }
    }

    LaunchedEffect(showCameraRationale) {
        if (showCameraRationale) {
            permissionsState.permissions.forEach { permission ->
                if (permission.permission == Manifest.permission.CAMERA) {
                    val status = permission.status
                    if (status is PermissionStatus.Denied) {
                        if (!status.shouldShowRationale) {
                            showCameraRequest = true
                        } else {
                            showCameraSettings = true
                        }
                    }
                }
            }
        }
    }

    LaunchedEffect(permissionsState, showMicrophoneRequest, showMicrophoneSettings) {
        permissionsState.permissions.forEach { permission ->
            if (permission.permission == Manifest.permission.RECORD_AUDIO) {
                onMicrophonePermissionGranted(permission.status.isGranted)
            }
        }
    }

    LaunchedEffect(permissionsState, showCameraRequest, showCameraSettings) {
        permissionsState.permissions.forEach { permission ->
            if (permission.permission == Manifest.permission.CAMERA) {
                onCameraPermissionGranted(permission.status.isGranted)
            }
        }
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                permissionsState.permissions.forEach { permission ->
                    when (permission.permission) {
                        Manifest.permission.RECORD_AUDIO -> {
                            permission.status.isGranted.let { isGranted ->
                                if (isGranted) {
                                    showMicrophoneRequest = false
                                    showMicrophoneSettings = false
                                }
                                onMicrophonePermissionGranted(isGranted)
                            }
                        }

                        Manifest.permission.CAMERA -> {
                            permission.status.isGranted.let { isGranted ->
                                if (isGranted) {
                                    showCameraRequest = false
                                    showCameraSettings = false
                                }
                                onCameraPermissionGranted(isGranted)
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

    if (showMicrophoneAndCameraRequest) {
        MicrophoneAndCameraPermissionDialog(
            onDismiss = {
                onHideMicrophoneAndCameraDialog()
                showMicrophoneAndCameraRequest = false
            },
            onConfirm = {
                onHideMicrophoneAndCameraDialog()
                showMicrophoneAndCameraRequest = false
                scope.launch {
                    permissionsState.launchMultiplePermissionRequest()
                }
            },
        )
    }

    if (showMicrophoneAndCameraSettings) {
        MicrophoneAndCameraSettingsDialog(
            onDismiss = {
                onHideMicrophoneAndCameraDialog()
                showMicrophoneAndCameraSettings = false
            },
            onConfirm = {
                onHideMicrophoneAndCameraDialog()
                showMicrophoneAndCameraSettings = false
                context.startActivity(
                    Intent(
                        Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                        Uri.fromParts("package", context.packageName, null)
                    )
                )
            },
        )
    }

    if (showMicrophoneRequest) {
        MicrophonePermissionDialog(
            onDismiss = {
                onHideMicrophoneDialog()
                showMicrophoneRequest = false
            },
            onConfirm = {
                onHideMicrophoneDialog()
                showMicrophoneRequest = false
                scope.launch {
                    permissionsState.launchMultiplePermissionRequest()
                }
            },
        )
    }

    if (showMicrophoneSettings) {
        MicrophoneSettingsDialog(
            onDismiss = {
                onHideMicrophoneDialog()
                showMicrophoneSettings = false
            },
            onConfirm = {
                showMicrophoneSettings = false
                context.startActivity(
                    Intent(
                        Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                        Uri.fromParts("package", context.packageName, null)
                    )
                )
            },
        )
    }

    if (showCameraRequest) {
        CameraPermissionDialog(
            onDismiss = {
                onHideCameraDialog()
                showCameraRequest = false
            },
            onConfirm = {
                onHideCameraDialog()
                showCameraRequest = false
                scope.launch {
                    permissionsState.launchMultiplePermissionRequest()
                }
            },
        )
    }

    if (showCameraSettings) {
        CameraSettingsDialog(
            onDismiss = {
                onHideCameraDialog()
                showCameraSettings = false
            },
            onConfirm = {
                showCameraSettings = false
                context.startActivity(
                    Intent(
                        Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                        Uri.fromParts("package", context.packageName, null)
                    )
                )
            },
        )
    }
}