/*
 * Copyright (c) 2011 - 2023, Zingaya, Inc. All rights reserved.
 */

plugins {
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.kotlinAndroid)
}

android {
    namespace = "com.voximplant.demos.sdk.core.permissions"
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

    buildFeatures {
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = libs.versions.androidxComposeCompiler.get()
    }
}

dependencies {
    api(project(":core:designsystem"))
    api(project(":core:resources"))
    api(project(":core:ui"))

    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.accompanist.permissions)

    debugApi(libs.androidx.compose.ui.tooling)
}
