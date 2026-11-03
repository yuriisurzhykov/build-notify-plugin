package me.yuriisoft.buildnotify.mobile.feature.activebuilds.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import me.yuriisoft.buildnotify.mobile.designsystem.particles.theme.BuildTone
import me.yuriisoft.buildnotify.mobile.designsystem.particles.theme.GraphiteTheme
import me.yuriisoft.buildnotify.mobile.designsystem.particles.theme.StatusStyle
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.domain.model.FinishStatus
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.generated.resources.Res
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.generated.resources.build_status_cancelled
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.generated.resources.build_status_failed
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.generated.resources.build_status_running
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.generated.resources.build_status_success
import org.jetbrains.compose.resources.StringResource

/**
 * How a build status looks, resolved in one place: tone (and through it the icon and every
 * colour, via [StatusStyle]) and the label. Screens never map a status to visuals themselves.
 */
enum class BuildStatusAppearance(
    val tone: BuildTone,
    val label: StringResource,
) {
    RUNNING(BuildTone.Running, Res.string.build_status_running),
    SUCCESS(BuildTone.Success, Res.string.build_status_success),
    FAILED(BuildTone.Failure, Res.string.build_status_failed),
    CANCELLED(BuildTone.Warning, Res.string.build_status_cancelled),
    ;

    /** Icon and colours of this status from the design system. */
    val style: StatusStyle
        @Composable @ReadOnlyComposable get() = GraphiteTheme.status(tone)

    companion object {
        /** `null` means the build has not finished yet. */
        fun of(status: FinishStatus?): BuildStatusAppearance = when (status) {
            null                   -> RUNNING
            FinishStatus.SUCCESS   -> SUCCESS
            FinishStatus.FAILED    -> FAILED
            FinishStatus.CANCELLED -> CANCELLED
        }
    }
}
