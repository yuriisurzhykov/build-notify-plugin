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
            // api: these modules declare types in AppComponent's public signature (its
            // supertypes and constructor parameters), which :androidApp and iOS consume.
            api(project(":core:common"))
            api(project(":core:data"))
            api(project(":core:network"))
            api(project(":core:navigation"))
            implementation(project(":core:ui"))
            implementation(project(":core:designsystem"))
            api(project(":feature:discovery"))
            api(project(":feature:active-builds"))
            implementation(project(":feature:history"))
            api(project(":feature:network-status"))
            api(project(":core:cache"))
            implementation(project(":core:toast"))
            // TODO: This is just a temporary dependency to view all components catalog.
            api(project(":feature:ui-catalog"))
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
