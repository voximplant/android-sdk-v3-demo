/*
 * Copyright 2022 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.voximplant.sdk3demo.core.foundation.di

import android.content.Context
import com.voximplant.core.Client
import com.voximplant.core.audio.AudioDeviceManager
import com.voximplant.sdk3demo.core.common.di.ApplicationScope
import com.voximplant.sdk3demo.core.foundation.AudioDeviceDataSource
import com.voximplant.sdk3demo.core.foundation.AuthDataSource
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
    ): AuthDataSource = AuthDataSource(client = Client.getInstance(context), coroutineScope)

    @Provides
    @Singleton
    fun providesAudioDeviceDataSource(
        @ApplicationContext context: Context,
    ): AudioDeviceDataSource = AudioDeviceDataSource(audioDeviceManager = AudioDeviceManager.getInstance(context))

}
