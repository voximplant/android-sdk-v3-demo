/*
 * Copyright (c) 2011 - 2023, Zingaya, Inc. All rights reserved.
 */

package com.voximplant.demos.sdk.feature.audiocall.incoming

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.voximplant.demos.sdk.core.domain.GetCallUseCase
import com.voximplant.demos.sdk.core.domain.RejectIncomingCallUseCase
import com.voximplant.demos.sdk.core.model.data.Call
import com.voximplant.demos.sdk.feature.audiocall.incoming.navigation.IncomingCallArgs
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AudioCallIncomingViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    getCall: GetCallUseCase,
    private val rejectCall: RejectIncomingCallUseCase,
) : ViewModel() {
    private val incomingCallArgs: IncomingCallArgs = IncomingCallArgs(savedStateHandle)

    val id = incomingCallArgs.id
    private val displayName = incomingCallArgs.displayName

    val callIncomingUiState: StateFlow<AudioCallIncomingUiState> = audioCallIncomingUiState(
        displayName = displayName,
        callFlow = getCall(),
    ).stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = AudioCallIncomingUiState(
            displayName = displayName,
            call = null,
        ),
    )

    fun reject() {
        viewModelScope.launch {
            rejectCall()
        }
    }

}

private fun audioCallIncomingUiState(
    displayName: String?,
    callFlow: Flow<Call?>,
): Flow<AudioCallIncomingUiState> = combine(callFlow) {
    AudioCallIncomingUiState(
        displayName = displayName,
        call = it[0],
    )
}

data class AudioCallIncomingUiState(
    val displayName: String?,
    val call: Call?,
)
