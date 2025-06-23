/*
 * Copyright (c) 2011 - 2024, Zingaya, Inc. All rights reserved.
 */

package com.voximplant.demos.sdk.core.camera.manager.di

import android.content.Context
import com.voximplant.android.sdk.calls.camera.CameraManager
import com.voximplant.demos.sdk.core.camera.manager.CameraDeviceManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object CameraModule {
    @Provides
    @Singleton
    fun provideCameraDeviceManager(
        @ApplicationContext context: Context,
    ): CameraDeviceManager = CameraDeviceManager(cameraManager = CameraManager.getInstance(context))
}