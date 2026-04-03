plugins {
    id("kmp-library")
    alias(libs.plugins.sqldelight)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "me.yuriisoft.buildnotify.mobile.feature.activebuilds"
}

sqldelight {
    databases {
        create("ActiveBuildsDatabase") {
            packageName.set("me.yuriisoft.buildnotify.mobile.feature.activebuilds.db")
        }
    }
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(project(":core:common"))
            implementation(project(":core:cache"))
            implementation(project(":core:data"))
            implementation(project(":core:network"))
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.sqldelight.coroutines)
        }
        androidMain.dependencies {
            implementation(libs.sqldelight.android.driver)
        }
        iosMain.dependencies {
            implementation(libs.sqldelight.native.driver)
        }
        commonTest.dependencies {
            implementation(libs.kotlinx.coroutines.test)
            implementation(libs.sqldelight.sqlite.driver)
        }
    }
}
