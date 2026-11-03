import org.jetbrains.changelog.Changelog
import org.jetbrains.changelog.date
import io.gitlab.arturbosch.detekt.Detekt
import io.gitlab.arturbosch.detekt.getSupportedKotlinVersion
import org.jetbrains.intellij.platform.gradle.TestFrameworkType

plugins {
    alias(libs.plugins.jetbrains.kotlin.jvm)
    alias(libs.plugins.jetbrains.kotlin.serialization)
    alias(libs.plugins.jetbrains.kotlin.kover)
    alias(libs.plugins.jetbrains.platform)
    alias(libs.plugins.jetbrains.qodana)
    alias(libs.plugins.jetbrains.changelog)
    alias(libs.plugins.detekt)
    alias(libs.plugins.ktlint)
}

group = providers.gradleProperty("plugin.groupId")
version = providers.gradleProperty("plugin.version").get()

repositories {
    mavenCentral()
    intellijPlatform {
        marketplace()
        defaultRepositories()
        androidStudioInstallers()
    }
}

// Read more: https://plugins.jetbrains.com/docs/intellij/tools-intellij-platform-gradle-plugin.html
dependencies {
    intellijPlatform {
        androidStudio(providers.gradleProperty("platformVersion").get())
        testFramework(TestFrameworkType.Platform)
        pluginVerifier()
        zipSigner()

        composeUI()

        bundledPlugins(
            "com.intellij.gradle",
            "org.jetbrains.kotlin",
        )
    }

    implementation(libs.java.websockets)
    implementation(libs.java.mdns)
    implementation(libs.kotlin.serialization.json)

    compileOnly(libs.kotlin.serialization.core)
    compileOnly(libs.kotlin.coroutines.core)

    testImplementation(libs.test.junit)
    testImplementation(libs.test.mockk)
    testImplementation(libs.test.opentest4j)
    testImplementation(libs.kotlin.coroutines.test)
}

intellijPlatform {
    pluginConfiguration {
        name = providers.gradleProperty("plugin.name")
        version = providers.gradleProperty("plugin.version")
        description = """
            <p>
            Sensds Gradle build results to your Android phone over local Wi-Fi.
            Multiple devices supported simultaneously. </br>
            Zero configuration - discovered automatically via mDNS.
            </p>
            <ul>
            <li>Real-time build status: SUCCESS / FAILED / CANCELLED</li>
            <li>Detailed Kotlin and Java compiler errors and warnings</li>
            <li>Heartbeat keeps mobile connected alive between builds.</li>
            </ul>
        """.trimIndent()

        ideaVersion {
            sinceBuild = providers.gradleProperty("plugin.sinceBuild")
            untilBuild = providers.gradleProperty("plugin.untilBuild")
        }

        val changelog = project.changelog
        changeNotes = providers.gradleProperty("plugin.version").map { version ->
            with(changelog) {
                renderItem(
                    (getOrNull(version) ?: getUnreleased())
                        .withHeader(false)
                        .withEmptySections(false),
                    Changelog.OutputType.HTML,
                )
            }
        }
    }

    signing {
        // Fill in when publishing to JetBrains Marketplace.
        // certificateChain = providers.environmentVariable("CERTIFICATE_CHAIN")
        // privateKey       = providers.environmentVariable("PRIVATE_KEY")
        // password         = providers.environmentVariable("PRIVATE_KEY_PASSWORD")
    }

    publishing {
        // Set PUBLISH_TOKEN in your environment before running publishPlugin
        token = providers.environmentVariable("PUBLISHING_TOKEN")
    }

    pluginVerification {
        ides {

        }
    }
}

kotlin {
    jvmToolchain(21)
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_21)
    }
}


// ── Code quality ─────────────────────────────────────────────────────────────
// Same rules as mobile-app: /config/detekt/detekt.yml, /.editorconfig, /config/konsist.
// Existing findings are frozen in detekt-baseline.xml / ktlint-baseline.xml.

detekt {
    buildUponDefaultConfig = true
    config.setFrom(rootDir.parentFile.resolve("config/detekt/detekt.yml"))
    baseline = file("detekt-baseline.xml")
    source.setFrom(files("src"))
    parallel = true
}

tasks.withType<Detekt>().configureEach {
    jvmTarget = "21"
    exclude("**/build/**")
    reports {
        html.required.set(true)
        xml.required.set(true)
        txt.required.set(false)
        sarif.required.set(false)
        md.required.set(false)
    }
}

// detekt runs on its own Kotlin compiler; keep Gradle from aligning it to the project's Kotlin.
configurations.matching { it.name == "detekt" }.configureEach {
    resolutionStrategy.eachDependency {
        if (requested.group == "org.jetbrains.kotlin") useVersion(getSupportedKotlinVersion())
    }
}

ktlint {
    version.set(libs.versions.ktlint)
    baseline.set(file("ktlint-baseline.xml"))
    filter {
        exclude { it.file.path.contains("/build/") }
    }
}

// Architecture tests (Konsist) in their own source set, isolated from the IntelliJ
// Platform test classpath (which bundles its own Kotlin compiler).
val konsistTest: SourceSet by sourceSets.creating {
    kotlin.srcDir(rootDir.parentFile.resolve("config/konsist/src"))
}

dependencies {
    "konsistTestImplementation"(libs.konsist)
    "konsistTestImplementation"(kotlin("test-junit5"))
    "konsistTestImplementation"(libs.test.junit)
    "konsistTestRuntimeOnly"(libs.test.junit.platform.launcher)
}

val konsistTestTask = tasks.register<Test>("konsistTest") {
    description = "Runs the Konsist architecture tests."
    group = "verification"
    testClassesDirs = konsistTest.output.classesDirs
    classpath = konsistTest.runtimeClasspath
    useJUnitPlatform()
}

tasks.named("check") { dependsOn(konsistTestTask) }

changelog {
    version = providers.gradleProperty("plugin.version").get()
    path = file("CHANGELOG.md").canonicalPath
    header = provider { "[${version.get()}] - ${date()}" }
    headerParserRegex = """(\d+\.\d+\.\d+)""".toRegex()
    itemPrefix = "-"
    keepUnreleasedSection = true
    unreleasedTerm = "[Unreleased]"
    groups = listOf("Added", "Changed", "Deprecated", "Removed", "Fixed", "Security")
    combinePreReleases = true
}

tasks {
    test {
        useJUnitPlatform()
    }

    // Ensure changelog is initialized before patching plugin.xml.
    patchPluginXml {

    }

    // When bumping pluginVersion in gradle.properties,
    // run `./gradlew patchChangelog` to move [Unreleased] → [x.y.z].
    patchChangelog {
        // No extra config needed — picks up version from changelog extension.
    }
}