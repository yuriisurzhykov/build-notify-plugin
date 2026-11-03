import org.jetbrains.kotlin.gradle.dsl.JvmTarget

/*
 * Kotlin Multiplatform library with an Android target, on the Android-KMP library
 * plugin (`com.android.kotlin.multiplatform.library`), as AGP 9 requires.
 *
 * Each module sets only its namespace:
 *
 *     kotlin { android { namespace = "…" } }
 *
 * Unit tests (commonTest + androidHostTest) run on the JVM with `testAndroidHostTest`.
 */
plugins {
    id("org.jetbrains.kotlin.multiplatform")
    id("com.android.kotlin.multiplatform.library")
    id("apple-ios-targets")
    id("quality-conventions")
}

val catalog = the<VersionCatalogsExtension>().named("libs")

kotlin {
    android {
        compileSdk = catalog.findVersion("android-compileSdk").get().toString().toInt()
        minSdk = catalog.findVersion("android-minSdk").get().toString().toInt()

        // Host tests are off by default with this plugin; every module has commonTest.
        withHostTest {}

        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }

    sourceSets {
        commonTest.dependencies {
            implementation(kotlin("test"))
        }
    }
}
