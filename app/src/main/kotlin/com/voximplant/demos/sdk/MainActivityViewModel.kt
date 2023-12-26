/*
 * Copyright (c) 2011 - 2023, Zingaya, Inc. All rights reserved.
 */

package com.voximplant.demos.sdk

import androidx.lifecycle.ViewModel
import com.voximplant.demos.sdk.core.data.repository.AudioCallRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class MainActivityViewModel @Inject constructor(
    audioCallRepository: AudioCallRepository,
) : ViewModel() {
    val uiState: StateFlow<MainActivityUiState> = MutableStateFlow(MainActivityUiState.Success)

    init {
        audioCallRepository.startListeningForIncomingCalls()
    }

}
