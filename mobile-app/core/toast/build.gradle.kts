plugins {
    id("cmp-library")
}

android {
    namespace = "me.yuriisoft.buildnotify.mobile.core.toast"
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(projects.core.ui)
        }

        commonTest.dependencies {
            implementation(libs.kotlinx.coroutines.test)
            implementation(kotlin("test"))
        }
    }
}
