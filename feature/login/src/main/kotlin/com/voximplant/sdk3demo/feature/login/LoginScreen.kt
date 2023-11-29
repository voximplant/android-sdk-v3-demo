package com.voximplant.sdk3demo.feature.login

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.voximplant.sdk3demo.core.designsystem.theme.VoximplantTheme
import com.voximplant.sdk3demo.core.model.data.AuthError
import kotlinx.coroutines.launch

@Composable
fun LoginRoute(
    viewModel: LoginViewModel = hiltViewModel(),
    onLoginSuccess: () -> Unit,
) {
    val scope = rememberCoroutineScope()
    val loginUiState by viewModel.loginUiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    if (loginUiState is LoginUiState.Success) {
        onLoginSuccess()
    } else if (loginUiState is LoginUiState.Failure) {
        when ((loginUiState as LoginUiState.Failure).error) {
            AuthError.AccountFrozen -> stringResource(R.string.account_frozen_error)
            AuthError.InternalError -> stringResource(R.string.internal_error_error)
            AuthError.Interrupted -> stringResource(R.string.interrupted_by_user_error)
            AuthError.InvalidState -> stringResource(R.string.invalid_state_error)
            AuthError.MauAccessDenied -> stringResource(R.string.mau_access_denied_error)
            AuthError.NetworkIssue -> stringResource(R.string.network_issue_error)
            AuthError.TimeOut -> stringResource(R.string.timeout_error)
            AuthError.TokenExpired -> stringResource(R.string.token_expired_error)
            else -> null
        }?.let { errorMessage ->
            scope.launch {
                snackbarHostState.showSnackbar(
                    message = errorMessage,
                    duration = SnackbarDuration.Long,
                )
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { paddingValues ->
        LoginScreen(
            loginUiState = loginUiState,
            modifier = Modifier.padding(paddingValues),
            onLogInClick = viewModel::logIn,
        )
    }
}

@Composable
fun LoginScreen(
    loginUiState: LoginUiState,
    modifier: Modifier = Modifier,
    onLogInClick: (String, String) -> Unit,
) {
    val interactionAvailable = loginUiState !is LoginUiState.Loading && loginUiState !is LoginUiState.Success

    var username by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var passwordHidden by rememberSaveable { mutableStateOf(true) }

    val invalidUsername = if (loginUiState is LoginUiState.Failure) {
        loginUiState.error is AuthError.InvalidUsername
    } else false
    val invalidPassword = if (loginUiState is LoginUiState.Failure) {
        loginUiState.error is AuthError.InvalidPassword
    } else false

    Surface(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .padding(vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp),
                enabled = interactionAvailable,
                label = { Text(text = stringResource(R.string.username)) },
                supportingText = {
                    if (invalidUsername) {
                        Text(
                            text = stringResource(R.string.invalid_username_error),
                            color = MaterialTheme.colorScheme.error,
                        )
                    }
                },
                isError = invalidUsername,
                shape = RoundedCornerShape(32.dp),
            )
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp),
                singleLine = true,
                enabled = interactionAvailable,
                label = { Text(text = stringResource(R.string.password)) },
                supportingText = {
                    if (invalidPassword) {
                        Text(
                            text = stringResource(R.string.invalid_password_error),
                            color = MaterialTheme.colorScheme.error,
                        )
                    }
                },
                isError = invalidPassword,
                visualTransformation = if (passwordHidden) PasswordVisualTransformation() else VisualTransformation.None,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                trailingIcon = {
                    IconButton(onClick = { passwordHidden = !passwordHidden }) {
                        val visibilityIcon = if (passwordHidden) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                        Icon(imageVector = visibilityIcon, contentDescription = null)
                    }
                },
                shape = RoundedCornerShape(32.dp),
            )
            Button(
                onClick = {
                    passwordHidden = true
                    onLogInClick(username, password)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp),
                enabled = interactionAvailable,
            ) {
                Text(text = stringResource(R.string.log_in))
            }
        }
    }
}

@Preview
@Composable
private fun LoginScreenPreview() {
    VoximplantTheme {
        LoginScreen(
            loginUiState = LoginUiState.Init,
            onLogInClick = { _, _ -> },
        )
    }
}
