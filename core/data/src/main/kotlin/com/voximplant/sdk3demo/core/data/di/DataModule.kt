package com.voximplant.sdk3demo.core.data.di

import android.content.Context
import com.voximplant.core.notifications.Notifier
import com.voximplant.sdk3demo.core.calls.CallDataSource
import com.voximplant.sdk3demo.core.common.di.ApplicationScope
import com.voximplant.sdk3demo.core.data.repository.AudioCallRepository
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
