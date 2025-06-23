/*
 * Copyright (c) 2011 - 2024, Zingaya, Inc. All rights reserved.
 */

package com.voximplant.demos.sdk.feature.videocall.incoming

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.voximplant.demos.sdk.core.data.repository.VideoCallRepository
import com.voximplant.demos.sdk.core.model.data.Call
import com.voximplant.demos.sdk.core.model.data.CallDirection
import com.voximplant.demos.sdk.core.model.data.CallState
import com.voximplant.demos.sdk.feature.videocall.incoming.navigation.IncomingCallArgs
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class VideoCallIncomingViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val videoCallRepository: VideoCallRepository,
) : ViewModel() {
    private val incomingCallArgs: IncomingCallArgs = IncomingCallArgs(savedStateHandle)

    val id = incomingCallArgs.id
    private val displayName = incomingCallArgs.displayName

    val callIncomingUiState: StateFlow<VideoCallIncomingUiState> = videoCallIncomingUiState(
        displayName = displayName,
        callFlow = videoCallRepository.callFlow,
    ).stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = VideoCallIncomingUiState(
            displayName = displayName,
            call = null,
        ),
    )

    fun reject() {
        videoCallRepository.reject()
    }

    override fun onCleared() {
        super.onCleared()
        callIncomingUiState.value.call?.let { call ->
            if (call.direction == CallDirection.INCOMING && (call.state is CallState.Created || call.state is CallState.Disconnected || call.state is CallState.Failed)) {
                videoCallRepository.clearCall(call)
            }
        }
    }
}

private fun videoCallIncomingUiState(
    displayName: String?,
    callFlow: Flow<Call?>,
): Flow<VideoCallIncomingUiState> = combine(callFlow) {
    VideoCallIncomingUiState(
        displayName = displayName,
        call = it[0],
    )
}

data class VideoCallIncomingUiState(
    val displayName: String?,
    val call: Call?,
)
