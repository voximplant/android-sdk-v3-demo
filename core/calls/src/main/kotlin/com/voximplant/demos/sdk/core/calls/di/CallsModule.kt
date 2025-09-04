/*
 * Copyright (c) 2011 - 2025, Zingaya, Inc. All rights reserved.
 */

package com.voximplant.demos.sdk.core.calls.di

import com.voximplant.android.sdk.calls.VICalls
import com.voximplant.demos.sdk.core.calls.CallDataSource
import com.voximplant.demos.sdk.core.common.di.ApplicationScope
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object CallsModule {

    @Provides
    @Singleton
    fun providesCallsSource(
        @ApplicationScope coroutineScope: CoroutineScope,
    ): CallDataSource = CallDataSource(callManager = VICalls.apply { initialize() }, coroutineScope)
}
