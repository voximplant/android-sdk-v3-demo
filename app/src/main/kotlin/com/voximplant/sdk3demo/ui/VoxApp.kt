package com.voximplant.sdk3demo.ui

import androidx.compose.runtime.Composable
import com.voximplant.sdk3demo.navigation.VoxNavHost

@Composable
fun VoxApp(appState: VoxAppState = rememberVoxAppState()) {
    VoxNavHost(appState)
}
