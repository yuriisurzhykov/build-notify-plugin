package me.yuriisoft.buildnotify.mobile.designsystem.catalog

/**
 * Sample build data shown by the catalog. These are not interface texts (those come from
 * string resources) but realistic values a build produces: project names, task paths,
 * durations, compiler output.
 */
internal object CatalogSamples {
    const val PRODUCT_NAME = "Graphite"
    const val PAYMENT_SDK = "payment-sdk"
    const val SHOP_ANDROID = "shop-android"
    const val DESIGN_KIT = "design-kit"
    const val IDE_CONNECTION = "MyProject · Android Studio"
    const val CLAUDE_CODE = "Claude Code"
    const val CODEX = "Codex"

    const val TASK_COMPILE_PAYMENT = ":payment:compileKotlin"
    const val TASK_COMPILE_APP = ":app:compileDebugKotlin"
    const val TASK_ASSEMBLE = ":app:assembleDebug"
    const val TASK_ASSEMBLE_SHORT = "assembleDebug"
    const val TASK_INSTALL = ":app:installDebug"
    const val TASK_UNIT_TEST = ":app:testDebugUnitTest"
    const val TASK_PUBLISH = ":lib:publishToMavenLocal"

    const val ERROR_UNRESOLVED = "Unresolved reference: PaymentProcessor"
    const val ERROR_LOCATION = "PaymentService.kt:42"
    const val LOG_MERGE = ":app:mergeDebugResources UP_TO_DATE"
    const val LOG_KAPT = ":app:kaptDebugKotlin SUCCESS"
    const val LOG_UNUSED = "w: Parameter 'context' is never used"
    const val LOG_DEPRECATED = "w: 'getColor(Int): Int' is deprecated"
    const val LOG_UNRESOLVED = "e: Unresolved reference: PaymentProcessor"
    const val LOG_FAILED = "BUILD FAILED in 48s"

    const val DURATION_FAILED = "48s"
    const val DURATION_SUCCESS = "1m 45s"
    const val DURATION_TEST = "1m 52s"
    const val DURATION_PUBLISH = "12s"
    const val DURATION_INSTALL_ESTIMATE = "~2m 05s"
    const val DURATION_ASSEMBLE_ESTIMATE = "~1m 45s"
    const val ELAPSED = "1:12"
    const val TIME_SUCCESS = "14:02"
    const val TIME_FAILED = "13:51"
    const val TIME_CANCELLED = "13:40"
    const val OFFSET_COMPILE = "+7.0s"
    const val OFFSET_WARNING = "+21.6s"
    const val OFFSET_ERROR = "+24.1s"
    const val OFFSET_FAILED = "+48.0s"

    const val MINUTES_AGO = 2
    const val MORE_ERRORS = 2
    const val COMPILE_ERRORS = 3
    const val LOG_LINES = 218
    const val LOG_KILOBYTES = 14
    const val FILTER_ALL = 12
    const val FILTER_PROBLEMS = 6
    const val FILTER_ERRORS = 4
}
