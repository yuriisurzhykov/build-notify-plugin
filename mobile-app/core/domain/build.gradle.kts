plugins {
    id("kmp-library")
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "me.yuriisoft.buildnotify.mobile.core.domain"
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.serialization.json)
        }
    }
}
