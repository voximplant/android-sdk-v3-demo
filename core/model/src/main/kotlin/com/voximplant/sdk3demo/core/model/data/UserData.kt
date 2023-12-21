package com.voximplant.sdk3demo.core.model.data

data class UserData(
    val user: User,
    val accessToken: String,
    val refreshToken: String,
    val node: Node?,
)
