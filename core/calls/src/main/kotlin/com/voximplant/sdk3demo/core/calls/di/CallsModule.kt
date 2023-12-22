package com.voximplant.sdk3demo.core.calls.di

import android.content.Context
import com.voximplant.android.sdk.calls.CallManager
import com.voximplant.sdk3demo.core.calls.CallDataSource
import com.voximplant.sdk3demo.core.common.Dispatcher
import com.voximplant.sdk3demo.core.common.VoxDispatchers.Default
import com.voximplant.sdk3demo.core.common.di.ApplicationScope
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
    ): CallDataSource = CallDataSource(callManager = CallManager.getInstance(context), defaultDispatcher, coroutineScope)

}
