/*
 * Copyright (c) 2011 - 2024, Zingaya, Inc. All rights reserved.
 */

package com.voximplant.demos.sdk.core.camera.manager

import com.voximplant.android.sdk.calls.VideoSource
import com.voximplant.android.sdk.calls.camera.CameraDevice
import com.voximplant.android.sdk.calls.camera.CameraDeviceType
import com.voximplant.android.sdk.calls.camera.CameraManager
import com.voximplant.android.sdk.calls.camera.CameraOrientation
import com.voximplant.android.sdk.calls.camera.CameraResolution
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

class CameraDeviceManager @Inject constructor(
    private val cameraManager: CameraManager,
) {
    val cameraVideoSource: VideoSource
        get() = cameraManager

    private val cameraDevices: List<CameraDevice>
        get() = cameraManager.cameraDevices

    private val _selectedCameraDevice = MutableStateFlow(cameraManager.currentCameraDevice)

    val selectedCameraDevice = _selectedCameraDevice.asStateFlow()
    private val resolution: CameraResolution = CameraResolution.Medium

    init {
        cameraManager.cameraOrientation = CameraOrientation.Screen
        cameraManager.setPreferredResolution(resolution)
        cameraDevices.first { cameraDevice -> cameraDevice.type == CameraDeviceType.Front }
            .let { cameraDevice ->
                _selectedCameraDevice.value = cameraDevice
                cameraManager.selectCameraDevice(cameraDevice)
            }
    }

    fun switchCameraDevice() {
        cameraDevices.firstOrNull { cameraDevice ->
            cameraDevice.type != selectedCameraDevice.value?.type
        }.let { cameraDevice ->
            if (cameraDevice == null) {

                return@let
            }
            cameraManager.selectCameraDevice(cameraDevice)
            _selectedCameraDevice.value = cameraDevice
        }
    }
}