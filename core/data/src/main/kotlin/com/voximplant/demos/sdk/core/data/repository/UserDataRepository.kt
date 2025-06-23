/*
 * Copyright (c) 2011 - 2023, Zingaya, Inc. All rights reserved.
 */

package com.voximplant.demos.sdk.core.data.repository

import com.voximplant.demos.sdk.core.datastore.UserPreferencesDataSource
import com.voximplant.demos.sdk.core.model.data.UserData
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class UserDataRepository @Inject constructor(
    private val userPreferencesDataSource: UserPreferencesDataSource,
) {
    val userData: Flow<UserData> = userPreferencesDataSource.userData

    suspend fun setShouldHideNotificationPermissionRequest(shouldHide: Boolean) {
        userPreferencesDataSource.setShouldHideNotificationPermissionRequest(shouldHide)
    }

    suspend fun setShouldHideMicrophonePermissionRequest(shouldHide: Boolean) {
        userPreferencesDataSource.setShouldHideMicrophonePermissionRequest(shouldHide)
    }

    suspend fun setShouldHideCameraPermissionRequest(shouldHide: Boolean) {
        userPreferencesDataSource.setShouldHideCameraPermissionRequest(shouldHide)
    }
}
