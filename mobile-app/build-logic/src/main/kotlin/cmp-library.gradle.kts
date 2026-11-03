plugins {
    id("org.jetbrains.kotlin.multiplatform")
    id("org.jetbrains.compose")
    id("org.jetbrains.kotlin.plugin.compose")
}

// Applied outside `plugins {}` because kmp-library applies AGP; see kmp-library.gradle.kts.
apply(plugin = "kmp-library")

val catalog = the<VersionCatalogsExtension>().named("libs")

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(catalog.findLibrary("jetbrains-compose-runtime").get())
            implementation(catalog.findLibrary("jetbrains-compose-ui").get())
            implementation(catalog.findLibrary("jetbrains-compose-foundation").get())
        }
    }
}
