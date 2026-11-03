plugins {
    id("kmp-library")
}

kotlin {
    android {
        namespace = "me.yuriisoft.buildnotify.mobile.core.common"
    }

    sourceSets {
        commonMain.dependencies {
            implementation(libs.kotlinx.coroutines.core)
            // AppScope (core.di) is a kotlin-inject @Scope shared by every feature module.
            api(libs.kotlin.inject.runtime)
        }
    }
}
