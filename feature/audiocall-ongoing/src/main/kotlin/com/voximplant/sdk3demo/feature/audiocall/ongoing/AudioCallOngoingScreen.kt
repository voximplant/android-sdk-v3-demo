package com.voximplant.sdk3demo.feature.audiocall.ongoing

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import com.voximplant.sdk3demo.core.designsystem.theme.VoximplantTheme

@Composable
fun AudioCallOngoingRoute(
    viewModel: AudioCallOngoingViewModel = hiltViewModel(),
) {
    val username = viewModel.username

    AudioCallOngoingScreen(username)
}

@Composable
fun AudioCallOngoingScreen(
    username: String,
) {
    Surface(modifier = Modifier.fillMaxSize()) {
        Text(text = "username: $username")
    }
}

@Preview
@Composable
fun PreviewAudioCallScreen() {
    VoximplantTheme {
        AudioCallOngoingScreen(username = "username")
    }
}
