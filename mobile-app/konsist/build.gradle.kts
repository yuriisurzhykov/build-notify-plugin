/*
 * Architecture tests (Konsist) for the whole mobile project: file size, one type per file,
 * `is` in `when` over sealed objects, layering, no raw styling outside the design system.
 * The rule implementations are shared with ide-plugin: /config/konsist/src.
 */
plugins {
    id("jvm-tests")
}

kotlin {
    sourceSets["test"].kotlin.srcDir(rootDir.resolve("../config/konsist/src"))
}

dependencies {
    testImplementation(libs.konsist)
    testImplementation(kotlin("test-junit5"))
    testImplementation(libs.junit.jupiter)
    testRuntimeOnly(libs.junit.platform.launcher)
}
