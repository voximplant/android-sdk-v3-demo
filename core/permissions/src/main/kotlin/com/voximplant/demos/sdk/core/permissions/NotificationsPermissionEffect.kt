/*
 * Copyright (c) 2011 - 2023, Zingaya, Inc. All rights reserved.
 */

package com.voximplant.demos.sdk.core.permissions

import android.Manifest
import android.content.Intent
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
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionStatus
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.voximplant.demos.sdk.core.ui.NotificationsPermissionDialog
import com.voximplant.demos.sdk.core.ui.NotificationsSettingsDialog

@Composable
@OptIn(ExperimentalPermissionsApi::class)
fun NotificationsPermissionEffect(
    showRationale: Boolean,
    onPermissionGranted: (Boolean) -> Unit,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var showRequest by rememberSaveable { mutableStateOf(false) }
    var showSettings by rememberSaveable { mutableStateOf(false) }

    if (LocalInspectionMode.current) return
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return

    val notificationsPermissionState = rememberPermissionState(
        Manifest.permission.POST_NOTIFICATIONS,
        onPermissionResult = { value ->
            onPermissionGranted(value)
        },
    )

    LaunchedEffect(notificationsPermissionState, showRationale, lifecycleOwner) {
        val status = notificationsPermissionState.status

        if (status is PermissionStatus.Denied) {
            if (!status.shouldShowRationale) {
                showRequest = true
            } else if (showRationale) {
                showSettings = true
            }
        }
    }

    LaunchedEffect(notificationsPermissionState, showRequest, showSettings) {
        onPermissionGranted(notificationsPermissionState.status.isGranted)
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                notificationsPermissionState.status.isGranted.let { isGranted ->
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
        NotificationsPermissionDialog(
            onDismiss = { showRequest = false },
            onConfirm = {
                showRequest = false
                notificationsPermissionState.launchPermissionRequest()
            },
        )
    }

    if (showSettings) {
        NotificationsSettingsDialog(
            onDismiss = { showSettings = false },
            onConfirm = {
                showSettings = false
                context.startActivity(
                    Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                        putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                    }
                )
            },
        )
    }
}
