/*
 * Copyright (c) 2011 - 2023, Zingaya, Inc. All rights reserved.
 */

pluginManagement {
    repositories {
        mavenLocal()
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        mavenLocal()
        google()
        mavenCentral()
        maven {
            url = uri("https://oss.sonatype.org/content/groups/staging")
        }
    }
}

rootProject.name = "Voximplant SDK Demo"
include(":app")

include(":core:calls")
include(":core:common")
include(":core:data")
include(":core:datastore")
include(":core:designsystem")
include(":core:domain")
include(":core:foundation")
include(":core:model")
include(":core:notifications")
include(":core:permissions")
include(":core:push")
include(":core:resources")
include(":core:ui")

include(":feature:catalog")
include(":feature:login")
include(":feature:audiocall")
include(":feature:audiocall-incoming")
include(":feature:audiocall-ongoing")
