plugins {
    id("kmp-library")
}

kotlin {
    android {
        namespace = "me.yuriisoft.buildnotify.mobile.core.testing"
    }

    sourceSets {
        commonMain.dependencies {
            implementation(projects.core.common)
            implementation(projects.core.network)
            implementation(projects.core.data)
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.coroutines.test)
        }
    }
}
