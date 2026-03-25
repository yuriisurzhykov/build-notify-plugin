pluginManagement {
    includeBuild("build-logic")
    repositories {
        google {
            mavenContent {
                includeGroupAndSubgroups("androidx")
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
            }
        }
        mavenCentral()
        maven("https://maven.pkg.jetbrains.space")
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google {
            mavenContent {
                includeGroupAndSubgroups("androidx")
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
            }
        }
        mavenCentral()
        maven("https://maven.pkg.jetbrains.space")
    }
}

rootProject.name = "build-notify-mobile"

include(":core:domain")
include(":core:data")
include(":core:navigation")
include(":core:ui")
include(":core:testing")
include(":feature:discovery")
include(":feature:build-status")
include(":feature:history")
include(":feature:ui-catalog")
include(":app")
