package com.voximplant.sdk3demo.core.domain

import com.voximplant.sdk3demo.core.data.repository.AudioCallRepository
import com.voximplant.sdk3demo.core.model.data.Call
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetCallUseCase @Inject constructor(
    private val callRepository: AudioCallRepository,
) {
    operator fun invoke(): Flow<Call> = callRepository.call
}
