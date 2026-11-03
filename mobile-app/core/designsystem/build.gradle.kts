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

kotlin {
    android {
        namespace = "me.yuriisoft.buildnotify.mobile.designsystem"
    }

    jvm("desktop")

    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        browser()
    }

    sourceSets {
        all {
            // Graphite components are built on the Compose Styles API (foundation 1.12+).
            // It is experimental; opting in module-wide keeps `Style` out of public
            // signatures, so consumers never depend on the experimental type.
            languageSettings.optIn("androidx.compose.foundation.style.ExperimentalFoundationStyleApi")
        }
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
