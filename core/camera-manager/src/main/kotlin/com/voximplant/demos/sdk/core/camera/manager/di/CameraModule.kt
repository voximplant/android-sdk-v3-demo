/*
 * Copyright (c) 2011 - 2025, Voximplant, Inc. All rights reserved.
 */

package com.voximplant.demos.sdk.core.camera.manager.di

import com.voximplant.android.sdk.calls.video.CameraVideoSource
import com.voximplant.demos.sdk.core.camera.manager.CameraDeviceManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object CameraModule {
    @Provides
    @Singleton
    fun provideCameraDeviceManager(): CameraDeviceManager = CameraDeviceManager(cameraVideoSource = CameraVideoSource)
}