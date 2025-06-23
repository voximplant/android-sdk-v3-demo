/*
 * Copyright (c) 2011 - 2024, Zingaya, Inc. All rights reserved.
 */

package com.voximplant.demos.sdk

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.voximplant.demos.sdk.core.calls.CallDataSource
import com.voximplant.demos.sdk.core.data.repository.AudioCallRepository
import com.voximplant.demos.sdk.core.data.repository.VideoCallRepository
import com.voximplant.demos.sdk.core.model.data.Call
import com.voximplant.demos.sdk.core.model.data.CallType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class MainActivityViewModel @Inject constructor(
    callDataSource: CallDataSource,
    audioCallRepository: AudioCallRepository,
    videoCallRepository: VideoCallRepository
) : ViewModel() {
    val uiState: StateFlow<MainActivityUiState> = getCallType(
        audioCallRepository.callFlow,
        videoCallRepository.callFlow
    ).stateIn(
        scope = viewModelScope,
        initialValue = MainActivityUiState.Loading,
        started = SharingStarted.WhileSubscribed(5_000),
    )

    init {
        callDataSource.startListeningForIncomingCalls()
    }

    private fun getCallType(
        audioCallFlow: Flow<Call?>,
        videoCallFlow: Flow<Call?>
    ): Flow<MainActivityUiState> = combine(
        audioCallFlow,
        videoCallFlow,
    ) { audioCall, videoCall ->
        if (audioCall?.type == CallType.AudioCall) {
            MainActivityUiState.Success(audioCall)
        } else if (videoCall?.type == CallType.VideoCall) {
            MainActivityUiState.Success(videoCall)
        } else {
            MainActivityUiState.Success(null)
        }
    }
}

sealed interface MainActivityUiState {
    data object Loading : MainActivityUiState
    data class Success(val call: Call?) : MainActivityUiState
}
