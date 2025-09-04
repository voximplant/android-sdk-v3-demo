/*
 * Copyright (c) 2011 - 2025, Zingaya, Inc. All rights reserved.
 */

package com.voximplant.demos.sdk.core.video.manager.di

import com.voximplant.demos.sdk.core.video.manager.LocalVideoManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object VideoModule {

    @Provides
    @Singleton
    fun providesLocalVideoManager(): LocalVideoManager = LocalVideoManager()
}
