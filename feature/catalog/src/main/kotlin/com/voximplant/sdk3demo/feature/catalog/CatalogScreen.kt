import android.Manifest
import android.content.Intent
import android.os.Build
import android.provider.Settings
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
import androidx.compose.material3.Icon
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionStatus
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.voximplant.sdk3demo.core.designsystem.icon.Icons
import com.voximplant.sdk3demo.core.designsystem.theme.Gray70
import com.voximplant.sdk3demo.core.designsystem.theme.Typography
import com.voximplant.sdk3demo.core.designsystem.theme.VoximplantTheme
import com.voximplant.sdk3demo.core.model.data.AuthError
import com.voximplant.sdk3demo.core.model.data.User
import com.voximplant.sdk3demo.core.ui.PermissionDialog
import com.voximplant.sdk3demo.feature.catalog.CatalogViewModel
import com.voximplant.sdk3demo.feature.catalog.LoginUiState
import com.voximplant.sdk3demo.feature.catalog.R
import com.voximplant.sdk3demo.feature.catalog.component.CatalogItem
import com.voximplant.sdk3demo.feature.catalog.component.NotificationBanner
import com.voximplant.sdk3demo.feature.catalog.component.UserBanner

@Composable
fun CatalogRoute(
    viewModel: CatalogViewModel = hiltViewModel(),
    onLoginClick: () -> Unit,
    onModuleClick: (String) -> Unit,
) {
    val loginUiState by viewModel.loginUiState.collectAsStateWithLifecycle()
    val user by viewModel.user.collectAsStateWithLifecycle()

    var showNotificationBanner by rememberSaveable { mutableStateOf(false) }
    var showNotificationRationale by rememberSaveable { mutableStateOf(false) }

    var authError: AuthError? by rememberSaveable(loginUiState) {
        if (loginUiState is LoginUiState.Failure) {
            when (val error = (loginUiState as LoginUiState.Failure).error) {
                AuthError.AccountFrozen,
                AuthError.MauAccessDenied,
                AuthError.TokenExpired,
                -> error

                else -> null
            }.let { authError ->
                mutableStateOf(authError)
            }
        } else {
            mutableStateOf(null)
        }
    }

    when (authError) {
        AuthError.AccountFrozen -> {
            AlertDialog(
                onDismissRequest = { authError = null },
                confirmButton = {
                    Button(
                        onClick = { authError = null },
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

        AuthError.MauAccessDenied -> {
            AlertDialog(
                onDismissRequest = { authError = null },
                confirmButton = {
                    Button(
                        onClick = { authError = null },
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

        AuthError.TokenExpired -> {
            AlertDialog(
                onDismissRequest = { authError = null },
                confirmButton = {
                    Button(
                        onClick = {
                            authError = null
                            onLoginClick()
                        },
                    ) {
                        Text(text = stringResource(com.voximplant.sdk3demo.core.resource.R.string.log_in))
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { authError = null },
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

    CatalogScreen(
        user = user,
        onLoginClick = onLoginClick,
        onLogoutClick = viewModel::logout,
        showNotificationBanner = showNotificationBanner,
        onNotificationRequestClick = {
            showNotificationRationale = true
        },
        onModuleClick = onModuleClick,
    )
    NotificationPermissionEffect(
        showRationale = showNotificationRationale,
        onPermissionGranted = { value ->
            showNotificationBanner = !value
            showNotificationRationale = false
        },
    )
}

@Composable
fun CatalogScreen(
    user: User?,
    onLoginClick: () -> Unit,
    onLogoutClick: () -> Unit,
    showNotificationBanner: Boolean,
    onNotificationRequestClick: () -> Unit,
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

            AnimatedVisibility(visible = showNotificationBanner) {
                NotificationBanner(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 12.dp, top = 16.dp, end = 12.dp),
                    onRequestClick = onNotificationRequestClick,
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
                        title = "Audio call", description = "Make and receive an audio call", onClick = { onModuleClick("audio_call_route") }, image = painterResource(id = R.drawable.ic_phone_call_circle)
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
                    text = "Version",
                    color = Gray70,
                    style = Typography.bodySmall,
                )
            }
        }
    }
}

@Composable
@OptIn(ExperimentalPermissionsApi::class)
private fun NotificationPermissionEffect(
    showRationale: Boolean,
    onPermissionGranted: (Boolean) -> Unit,
) {
    val context = LocalContext.current

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

    LaunchedEffect(notificationsPermissionState, showRationale) {
        val status = notificationsPermissionState.status
        if (status is PermissionStatus.Denied && !status.shouldShowRationale) {
            showRequest = true
        } else {
            showSettings = true
        }
    }

    LaunchedEffect(notificationsPermissionState, showRequest) {
        onPermissionGranted(notificationsPermissionState.status.isGranted)
    }

    if (showRequest) {
        PermissionDialog(
            title = { Text(text = stringResource(id = R.string.permission_post_notification)) },
            description = { Text(text = stringResource(id = R.string.permission_post_notification_description)) },
            onDismiss = { showRequest = false },
            onConfirm = {
                showRequest = false
                notificationsPermissionState.launchPermissionRequest()
            },
        )
    }

    if (showSettings) {
        AlertDialog(
            onDismissRequest = { showSettings = false },
            confirmButton = {
                Button(
                    onClick = {
                        showSettings = false
                        context.startActivity(
                            Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                                putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                            }
                        )
                    },
                ) {
                    Text(text = stringResource(R.string.go_to_settings))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showSettings = false
                    },
                ) {
                    Text(text = stringResource(id = android.R.string.cancel))
                }
            },
            icon = {
                Icon(
                    painterResource(id = Icons.Notification),
                    contentDescription = null,
                )
            },
            title = { Text(text = stringResource(id = R.string.permission_post_notification)) },
            text = { Text(text = stringResource(id = R.string.permission_post_notification_description)) },
        )
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
            showNotificationBanner = true,
            onNotificationRequestClick = {},
            onModuleClick = {},
        )
    }
}
