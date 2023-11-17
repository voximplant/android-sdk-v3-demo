package com.voximplant.sdk3demo.feature.login

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.voximplant.sdk3demo.core.designsystem.theme.VoximplantTheme

@Composable
fun LoginRoute() {
    LoginScreen()
}

@Composable
fun LoginScreen() {
    Surface(modifier = Modifier.fillMaxSize()) {
        Text("Login")
    }
}

@Preview
@Composable
private fun LoginScreenPreview() {
    VoximplantTheme {
        LoginScreen()
    }
}
