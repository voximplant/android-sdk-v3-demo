/*
 * Copyright (c) 2011 - 2023, Zingaya, Inc. All rights reserved.
 */

plugins {
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.kotlinAndroid)
}

android {
    namespace = "com.voximplant.demos.sdk.core.ui"
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

    api(platform(libs.androidx.compose.bom))
    api(libs.androidx.compose.runtime)
    api(libs.androidx.compose.ui)
    api(libs.androidx.compose.ui.tooling.preview)
    api(libs.androidx.compose.ui.util)
    api(libs.androidx.compose.foundation)
    api(libs.androidx.compose.material.iconsExtended)
    api(libs.androidx.compose.material3)

    debugApi(libs.androidx.compose.ui.tooling)
}
