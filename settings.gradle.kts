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

rootProject.name = "Voximplant SDK v3 Demo"
include(":app")

include(":core:data")
include(":core:datastore")
include(":core:designsystem")
include(":core:domain")
include(":core:model")
include(":core:network")
include(":core:resource")
include(":core:ui")

include(":feature:catalog")
include(":feature:login")
include(":feature:audiocall")
include(":feature:audiocall-ongoing")
