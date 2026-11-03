@file:Suppress("MaxLineLength") // allowlist entries are full paths

package me.yuriisoft.buildnotify.mobile.konsist

/**
 * Violations that existed when a rule was introduced. They are tolerated, not accepted:
 * the lists may only shrink (see Ratchet). Fix a file, then delete its line here.
 *
 * `core/ui` and `feature/ui-catalog` are the pre-Graphite design system and are scheduled
 * for removal; `core/toast` still renders with legacy primitives.
 */
object KnownViolations {

    val manyTypesPerFile = listOf(
        "core/toast/src/commonMain/kotlin/me/yuriisoft/buildnotify/mobile/toast/ui/animation/AuroraGradientBrush.kt",
        "core/toast/src/commonMain/kotlin/me/yuriisoft/buildnotify/mobile/toast/ui/animation/PullRevealShape.kt",
        "core/ui/src/commonMain/kotlin/me/yuriisoft/buildnotify/mobile/ui/components/layout/FlatRow.kt",
        "core/ui/src/commonMain/kotlin/me/yuriisoft/buildnotify/mobile/ui/resource/StableImage.kt",
        "core/ui/src/commonMain/kotlin/me/yuriisoft/buildnotify/mobile/ui/theme/typography/Type.kt",
    )

    val longFiles = listOf(
        "app/src/iosMain/kotlin/me/yuriisoft/buildnotify/mobile/tls/SelfSignedCertificateFactory.kt",
        "core/network/src/commonMain/kotlin/me/yuriisoft/buildnotify/mobile/network/connection/ConnectionOrchestrator.kt",
        "core/ui/src/commonMain/kotlin/me/yuriisoft/buildnotify/mobile/ui/components/layout/FlatRow.kt",
        "core/ui/src/commonMain/kotlin/me/yuriisoft/buildnotify/mobile/ui/components/progress/mode/circular/CircularProgressMode.kt",
        "core/ui/src/commonMain/kotlin/me/yuriisoft/buildnotify/mobile/ui/components/progress/mode/linear/LinearProgressMode.kt",
        "feature/discovery/src/commonMain/kotlin/me/yuriisoft/buildnotify/mobile/feature/discovery/presentation/DiscoveryViewModel.kt",
        "feature/ui-catalog/src/commonMain/kotlin/com/yuriisurzhykov/buildnotifier/feature/catalog/CatalogContent.kt",
    )

    val longTestFiles = listOf(
        "feature/active-builds/src/commonTest/kotlin/me/yuriisoft/buildnotify/mobile/feature/activebuilds/data/mapper/BuildEventMapperTest.kt",
    )

    val legacyUiImports = listOf(
        "core/toast/src/commonMain/kotlin/me/yuriisoft/buildnotify/mobile/toast/ui/ToastBanner.kt: me.yuriisoft.buildnotify.mobile.ui.components.icon.Icon",
        "core/toast/src/commonMain/kotlin/me/yuriisoft/buildnotify/mobile/toast/ui/ToastBanner.kt: me.yuriisoft.buildnotify.mobile.ui.theme.color.semantic.StatusRole",
        "core/toast/src/commonMain/kotlin/me/yuriisoft/buildnotify/mobile/toast/ui/animation/AuroraGradientBrush.kt: me.yuriisoft.buildnotify.mobile.ui.theme.color.semantic.StatusRole",
    )
}
