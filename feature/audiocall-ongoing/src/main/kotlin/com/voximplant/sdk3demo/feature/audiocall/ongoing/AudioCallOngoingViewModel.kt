package com.voximplant.sdk3demo.feature.audiocall.ongoing

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.voximplant.sdk3demo.feature.audiocall.ongoing.navigation.OngoingCallArgs
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class AudioCallOngoingViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
) : ViewModel() {
    private val ongoingCallArgs: OngoingCallArgs = OngoingCallArgs(savedStateHandle)
    val username = ongoingCallArgs.username
}
