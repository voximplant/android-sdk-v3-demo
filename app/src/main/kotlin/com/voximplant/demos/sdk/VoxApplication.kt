/*
 * Copyright (c) 2011 - 2025, Zingaya, Inc. All rights reserved.
 */

package com.voximplant.demos.sdk

import android.app.Application
import com.voximplant.android.sdk.core.VICore
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class VoxApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        VICore.initialize(this)
    }
}
