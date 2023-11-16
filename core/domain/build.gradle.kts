plugins {
//    id("com.android.library")
//    id("org.jetbrains.kotlin.android")
//    id("com.google.devtools.ksp")
}

android {
    namespace = "com.voximplant.sdk3demo.core.domain"
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
        kotlinCompilerExtensionVersion = "1.5.4"
    }
}

dependencies {
//    implementation(libs.hilt.android)
//    implementation(libs.kotlinx.coroutines.android)
//    ksp(libs.hilt.compiler)
}
