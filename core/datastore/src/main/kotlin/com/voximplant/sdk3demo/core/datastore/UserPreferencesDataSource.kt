package com.voximplant.sdk3demo.core.datastore

import androidx.datastore.core.DataStore
import com.voximplant.sdk3demo.core.model.data.Node
import com.voximplant.sdk3demo.core.model.data.Node1
import com.voximplant.sdk3demo.core.model.data.Node10
import com.voximplant.sdk3demo.core.model.data.Node2
import com.voximplant.sdk3demo.core.model.data.Node3
import com.voximplant.sdk3demo.core.model.data.Node4
import com.voximplant.sdk3demo.core.model.data.Node5
import com.voximplant.sdk3demo.core.model.data.Node6
import com.voximplant.sdk3demo.core.model.data.Node7
import com.voximplant.sdk3demo.core.model.data.Node8
import com.voximplant.sdk3demo.core.model.data.Node9
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
            node = when (userPreferences.node) {
                null,
                NodeProto.UNRECOGNIZED,
                NodeProto.UNASSIGNED,
                -> null

                NodeProto.NODE_1 -> Node1
                NodeProto.NODE_2 -> Node2
                NodeProto.NODE_3 -> Node3
                NodeProto.NODE_4 -> Node4
                NodeProto.NODE_5 -> Node5
                NodeProto.NODE_6 -> Node6
                NodeProto.NODE_7 -> Node7
                NodeProto.NODE_8 -> Node8
                NodeProto.NODE_9 -> Node9
                NodeProto.NODE_10 -> Node10
            },
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

    suspend fun clearUser() {
        userPreferencesDataStore.updateData { userPreferences ->
            userPreferences.copy {
                clearUser()
                clearAccessToken()
                clearRefreshToken()
                clearNode()
            }
        }
    }

    suspend fun updateNode(node: Node) {
        userPreferencesDataStore.updateData { userPreferences ->
            userPreferences.copy {
                this.node = when (node) {
                    Node1 -> NodeProto.NODE_1
                    Node2 -> NodeProto.NODE_2
                    Node3 -> NodeProto.NODE_3
                    Node4 -> NodeProto.NODE_4
                    Node5 -> NodeProto.NODE_5
                    Node6 -> NodeProto.NODE_6
                    Node7 -> NodeProto.NODE_7
                    Node8 -> NodeProto.NODE_8
                    Node9 -> NodeProto.NODE_9
                    Node10 -> NodeProto.NODE_10
                }
            }
        }
    }
}
