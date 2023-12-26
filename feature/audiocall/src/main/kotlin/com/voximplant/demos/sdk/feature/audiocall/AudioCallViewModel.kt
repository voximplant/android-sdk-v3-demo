/*
 * Copyright (c) 2011 - 2023, Zingaya, Inc. All rights reserved.
 */

package com.voximplant.demos.sdk.feature.audiocall

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.voximplant.demos.sdk.core.domain.CreateCallUseCase
import com.voximplant.demos.sdk.core.domain.GetCallUseCase
import com.voximplant.demos.sdk.core.domain.GetUserUseCase
import com.voximplant.demos.sdk.core.model.data.Call
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class AudioCallViewModel @Inject constructor(
    getUserUseCase: GetUserUseCase,
    private val createCallUseCase: CreateCallUseCase,
    getCall: GetCallUseCase,
) : ViewModel() {
    val user = getUserUseCase().stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = null,
    )

    val audioCallUiState: StateFlow<AudioCallUiState> = audioCallUiState(
        callFlow = getCall(),
    ).stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = AudioCallUiState.Inactive,
    )

    suspend fun createCall(username: String): Call? = viewModelScope.async {
        createCallUseCase(username)
    }.await().fold(
        onSuccess = { return it },
        onFailure = { return null },
    )
}

private fun audioCallUiState(
    callFlow: Flow<Call?>,
): Flow<AudioCallUiState> = combine(callFlow) {
    val call = it[0]
    if (call == null) {
        AudioCallUiState.Inactive
    } else {
        AudioCallUiState.Active(call = call)
    }
}

sealed interface AudioCallUiState {
    data class Active(val call: Call) : AudioCallUiState
    data object Inactive : AudioCallUiState
}
