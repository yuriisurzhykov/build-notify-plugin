plugins {
    id("cmp-library")
}

android {
    namespace = "me.yuriisoft.buildnotify.mobile.feature.networkstatus"
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(project(":core:toast"))
            implementation(project(":core:network"))
            implementation(project(":core:navigation"))
            implementation(project(":core:ui"))
        }

        commonTest.dependencies {
            implementation(libs.kotlinx.coroutines.test)
            implementation(kotlin("test"))
        }
    }
}
