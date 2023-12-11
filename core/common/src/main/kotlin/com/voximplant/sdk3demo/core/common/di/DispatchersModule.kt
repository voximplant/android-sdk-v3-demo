package com.voximplant.sdk3demo.core.common.di

import com.voximplant.sdk3demo.core.common.Dispatcher
import com.voximplant.sdk3demo.core.common.VoxDispatchers
import com.voximplant.sdk3demo.core.common.VoxDispatchers.Default
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

@Module
@InstallIn(SingletonComponent::class)
object DispatchersModule {
    @Provides
    @Dispatcher(VoxDispatchers.IO)
    fun providesIODispatcher(): CoroutineDispatcher = Dispatchers.IO

    @Provides
    @Dispatcher(Default)
    fun providesDefaultDispatcher(): CoroutineDispatcher = Dispatchers.Default
}
