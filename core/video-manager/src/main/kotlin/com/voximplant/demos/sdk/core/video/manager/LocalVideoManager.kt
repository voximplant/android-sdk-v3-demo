/*
 * Copyright (c) 2011 - 2025, Zingaya, Inc. All rights reserved.
 */

package com.voximplant.demos.sdk.core.video.manager


import com.voximplant.android.sdk.calls.LocalVideoStream
import com.voximplant.android.sdk.calls.video.VideoSource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

class LocalVideoManager @Inject constructor() {
    private val _localVideoStream = MutableStateFlow<LocalVideoStream?>(null)
    val localVideoStream = _localVideoStream.asStateFlow()

    suspend fun createLocalVideoStream(videoSource: VideoSource) {
        _localVideoStream.value ?: _localVideoStream.emit(LocalVideoStream(videoSource))
    }

    suspend fun releaseLocalVideoStream() {
        _localVideoStream.value?.let {
            _localVideoStream.emit(null)
            it.removeAllVideoRenderers()
        }
    }
}