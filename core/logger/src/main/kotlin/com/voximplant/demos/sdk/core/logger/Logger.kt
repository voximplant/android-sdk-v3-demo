/*
 * Copyright (c) 2011 - 2023, Zingaya, Inc. All rights reserved.
 */

package com.voximplant.demos.sdk.core.logger

import android.util.Log

const val TAG = "Voximplant"

object Logger {

    fun verbose(tag: String, message: String) {
        Log.v(tag, message)
    }

    fun verbose(message: String) {
        Log.v(TAG, message)
    }

    fun debug(tag: String, message: String) {
        Log.d(tag, message)
    }

    fun debug(message: String) {
        Log.d(TAG, message)
    }

    fun info(tag: String, message: String) {
        Log.i(tag, message)
    }

    fun info(message: String) {
        Log.i(TAG, message)
    }

    fun warning(tag: String, message: String) {
        Log.w(tag, message)
    }

    fun warning(message: String) {
        Log.w(TAG, message)
    }

    fun error(tag: String, message: String, throwable: Throwable? = null) {
        Log.e(tag, message, throwable)
    }

    fun error(message: String, throwable: Throwable? = null) {
        Log.e(TAG, message, throwable)
    }
}
