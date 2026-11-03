/*
 * A plain JVM module that only holds tests (e.g. :konsist architecture tests).
 * Applying the Kotlin JVM plugin here keeps its version owned by build-logic.
 */
plugins {
    id("org.jetbrains.kotlin.jvm")
    id("quality-conventions")
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
}
