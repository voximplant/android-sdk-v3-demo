package com.voximplant.sdk3demo.core.foundation.model

import com.voximplant.sdk3demo.core.model.data.UserData

data class NetworkUserData(
    val user: NetworkUser,
    val accessToken: String,
    val refreshToken: String,
)

fun NetworkUserData.asUserData() = UserData(
    user = user.asUser(),
    accessToken = accessToken,
    refreshToken = refreshToken,
    node = null,
)
