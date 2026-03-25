plugins {
    id("kmp-library")
}

android {
    namespace = "me.yuriisoft.buildnotify.mobile.core.testing"
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(project(":core:domain"))
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.coroutines.test)
        }
    }
}
