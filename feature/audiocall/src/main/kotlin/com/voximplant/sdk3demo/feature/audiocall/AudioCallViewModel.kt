package com.voximplant.sdk3demo.feature.audiocall

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.voximplant.sdk3demo.core.domain.CreateCallUseCase
import com.voximplant.sdk3demo.core.domain.GetUserUseCase
import com.voximplant.sdk3demo.core.model.data.Call
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class AudioCallViewModel @Inject constructor(
    getUserUseCase: GetUserUseCase,
    private val createCallUseCase: CreateCallUseCase,
) : ViewModel() {
    val user = getUserUseCase().stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = null,
    )

    suspend fun createCall(username: String): Call? = viewModelScope.async {
        createCallUseCase(username)
    }.await().fold(
        onSuccess = { return it },
        onFailure = { return null },
    )
}
