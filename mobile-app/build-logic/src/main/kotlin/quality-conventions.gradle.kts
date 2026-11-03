import io.gitlab.arturbosch.detekt.Detekt
import io.gitlab.arturbosch.detekt.getSupportedKotlinVersion

/*
 * Static analysis for every module: detekt (structure, complexity, naming) and ktlint
 * (JetBrains formatting, from the repository's .editorconfig). Both are wired into `check`.
 *
 * Shared rules live at the repository root (`config/detekt/detekt.yml`, `.editorconfig`)
 * and are the same for ide-plugin. Existing findings are frozen per module in
 * `detekt-baseline.xml` / `ktlint-baseline.xml`; new code must be clean.
 */
plugins {
    id("io.gitlab.arturbosch.detekt")
    id("org.jlleitschuh.gradle.ktlint")
}

val catalog = the<VersionCatalogsExtension>().named("libs")
val repositoryRoot = rootDir.parentFile

detekt {
    buildUponDefaultConfig = true
    config.setFrom(repositoryRoot.resolve("config/detekt/detekt.yml"))
    baseline = file("detekt-baseline.xml")
    // Kotlin Multiplatform: analyse every source set (commonMain, androidMain, iosMain, tests).
    source.setFrom(files("src"))
    parallel = true
}

tasks.withType<Detekt>().configureEach {
    exclude("**/build/**", "**/generated/**")
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
    version.set(catalog.findVersion("ktlint").get().requiredVersion)
    baseline.set(file("ktlint-baseline.xml"))
    filter {
        exclude { it.file.path.contains("/build/") }
    }
}
