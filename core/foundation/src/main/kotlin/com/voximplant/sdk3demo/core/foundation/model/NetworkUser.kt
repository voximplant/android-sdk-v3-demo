package com.voximplant.sdk3demo.core.foundation.model

import com.voximplant.sdk3demo.core.model.data.User

data class NetworkUser(
    val username: String,
    val displayName: String,
)

fun NetworkUser.asUser() = User(
    username = username,
    displayName = displayName,
)
