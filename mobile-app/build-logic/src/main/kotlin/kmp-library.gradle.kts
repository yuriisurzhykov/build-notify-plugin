import com.android.build.api.dsl.LibraryExtension
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id("org.jetbrains.kotlin.multiplatform")
    id("apple-ios-targets")
    id("quality-conventions")
}

// AGP is applied here, not in `plugins {}`: Gradle generates accessors for precompiled
// script plugins by applying that block to a synthetic project that does not read
// gradle.properties, and AGP 9 rejects com.android.library next to Kotlin Multiplatform
// there. Real modules still get the AGP 9 bridge flags from the root gradle.properties.
apply(plugin = "com.android.library")

kotlin {
    androidTarget {
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

extensions.configure<LibraryExtension> {
    val catalog = the<VersionCatalogsExtension>().named("libs")
    compileSdk = catalog.findVersion("android-compileSdk").get().toString().toInt()
    defaultConfig {
        minSdk = catalog.findVersion("android-minSdk").get().toString().toInt()
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}
