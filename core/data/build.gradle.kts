/*
 * Copyright (c) 2011 - 2026, Voximplant, Inc. All rights reserved.
 */

plugins {
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.voximplant.demos.sdk.core.data"
    compileSdk = 37
    defaultConfig {
        minSdk = 23
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

kotlin {
    jvmToolchain(17)
}

dependencies {
    api(project(":core:calls"))
    implementation(project(":core:camera-manager"))
    implementation(project(":core:common"))
    implementation(project(":core:datastore"))
    implementation(project(":core:foundation"))
    implementation(project(":core:logger"))
    implementation(project(":core:model"))
    implementation(project(":core:notifications"))
    implementation(project(":core:video-manager"))

    implementation(libs.androidx.core.ktx)
    implementation(libs.hilt.android)
    implementation(libs.kotlinx.coroutines.android)
    ksp(libs.hilt.compiler)
}
