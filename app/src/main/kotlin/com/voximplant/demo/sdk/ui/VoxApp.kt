package com.voximplant.demo.sdk.ui

import androidx.compose.runtime.Composable
import com.voximplant.demo.sdk.navigation.VoxNavHost

@Composable
fun VoxApp(appState: VoxAppState = rememberVoxAppState()) {
    VoxNavHost(appState)
}
