plugins {
    id("cmp-library")
}

kotlin {
    android {
        namespace = "me.yuriisoft.buildnotify.mobile.core.navigation"
    }

    sourceSets {
        commonMain.dependencies {
            api(libs.jetbrains.compose.navigation)
            implementation(projects.core.designsystem)
        }
    }
}
