/*
 * Copyright (c) 2011 - 2025, Voximplant, Inc. All rights reserved.
 */

package com.voximplant.demos.sdk.core.camera.manager

import com.voximplant.android.sdk.calls.video.CameraDevice
import com.voximplant.android.sdk.calls.video.CameraDeviceType
import com.voximplant.android.sdk.calls.video.CameraOrientation
import com.voximplant.android.sdk.calls.video.CameraResolution
import com.voximplant.android.sdk.calls.video.CameraVideoSource
import com.voximplant.android.sdk.calls.video.VideoSource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

class CameraDeviceManager @Inject constructor(
    val cameraVideoSource: VideoSource
) {

    private val cameraDevices: List<CameraDevice>
        get() = CameraVideoSource.cameraDevices

    private val _selectedCameraDevice: MutableStateFlow<CameraDevice?> = MutableStateFlow(CameraVideoSource.currentCameraDevice)

    val selectedCameraDevice = _selectedCameraDevice.asStateFlow()
    private val resolution: CameraResolution = CameraResolution.Medium

    init {
        CameraVideoSource.cameraOrientation = CameraOrientation.Screen
        CameraVideoSource.setPreferredResolution(resolution)
        cameraDevices.first { cameraDevice -> cameraDevice.type == CameraDeviceType.Front }
            .let { cameraDevice ->
                _selectedCameraDevice.value = cameraDevice
                CameraVideoSource.selectCameraDevice(cameraDevice)
            }
    }

    fun switchCameraDevice() {
        cameraDevices.firstOrNull { cameraDevice ->
            cameraDevice.type != selectedCameraDevice.value?.type
        }.let { cameraDevice ->
            if (cameraDevice == null) {

                return@let
            }
            CameraVideoSource.selectCameraDevice(cameraDevice)
            _selectedCameraDevice.value = cameraDevice
        }
    }
}