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
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
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
import com.voximplant.sdk3demo.core.model.data.LoginError
import com.voximplant.sdk3demo.core.model.data.Node
import com.voximplant.sdk3demo.core.model.data.Node1
import com.voximplant.sdk3demo.core.model.data.Node10
import com.voximplant.sdk3demo.core.model.data.Node2
import com.voximplant.sdk3demo.core.model.data.Node3
import com.voximplant.sdk3demo.core.model.data.Node4
import com.voximplant.sdk3demo.core.model.data.Node5
import com.voximplant.sdk3demo.core.model.data.Node6
import com.voximplant.sdk3demo.core.model.data.Node7
import com.voximplant.sdk3demo.core.model.data.Node8
import com.voximplant.sdk3demo.core.model.data.Node9
import kotlinx.coroutines.launch

@Composable
fun LoginRoute(
    viewModel: LoginViewModel = hiltViewModel(),
    onLoginSuccess: () -> Unit,
) {
    val scope = rememberCoroutineScope()
    val loginUiState by viewModel.loginUiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    var loginError: LoginError? by rememberSaveable(loginUiState) {
        when (loginUiState.loginState) {
            is LoginState.Success -> {
                onLoginSuccess()
                mutableStateOf(null)
            }

            is LoginState.Failure -> {
                when (val error = (loginUiState.loginState as LoginState.Failure).error) {
                    LoginError.AccountFrozen,
                    LoginError.InternalError,
                    LoginError.Interrupted,
                    LoginError.InvalidState,
                    LoginError.MauAccessDenied,
                    LoginError.NetworkIssue,
                    LoginError.TimeOut,
                    LoginError.TokenExpired,
                    -> error

                    else -> null
                }.let { authError ->
                    mutableStateOf(authError)
                }
            }

            else -> {
                mutableStateOf(null)
            }
        }
    }

    when (loginError) {
        LoginError.AccountFrozen -> stringResource(com.voximplant.sdk3demo.core.resource.R.string.account_frozen_login_error)
        LoginError.InternalError -> stringResource(com.voximplant.sdk3demo.core.resource.R.string.internal_login_error)
        LoginError.Interrupted -> stringResource(com.voximplant.sdk3demo.core.resource.R.string.interrupted_by_user_error)
        LoginError.InvalidState -> stringResource(com.voximplant.sdk3demo.core.resource.R.string.invalid_state_error)
        LoginError.MauAccessDenied -> stringResource(com.voximplant.sdk3demo.core.resource.R.string.mau_access_denied_error)
        LoginError.NetworkIssue -> stringResource(com.voximplant.sdk3demo.core.resource.R.string.network_issue_error)
        LoginError.TimeOut -> stringResource(com.voximplant.sdk3demo.core.resource.R.string.timeout_error)
        LoginError.TokenExpired -> stringResource(com.voximplant.sdk3demo.core.resource.R.string.token_expired_error)
        else -> null
    }?.let { errorMessage ->
        scope.launch {
            snackbarHostState.showSnackbar(
                message = errorMessage,
                duration = SnackbarDuration.Short,
            ).let {
                loginError = null
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    loginUiState: LoginUiState,
    modifier: Modifier = Modifier,
    onLogInClick: (String, String, Node) -> Unit,
) {
    val interactionAvailable = loginUiState.loginState !is LoginState.Loading && loginUiState.loginState !is LoginState.Success

    var username by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var passwordHidden by rememberSaveable { mutableStateOf(true) }

    val nodes = listOf(Node1, Node2, Node3, Node4, Node5, Node6, Node7, Node8, Node9, Node10)
    var nodesExpanded by remember { mutableStateOf(false) }
    var selectedNode: Node? by remember { mutableStateOf(null) }

    val invalidUsername = if (loginUiState.loginState is LoginState.Failure) {
        loginUiState.loginState.error is LoginError.InvalidUsername
    } else false
    val invalidPassword = if (loginUiState.loginState is LoginState.Failure) {
        loginUiState.loginState.error is LoginError.InvalidPassword
    } else false
    var isNodeError by rememberSaveable { mutableStateOf(false) }

    fun validateNode(): Boolean {
        isNodeError = selectedNode == null
        return !isNodeError
    }

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
                suffix = {
                    Text(text = stringResource(R.string.username_suffix))
                },
                supportingText = {
                    if (invalidUsername) {
                        Text(
                            text = stringResource(R.string.invalid_username_error),
                            color = MaterialTheme.colorScheme.error,
                        )
                    }
                },
                singleLine = true,
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
            ExposedDropdownMenuBox(
                expanded = nodesExpanded,
                onExpandedChange = { nodesExpanded = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp),
            ) {
                OutlinedTextField(
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    readOnly = true,
                    value = selectedNode?.toString().orEmpty(),
                    onValueChange = {},
                    enabled = interactionAvailable,
                    label = { Text("Node") },
                    supportingText = {
                        if (isNodeError) {
                            Text(
                                text = stringResource(R.string.required_field),
                                color = MaterialTheme.colorScheme.error,
                            )
                        }
                    },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = nodesExpanded) },
                    isError = isNodeError,
                    shape = RoundedCornerShape(32.dp),
                )
                ExposedDropdownMenu(
                    expanded = nodesExpanded,
                    onDismissRequest = { nodesExpanded = false },
                ) {
                    nodes.forEach { node ->
                        DropdownMenuItem(
                            text = { Text(node.toString()) },
                            onClick = {
                                selectedNode = node
                                isNodeError = false
                                nodesExpanded = false
                            },
                            contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
                        )
                    }
                }
            }
            Button(
                onClick = {
                    passwordHidden = true
                    if (validateNode()) {
                        selectedNode?.let { node ->
                            onLogInClick(username, password, node)
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp),
                enabled = interactionAvailable,
            ) {
                Text(text = stringResource(com.voximplant.sdk3demo.core.resource.R.string.log_in))
            }
        }
    }
}

@Preview
@Composable
private fun LoginScreenPreview() {
    VoximplantTheme {
        LoginScreen(
            loginUiState = LoginUiState(),
            onLogInClick = { _, _, _ -> },
        )
    }
}
