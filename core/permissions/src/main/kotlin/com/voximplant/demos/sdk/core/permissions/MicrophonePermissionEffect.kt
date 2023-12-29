/*
 * Copyright (c) 2011 - 2023, Zingaya, Inc. All rights reserved.
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionStatus
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.voximplant.demos.sdk.core.ui.MicrophonePermissionDialog
import com.voximplant.demos.sdk.core.ui.MicrophoneSettingsDialog

@Composable
@OptIn(ExperimentalPermissionsApi::class)
fun MicrophonePermissionEffect(
    showRationale: Boolean,
    onHideDialog: () -> Unit = {},
    onPermissionGranted: (Boolean) -> Unit,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var showRequest by rememberSaveable { mutableStateOf(false) }
    var showSettings by rememberSaveable { mutableStateOf(false) }

    if (LocalInspectionMode.current) return

    val microphonePermissionState = rememberPermissionState(
        Manifest.permission.RECORD_AUDIO,
        onPermissionResult = { value ->
            onPermissionGranted(value)
        },
    )

    LaunchedEffect(showRationale) {
        if (showRationale) {
            val status = microphonePermissionState.status

            if (status is PermissionStatus.Denied) {
                if (!status.shouldShowRationale) {
                    showRequest = true
                } else {
                    showSettings = true
                }
            }
        }
    }

    LaunchedEffect(microphonePermissionState, showRequest, showSettings) {
        onPermissionGranted(microphonePermissionState.status.isGranted)
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                microphonePermissionState.status.isGranted.let { isGranted ->
                    if (isGranted) {
                        showRequest = false
                        showSettings = false
                    }
                    onPermissionGranted(isGranted)
                }
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    if (showRequest) {
        MicrophonePermissionDialog(
            onDismiss = {
                onHideDialog()
                showRequest = false
            },
            onConfirm = {
                onHideDialog()
                showRequest = false
                microphonePermissionState.launchPermissionRequest()
            },
        )
    }

    if (showSettings) {
        MicrophoneSettingsDialog(
            onDismiss = { showSettings = false },
            onConfirm = {
                showSettings = false
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
