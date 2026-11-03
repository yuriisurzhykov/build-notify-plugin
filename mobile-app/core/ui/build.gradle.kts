plugins {
    id("cmp-library")
}

kotlin {
    android {
        namespace = "me.yuriisoft.buildnotify.mobile.core.ui"
    }

    sourceSets {
        commonMain.dependencies {
            implementation(libs.jetbrains.compose.material)
            implementation(libs.jetbrains.compose.resources)
        }
    }
}
