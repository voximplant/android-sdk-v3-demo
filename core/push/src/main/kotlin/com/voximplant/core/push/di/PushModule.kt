package com.voximplant.core.push.di

import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.ktx.messaging
import com.voximplant.core.push.util.FirebasePushManager
import com.voximplant.core.push.util.FirebasePushTokenProvider
import com.voximplant.sdk3demo.core.data.util.PushManager
import com.voximplant.sdk3demo.core.data.util.PushTokenProvider
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
interface PushModule {

    @Binds
    fun bindsFirebaseTokenProvider(
        pushTokenProvider: FirebasePushTokenProvider,
    ): PushTokenProvider

    @Binds
    fun bindsPushManager(
        firebasePushManager: FirebasePushManager,
    ): PushManager

    companion object {
        @Provides
        @Singleton
        fun provideFirebaseMessaging(): FirebaseMessaging = Firebase.messaging
    }
}
