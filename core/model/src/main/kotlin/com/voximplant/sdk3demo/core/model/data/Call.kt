package com.voximplant.sdk3demo.core.model.data

data class Call(
    val id: String,
    val direction: CallDirection,
    val duration: Long,
    val remoteDisplayName: String?,
    val remoteSipUri: String?,
)
