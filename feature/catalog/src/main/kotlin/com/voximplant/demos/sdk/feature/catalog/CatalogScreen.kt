/*
 * Copyright (c) 2011 - 2023, Zingaya, Inc. All rights reserved.
 */

package com.voximplant.demos.sdk.feature.catalog

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.voximplant.demos.sdk.core.designsystem.icon.Icons
import com.voximplant.demos.sdk.core.designsystem.theme.Gray70
import com.voximplant.demos.sdk.core.designsystem.theme.Typography
import com.voximplant.demos.sdk.core.designsystem.theme.VoximplantTheme
import com.voximplant.demos.sdk.core.model.data.LoginError
import com.voximplant.demos.sdk.core.model.data.LoginState
import com.voximplant.demos.sdk.core.model.data.User
import com.voximplant.demos.sdk.core.model.data.isNotEmpty
import com.voximplant.demos.sdk.core.permissions.NotificationsPermissionEffect
import com.voximplant.demos.sdk.core.ui.NotificationsBanner
import com.voximplant.demos.sdk.feature.audiocall.navigation.audioCallRoute
import com.voximplant.demos.sdk.feature.catalog.component.CatalogItem
import com.voximplant.demos.sdk.feature.catalog.component.UserBanner
import com.voximplant.demos.sdk.feature.videocall.navigation.videoCallRoute

@Composable
fun CatalogRoute(
    viewModel: CatalogViewModel = hiltViewModel(),
    onLoginClick: () -> Unit,
    onModuleClick: (String) -> Unit,
) {
    val catalogUiState by viewModel.catalogUiState.collectAsStateWithLifecycle()

    var notificationsPermissionGranted by rememberSaveable { mutableStateOf(true) }
    var showNotificationsRationale by rememberSaveable { mutableStateOf(false) }

    var loginError: LoginError? by rememberSaveable(catalogUiState) {
        if (catalogUiState.loginState is LoginState.Failed) {
            when (val error = (catalogUiState.loginState as LoginState.Failed).error) {
                LoginError.AccountFrozen,
                LoginError.MauAccessDenied,
                LoginError.TokenExpired,
                -> error

                else -> null
            }.let { authError ->
                mutableStateOf(authError)
            }
        } else {
            mutableStateOf(null)
        }
    }

    when (loginError) {
        LoginError.AccountFrozen -> {
            AlertDialog(
                onDismissRequest = { loginError = null },
                confirmButton = {
                    Button(
                        onClick = { loginError = null },
                    ) {
                        Text(text = stringResource(id = android.R.string.ok))
                    }
                },
                title = {
                    Text(text = stringResource(com.voximplant.demos.sdk.core.resources.R.string.login_problem))
                },
                text = {
                    Text(text = stringResource(id = com.voximplant.demos.sdk.core.resources.R.string.account_frozen_login_error))
                },
            )
        }

        LoginError.MauAccessDenied -> {
            AlertDialog(
                onDismissRequest = { loginError = null },
                confirmButton = {
                    Button(
                        onClick = { loginError = null },
                    ) {
                        Text(text = stringResource(id = android.R.string.ok))
                    }
                },
                title = {
                    Text(text = stringResource(com.voximplant.demos.sdk.core.resources.R.string.login_problem))
                },
                text = {
                    Text(text = stringResource(id = com.voximplant.demos.sdk.core.resources.R.string.mau_access_denied_error))
                },
            )
        }

        LoginError.TokenExpired -> {
            AlertDialog(
                onDismissRequest = { loginError = null },
                confirmButton = {
                    Button(
                        onClick = {
                            loginError = null
                            onLoginClick()
                        },
                    ) {
                        Text(text = stringResource(com.voximplant.demos.sdk.core.resources.R.string.log_in))
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { loginError = null },
                    ) {
                        Text(text = stringResource(id = android.R.string.cancel))
                    }
                },
                title = {
                    Text(text = stringResource(com.voximplant.demos.sdk.core.resources.R.string.login_problem))
                },
                text = {
                    Text(text = stringResource(id = com.voximplant.demos.sdk.core.resources.R.string.token_expired_error))
                },
            )
        }

        else -> {}
    }

    CatalogScreen(
        user = catalogUiState.user,
        onLoginClick = onLoginClick,
        onLogoutClick = viewModel::logout,
        showNotificationsBanner = !notificationsPermissionGranted,
        onNotificationsRequestClick = {
            showNotificationsRationale = true
        },
        onModuleClick = onModuleClick,
    )

    NotificationsPermissionEffect(
        showRationale = showNotificationsRationale || (catalogUiState.user.isNotEmpty() && catalogUiState.shouldShowNotificationPermissionRequest),
        onHideDialog = {
            viewModel.dismissNotificationPermissionRequest()
            showNotificationsRationale = false
        },
        onPermissionGranted = { value ->
            notificationsPermissionGranted = value
            showNotificationsRationale = false
        },
    )
}

@Composable
fun CatalogScreen(
    user: User,
    onLoginClick: () -> Unit,
    onLogoutClick: () -> Unit,
    showNotificationsBanner: Boolean,
    onNotificationsRequestClick: () -> Unit,
    onModuleClick: (String) -> Unit,
) {
    Surface(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .systemBarsPadding()
                .animateContentSize()
        ) {
            UserBanner(
                user = user,
                modifier = Modifier.padding(start = 12.dp, top = 16.dp, end = 12.dp),
                onLoginClick = onLoginClick,
                onLogoutClick = onLogoutClick,
            )

            AnimatedVisibility(visible = showNotificationsBanner) {
                NotificationsBanner(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 12.dp, top = 16.dp, end = 12.dp),
                    onRequestClick = onNotificationsRequestClick,
                )
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f, false),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                item {
                    CatalogItem(
                        title = stringResource(id = com.voximplant.demos.sdk.core.resources.R.string.audio_call),
                        description = stringResource(id = com.voximplant.demos.sdk.core.resources.R.string.audio_call_description),
                        onClick = { onModuleClick(audioCallRoute) },
                        image = painterResource(id = Icons.Call),
                    )
                }
                item {
                    CatalogItem(
                        title = stringResource(id = com.voximplant.demos.sdk.core.resources.R.string.video_call),
                        description = stringResource(id = com.voximplant.demos.sdk.core.resources.R.string.video_call_description),
                        onClick = { onModuleClick(videoCallRoute) },
                        image = painterResource(id = Icons.Camera),
                    )
                }
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = stringResource(R.string.voximplant_sdk_version, BuildConfig.VOXIMPLANT_SDK_VERSION),
                    color = Gray70,
                    style = Typography.bodySmall,
                )
            }
        }
    }
}

@Preview
@Composable
fun PreviewCatalogScreen() {
    VoximplantTheme {
        CatalogScreen(
            user = User("", ""),
            onLoginClick = {},
            onLogoutClick = {},
            showNotificationsBanner = true,
            onNotificationsRequestClick = {},
            onModuleClick = {},
        )
    }
}
