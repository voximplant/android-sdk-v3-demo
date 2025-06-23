/*
 * Copyright (c) 2011 - 2023, Zingaya, Inc. All rights reserved.
 */

package com.voximplant.demos.sdk.core.permissions

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
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
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionStatus
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.voximplant.demos.sdk.core.ui.CameraPermissionDialog
import com.voximplant.demos.sdk.core.ui.CameraSettingsDialog

@Composable
@OptIn(ExperimentalPermissionsApi::class)
fun CameraPermissionEffect(
    showRationale: Boolean,
    onHideDialog: () -> Unit = {},
    onPermissionGranted: (Boolean) -> Unit,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var showRequest by rememberSaveable { mutableStateOf(false) }
    var showSettings by rememberSaveable { mutableStateOf(false) }

    if (LocalInspectionMode.current) return
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return

    val cameraPermissionState = rememberPermissionState(
        Manifest.permission.CAMERA,
        onPermissionResult = { value ->
            onPermissionGranted(value)
        },
    )

    LaunchedEffect(showRationale) {
        if (showRationale) {
            val status = cameraPermissionState.status

            if (status is PermissionStatus.Denied) {
                if (!status.shouldShowRationale) {
                    showRequest = true
                } else {
                    showSettings = true
                }
            }
        }
    }

    LaunchedEffect(cameraPermissionState, showRequest, showSettings) {
        onPermissionGranted(cameraPermissionState.status.isGranted)
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                cameraPermissionState.status.isGranted.let { isGranted ->
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
        CameraPermissionDialog(
            onDismiss = {
                onHideDialog()
                showRequest = false
            },
            onConfirm = {
                onHideDialog()
                showRequest = false
                cameraPermissionState.launchPermissionRequest()
            },
        )
    }

    if (showSettings) {
        CameraSettingsDialog(
            onDismiss = {
                onHideDialog()
                showSettings = false
            },
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
