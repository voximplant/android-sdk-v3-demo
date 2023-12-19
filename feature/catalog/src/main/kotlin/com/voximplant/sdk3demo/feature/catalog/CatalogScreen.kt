package com.voximplant.sdk3demo.feature.catalog

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
import androidx.compose.runtime.LaunchedEffect
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
import com.voximplant.sdk3demo.core.designsystem.theme.Gray70
import com.voximplant.sdk3demo.core.designsystem.theme.Typography
import com.voximplant.sdk3demo.core.designsystem.theme.VoximplantTheme
import com.voximplant.sdk3demo.core.model.data.LoginError
import com.voximplant.sdk3demo.core.model.data.LoginState
import com.voximplant.sdk3demo.core.model.data.User
import com.voximplant.sdk3demo.core.permissions.NotificationsPermissionEffect
import com.voximplant.sdk3demo.core.ui.NotificationsBanner
import com.voximplant.sdk3demo.feature.audiocall.navigation.audioCallRoute
import com.voximplant.sdk3demo.feature.catalog.component.CatalogItem
import com.voximplant.sdk3demo.feature.catalog.component.UserBanner

@Composable
fun CatalogRoute(
    viewModel: CatalogViewModel = hiltViewModel(),
    onLoginClick: () -> Unit,
    onModuleClick: (String) -> Unit,
) {
    val catalogUiState by viewModel.catalogUiState.collectAsStateWithLifecycle()
    val user by viewModel.user.collectAsStateWithLifecycle()

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
                    Text(text = stringResource(com.voximplant.sdk3demo.core.resource.R.string.login_problem))
                },
                text = {
                    Text(text = stringResource(id = com.voximplant.sdk3demo.core.resource.R.string.account_frozen_login_error))
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
                    Text(text = stringResource(com.voximplant.sdk3demo.core.resource.R.string.login_problem))
                },
                text = {
                    Text(text = stringResource(id = com.voximplant.sdk3demo.core.resource.R.string.mau_access_denied_error))
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
                        Text(text = stringResource(com.voximplant.sdk3demo.core.resource.R.string.log_in))
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
                    Text(text = stringResource(com.voximplant.sdk3demo.core.resource.R.string.login_problem))
                },
                text = {
                    Text(text = stringResource(id = com.voximplant.sdk3demo.core.resource.R.string.token_expired_error))
                },
            )
        }

        else -> {}
    }

    LaunchedEffect(catalogUiState.call) {
        if (catalogUiState.call != null && catalogUiState.callState !is CallState.Disconnected) {
            onModuleClick(audioCallRoute)
        }
    }

    CatalogScreen(
        user = user,
        onLoginClick = onLoginClick,
        onLogoutClick = viewModel::logout,
        showNotificationsBanner = !notificationsPermissionGranted,
        onNotificationsRequestClick = {
            showNotificationsRationale = true
        },
        onModuleClick = onModuleClick,
    )

    NotificationsPermissionEffect(
        showRationale = showNotificationsRationale,
        onPermissionGranted = { value ->
            notificationsPermissionGranted = value
            showNotificationsRationale = false
        },
    )
}

@Composable
fun CatalogScreen(
    user: User?,
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
                        title = stringResource(id = com.voximplant.sdk3demo.core.resource.R.string.audio_call),
                        description = stringResource(id = com.voximplant.sdk3demo.core.resource.R.string.audio_call_description),
                        onClick = { onModuleClick(audioCallRoute) },
                        image = painterResource(id = R.drawable.ic_phone_call_circle),
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
            user = null,
            onLoginClick = {},
            onLogoutClick = {},
            showNotificationsBanner = true,
            onNotificationsRequestClick = {},
            onModuleClick = {},
        )
    }
}
