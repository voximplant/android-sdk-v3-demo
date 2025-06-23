/*
 * Copyright (c) 2011 - 2024, Zingaya, Inc. All rights reserved.
 */

package com.voximplant.demos.sdk.core.video.manager


import com.voximplant.android.sdk.calls.CallManager
import com.voximplant.android.sdk.calls.LocalVideoStream
import com.voximplant.android.sdk.calls.VideoSource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

class LocalVideoManager @Inject constructor(
    private val callManager: CallManager,
) {
    private val _localVideoStream = MutableStateFlow<LocalVideoStream?>(null)
    val localVideoStream = _localVideoStream.asStateFlow()

    suspend fun createLocalVideoStream(videoSource: VideoSource) {
        _localVideoStream.value ?: callManager.createLocalVideoStream(videoSource).let { videoStream ->
            _localVideoStream.emit(videoStream)
        }
    }

    suspend fun releaseLocalVideoStream() {
        _localVideoStream.value?.let {
            _localVideoStream.emit(null)
            it.close()
        }
    }
}