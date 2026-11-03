/*
 * Compose Multiplatform library on top of kmp-library. Android resources are enabled
 * because the Android-KMP library plugin leaves them off by default, and Compose
 * Multiplatform resources are packaged through them on Android.
 */
plugins {
    id("kmp-library")
    id("org.jetbrains.compose")
    id("org.jetbrains.kotlin.plugin.compose")
}

val catalog = the<VersionCatalogsExtension>().named("libs")

kotlin {
    android {
        androidResources {
            enable = true
        }
    }

    sourceSets {
        commonMain.dependencies {
            implementation(catalog.findLibrary("jetbrains-compose-runtime").get())
            implementation(catalog.findLibrary("jetbrains-compose-ui").get())
            implementation(catalog.findLibrary("jetbrains-compose-foundation").get())
        }
    }
}
