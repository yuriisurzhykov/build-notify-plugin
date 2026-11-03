/*
 * The shared application module: dependency graph (AppComponent), navigation and the
 * root UI for every platform, plus each platform's implementations of the core
 * interfaces. It is a Kotlin Multiplatform library; the Android entry points live in
 * `:androidApp`, the iOS app consumes this module's framework.
 */
plugins {
    id("cmp-library")
    alias(libs.plugins.ksp)
}

kotlin {
    android {
        namespace = "me.yuriisoft.buildnotify.mobile.shared"
    }

    sourceSets {
        commonMain.dependencies {
            implementation(project(":core:common"))
            implementation(project(":core:data"))
            implementation(project(":core:network"))
            implementation(project(":core:navigation"))
            implementation(project(":core:ui"))
            implementation(project(":core:designsystem"))
            implementation(project(":feature:discovery"))
            implementation(project(":feature:active-builds"))
            implementation(project(":feature:history"))
            implementation(project(":feature:network-status"))
            implementation(project(":core:cache"))
            implementation(project(":core:toast"))
            // TODO: This is just a temporary dependency to view all components catalog.
            implementation(project(":feature:ui-catalog"))
            implementation(libs.jetbrains.compose.runtime)
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.jetbrains.compose.ui)
            implementation(libs.jetbrains.compose.material)
            implementation(libs.jetbrains.compose.foundation)
            implementation(libs.jetbrains.compose.navigation)
            implementation(libs.lifecycle.viewmodel)
            implementation(libs.kotlin.inject.runtime)
            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.websockets)
            implementation(libs.kotlinx.serialization.json)
        }

        androidMain.dependencies {
            implementation(libs.ktor.client.okhttp)
        }

        iosMain.dependencies {
            implementation(libs.ktor.client.darwin)
        }
    }
}

dependencies {
    add("kspCommonMainMetadata", libs.kotlin.inject.compiler)
    add("kspAndroid", libs.kotlin.inject.compiler)
}
