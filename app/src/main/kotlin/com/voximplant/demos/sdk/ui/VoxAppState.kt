/*
 * Copyright (c) 2011 - 2023, Zingaya, Inc. All rights reserved.
 */

package com.voximplant.demos.sdk.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController

@Composable
fun rememberVoxAppState(
    navController: NavHostController = rememberNavController(),
): VoxAppState {
    return remember(
        navController,
    ) {
        VoxAppState(
            navController,
        )
    }
}

@Stable
class VoxAppState(
    val navController: NavHostController,
)
