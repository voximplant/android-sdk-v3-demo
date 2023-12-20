package com.voximplant.sdk3demo.core.domain

import com.voximplant.sdk3demo.core.data.repository.AuthDataRepository
import com.voximplant.sdk3demo.core.model.data.Node
import javax.inject.Inject

class SelectNodeUseCase @Inject constructor(
    private val authDataRepository: AuthDataRepository,
) {

    operator fun invoke(node: Node) = authDataRepository.selectNode(node)
}
