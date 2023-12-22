/*
 * Copyright (c) 2011 - 2023, Zingaya, Inc. All rights reserved.
 */

plugins {
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.kotlinAndroid)
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.voximplant.demos.sdk.core.data"
    compileSdk = 34
    defaultConfig {
        minSdk = 21
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {
    implementation(project(":core:calls"))
    implementation(project(":core:common"))
    implementation(project(":core:datastore"))
    implementation(project(":core:foundation"))
    implementation(project(":core:model"))
    implementation(project(":core:notifications"))

    implementation(libs.hilt.android)
    implementation(libs.kotlinx.coroutines.android)
    ksp(libs.hilt.compiler)
}
