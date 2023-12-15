package com.voximplant.sdk3demo.core.domain

import com.voximplant.sdk3demo.core.data.repository.AudioCallRepository
import com.voximplant.sdk3demo.core.model.data.Call
import javax.inject.Inject

class StartCallUseCase @Inject constructor(
    private val audioCallRepository: AudioCallRepository,
) {

    operator fun invoke(id: String): Result<Call> {
        return audioCallRepository.startCall(id)
    }
}
