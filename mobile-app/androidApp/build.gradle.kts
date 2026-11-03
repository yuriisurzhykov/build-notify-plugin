import org.jetbrains.kotlin.gradle.dsl.JvmTarget

/*
 * The Android application: entry points only (Application, Activity, foreground
 * service, notifications), the manifest and launcher resources. Everything shared
 * lives in the Kotlin Multiplatform module `:app`.
 *
 * AGP 9 compiles Kotlin itself (built-in Kotlin), so no Kotlin Android plugin is
 * applied; com.android.application cannot be combined with Kotlin Multiplatform.
 */
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.compose.compiler)
    id("quality-conventions")
}

android {
    namespace = "me.yuriisoft.buildnotify.mobile"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "me.yuriisoft.buildnotify.mobile"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = 1
        versionName = "1.0.0"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }
}

kotlin {
    target {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }
}

dependencies {
    implementation(project(":app"))
    // Types the entry points use directly: platform factories handed to AppComponent,
    // connection state for the foreground service, protocol payloads for notifications.
    implementation(project(":core:common"))
    implementation(project(":core:cache"))
    implementation(project(":core:data"))
    implementation(project(":core:navigation"))
    implementation(project(":core:network"))
    implementation(project(":feature:active-builds"))
    implementation(project(":feature:discovery"))

    implementation(libs.jetbrains.compose.runtime)
    implementation(libs.activity.compose)
    implementation(libs.kotlinx.coroutines.core)
}
