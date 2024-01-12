/*
 * Copyright (c) 2011 - 2023, Zingaya, Inc. All rights reserved.
 */

package com.voximplant.demos.sdk.core.calls.di

import android.content.Context
import com.voximplant.android.sdk.calls.CallManager
import com.voximplant.demos.sdk.core.calls.CallDataSource
import com.voximplant.demos.sdk.core.common.Dispatcher
import com.voximplant.demos.sdk.core.common.VoxDispatchers.Default
import com.voximplant.demos.sdk.core.common.di.ApplicationScope
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object CallsModule {

    @Provides
    @Singleton
    fun providesCallsSource(
        @ApplicationContext context: Context,
        @Dispatcher(Default) defaultDispatcher: CoroutineDispatcher,
        @ApplicationScope coroutineScope: CoroutineScope,
    ): CallDataSource = CallDataSource(callManager = CallManager.getInstance(context), coroutineScope)

}
