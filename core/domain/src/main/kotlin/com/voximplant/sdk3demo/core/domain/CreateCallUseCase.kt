package com.voximplant.sdk3demo.core.domain

import com.voximplant.sdk3demo.core.data.repository.AudioCallRepository
import com.voximplant.sdk3demo.core.model.data.Call
import javax.inject.Inject

class CreateCallUseCase @Inject constructor(
    private val audioCallRepository: AudioCallRepository,
) {

    operator fun invoke(username: String): Result<Call> {
        return audioCallRepository.createCall(username)
    }
}
