package com.voximplant.sdk3demo.core.domain

import com.voximplant.sdk3demo.core.data.repository.AudioCallRepository
import javax.inject.Inject

class StopListeningForIncomingCallsUseCase @Inject constructor(
    private val audioCallRepository: AudioCallRepository,
) {

    operator fun invoke() {
        return audioCallRepository.stopListeningIncomingCalls()
    }
}
