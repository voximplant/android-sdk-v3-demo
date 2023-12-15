package com.voximplant.sdk3demo.core.domain

import com.voximplant.sdk3demo.core.data.repository.AudioCallRepository
import javax.inject.Inject

class GetMuteStateUseCase @Inject constructor(
    private val callRepository: AudioCallRepository,
) {
    operator fun invoke() = callRepository.isMuted
}
