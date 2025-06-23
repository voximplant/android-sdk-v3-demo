/*
 * Copyright (c) 2011 - 2024, Zingaya, Inc. All rights reserved.
 */

package com.voximplant.demos.sdk.core.data.di

import android.content.Context
import com.voximplant.demos.sdk.core.calls.CallDataSource
import com.voximplant.demos.sdk.core.camera.manager.CameraDeviceManager
import com.voximplant.demos.sdk.core.video.manager.LocalVideoManager
import com.voximplant.demos.sdk.core.common.di.ApplicationScope
import com.voximplant.demos.sdk.core.data.repository.AudioCallRepository
import com.voximplant.demos.sdk.core.data.repository.AudioDeviceRepository
import com.voximplant.demos.sdk.core.data.repository.VideoCallRepository
import com.voximplant.demos.sdk.core.notifications.Notifier
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DataModule {

    @Provides
    @Singleton
    fun providesAudioCallRepository(
        @ApplicationContext context: Context,
        callDataSource: CallDataSource,
        notifier: Notifier,
        @ApplicationScope coroutineScope: CoroutineScope,
        audioDeviceRepository: AudioDeviceRepository,
    ): AudioCallRepository = AudioCallRepository(
        context,
        callDataSource,
        notifier,
        coroutineScope,
        audioDeviceRepository
    )

    @Provides
    @Singleton
    fun provideVideoCallRepository(
        @ApplicationContext context: Context,
        localVideoManager: LocalVideoManager,
        cameraDeviceManager: CameraDeviceManager,
        notifier: Notifier,
        callDataSource: CallDataSource,
        @ApplicationScope coroutineScope: CoroutineScope,
        audioDeviceRepository: AudioDeviceRepository,
    ): VideoCallRepository = VideoCallRepository(
        context,
        localVideoManager,
        cameraDeviceManager,
        notifier,
        callDataSource,
        coroutineScope,
        audioDeviceRepository
    )
}
