/*
 * Copyright (c) 2011 - 2024, Zingaya, Inc. All rights reserved.
 */

package com.voximplant.demos.sdk.core.video.manager.di

import android.content.Context
import com.voximplant.android.sdk.calls.CallManager
import com.voximplant.demos.sdk.core.video.manager.LocalVideoManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object VideoModule {

    @Provides
    @Singleton
    fun providesLocalVideoManager(
        @ApplicationContext context: Context,
    ): LocalVideoManager = LocalVideoManager(callManager = CallManager.getInstance(context))
}
