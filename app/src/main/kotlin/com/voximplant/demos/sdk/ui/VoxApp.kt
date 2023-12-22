/*
 * Copyright (c) 2011 - 2023, Zingaya, Inc. All rights reserved.
 */

package com.voximplant.demos.sdk.ui

import androidx.compose.runtime.Composable
import com.voximplant.demos.sdk.navigation.VoxNavHost

@Composable
fun VoxApp(appState: VoxAppState = rememberVoxAppState()) {
    VoxNavHost(appState)
}
