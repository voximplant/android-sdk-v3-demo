package com.voximplant.sdk3demo.core.datastore

import androidx.datastore.core.DataStore
import com.voximplant.sdk3demo.core.model.data.User
import com.voximplant.sdk3demo.core.model.data.UserData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class UserPreferencesDataSource @Inject constructor(
    private val userPreferencesDataStore: DataStore<UserPreferences>,
) {
    val userData: Flow<UserData?> = userPreferencesDataStore.data.map { userPreferences ->
        if (!userPreferences.hasUser()) return@map null

        UserData(
            user = User(
                username = userPreferences.user.username,
                displayName = userPreferences.user.displayName,
            ),
            accessToken = userPreferences.accessToken,
            refreshToken = userPreferences.refreshToken,
        )
    }

    suspend fun updateUser(userData: UserData) {
        userPreferencesDataStore.updateData { userPreferences ->
            userPreferences.copy {
                user = userProto {
                    username = userData.user.username
                    displayName = userData.user.displayName
                }
                accessToken = userData.accessToken
                refreshToken = userData.refreshToken
            }
        }
    }

    suspend fun updateTokens(accessToken: String, refreshToken: String) {
        userPreferencesDataStore.updateData { userPreferences ->
            userPreferences.copy {
                this.accessToken = accessToken
                this.refreshToken = refreshToken
            }
        }
    }

    suspend fun clearUser() {
        userPreferencesDataStore.updateData { userPreferences ->
            userPreferences.copy {
                clearUser()
                clearAccessToken()
                clearRefreshToken()
            }
        }
    }
}
