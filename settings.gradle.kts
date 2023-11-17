pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "Voximplant SDK v3 Demo"
include(":app")

include(":core:designsystem")

include(":feature:catalog")
include(":feature:login")
include(":feature:audiocall")
