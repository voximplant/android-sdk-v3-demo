/*
 * Copyright (c) 2011 - 2023, Zingaya, Inc. All rights reserved.
 */

package com.voximplant.demos.sdk.core.foundation.di

import android.content.Context
import com.voximplant.android.sdk.core.Client
import com.voximplant.android.sdk.core.audio.AudioDeviceManager
import com.voximplant.demos.sdk.core.common.di.ApplicationScope
import com.voximplant.demos.sdk.core.foundation.AudioDeviceDataSource
import com.voximplant.demos.sdk.core.foundation.AuthDataSource
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object FoundationModule {

    @Provides
    @Singleton
    fun providesUserDataSource(
        @ApplicationContext context: Context,
        @ApplicationScope coroutineScope: CoroutineScope,
    ): AuthDataSource = AuthDataSource(context, coroutineScope, client = Client.getInstance(context))

    @Provides
    @Singleton
    fun providesAudioDeviceDataSource(
        @ApplicationContext context: Context,
    ): AudioDeviceDataSource = AudioDeviceDataSource(audioDeviceManager = AudioDeviceManager.getInstance(context))

}
