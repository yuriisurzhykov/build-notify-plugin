plugins {
    id("kmp-library")
}

android {
    namespace = "me.yuriisoft.buildnotify.mobile.core.common"
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(libs.kotlinx.coroutines.core)
            // AppScope (core.di) is a kotlin-inject @Scope shared by every feature module.
            api(libs.kotlin.inject.runtime)
        }
    }
}
