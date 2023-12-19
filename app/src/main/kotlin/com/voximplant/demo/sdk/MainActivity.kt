package com.voximplant.demo.sdk

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.voximplant.demo.sdk.MainActivityUiState.Loading
import com.voximplant.demo.sdk.MainActivityUiState.Success
import com.voximplant.demo.sdk.ui.VoxApp
import com.voximplant.demo.sdk.ui.VoxAppState
import com.voximplant.demo.sdk.ui.rememberVoxAppState
import com.voximplant.sdk3demo.core.designsystem.theme.VoximplantTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val viewModel: MainActivityViewModel by viewModels()
    private lateinit var voxAppState: VoxAppState

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)

        var uiState: MainActivityUiState by mutableStateOf(Loading)

        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState
                    .onEach {
                        uiState = it
                    }
                    .collect()
            }
        }

        splashScreen.setKeepOnScreenCondition {
            when (uiState) {
                Loading -> true
                is Success -> false
            }
        }

        enableEdgeToEdge()

        setContent {
            voxAppState = rememberVoxAppState()
            val darkTheme = isSystemInDarkTheme()

            DisposableEffect(darkTheme) {
                enableEdgeToEdge(
                    statusBarStyle = SystemBarStyle.auto(
                        Color.TRANSPARENT,
                        Color.TRANSPARENT,
                    ) { darkTheme },
                    navigationBarStyle = SystemBarStyle.auto(
                        lightScrim,
                        darkScrim,
                    ) { darkTheme },
                )
                onDispose {}
            }
            VoximplantTheme(
                darkTheme = darkTheme,
            ) {
                VoxApp(voxAppState)
            }
        }
    }
}

private val lightScrim = Color.argb(0xe6, 0xFF, 0xFF, 0xFF)

private val darkScrim = Color.argb(0x80, 0x1b, 0x1b, 0x1b)
