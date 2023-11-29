package com.voximplant.sdk3demo.feature.catalog

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.voximplant.sdk3demo.core.domain.GetUserUseCase
import com.voximplant.sdk3demo.core.domain.LogOutUseCase
import com.voximplant.sdk3demo.core.domain.SilentLogInUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CatalogViewModel @Inject constructor(
    getUserUseCase: GetUserUseCase,
    private val silentLogInUseCase: SilentLogInUseCase,
    private val logOutUseCase: LogOutUseCase,
) : ViewModel() {
    val user = getUserUseCase().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = null,
    )

    init {
        viewModelScope.launch {
            silentLogInUseCase()
        }
    }

    fun logout() {
        viewModelScope.launch {
            logOutUseCase()
        }
    }
}
