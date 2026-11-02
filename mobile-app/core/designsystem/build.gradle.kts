import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

/*
 * Graphite — the Build Notifier design system.
 *
 * Pure Compose Multiplatform: depends only on compose runtime / ui / foundation /
 * resources (no Material), so it compiles for every target Compose supports.
 * Android + iOS come from the `cmp-library` convention; desktop (JVM) and web (Wasm)
 * are added here so the system can be previewed and reused outside the phone app.
 */
plugins {
    id("cmp-library")
}

android {
    namespace = "me.yuriisoft.buildnotify.mobile.designsystem"
}

kotlin {
    jvm("desktop")

    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        browser()
    }

    sourceSets {
        commonMain.dependencies {
            api(libs.jetbrains.compose.runtime)
            api(libs.jetbrains.compose.ui)
            api(libs.jetbrains.compose.foundation)
            implementation(libs.jetbrains.compose.resources)
        }
    }
}

compose.resources {
    packageOfResClass = "me.yuriisoft.buildnotify.mobile.designsystem.generated.resources"
}
