package com.voximplant.sdk3demo.feature.audiocall

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.voximplant.sdk3demo.core.designsystem.theme.VoximplantTheme

@Composable
fun AudioCallRoute() {
    AudioCallScreen()
}

@Composable
fun AudioCallScreen() {
    Surface(modifier = Modifier.fillMaxSize()) {
        Text("AudioCall")
    }
}

@Preview
@Composable
fun PreviewAudioCallScreen() {
    VoximplantTheme {
        AudioCallScreen()
    }
}
