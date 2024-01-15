/*
 * Copyright (c) 2011 - 2024, Zingaya, Inc. All rights reserved.
 */

package com.voximplant.demos.sdk

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.voximplant.demos.sdk.core.data.repository.AudioCallRepository
import com.voximplant.demos.sdk.core.model.data.Call
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class MainActivityViewModel @Inject constructor(
    audioCallRepository: AudioCallRepository,
) : ViewModel() {
    val uiState: StateFlow<MainActivityUiState> = audioCallRepository.callFlow.map { call ->
        MainActivityUiState.Success(call)
    }.stateIn(
        scope = viewModelScope,
        initialValue = MainActivityUiState.Loading,
        started = SharingStarted.WhileSubscribed(5_000),
    )

    init {
        audioCallRepository.startListeningForIncomingCalls()
    }

}

sealed interface MainActivityUiState {
    data object Loading : MainActivityUiState
    data class Success(val call: Call?) : MainActivityUiState
}
