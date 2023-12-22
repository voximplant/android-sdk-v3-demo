/*
 * Copyright (c) 2011 - 2023, Zingaya, Inc. All rights reserved.
 */

package com.voximplant.demos.sdk.core.data.di

import android.content.Context
import com.voximplant.demos.sdk.core.calls.CallDataSource
import com.voximplant.demos.sdk.core.common.di.ApplicationScope
import com.voximplant.demos.sdk.core.data.repository.AudioCallRepository
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
    ): AudioCallRepository = AudioCallRepository(context, callDataSource, notifier, coroutineScope)

}
