package me.yuriisoft.buildnotify.mobile.feature.activebuilds.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.domain.model.FinishStatus
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.generated.resources.Res
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.generated.resources.build_status_cancelled
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.generated.resources.build_status_failed
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.generated.resources.build_status_running
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.generated.resources.build_status_success
import me.yuriisoft.buildnotify.mobile.ui.components.foundation.Badge
import me.yuriisoft.buildnotify.mobile.ui.components.foundation.Text
import me.yuriisoft.buildnotify.mobile.ui.resource.textResource
import me.yuriisoft.buildnotify.mobile.ui.theme.BuildNotifyTheme
import me.yuriisoft.buildnotify.mobile.ui.theme.color.semantic.StatusRole
import org.jetbrains.compose.resources.StringResource

/** Status pill. `null` [status] means the build is still running. */
@Composable
internal fun BuildStatusBadge(
    status: FinishStatus?,
    modifier: Modifier = Modifier,
) {
    val statuses = BuildNotifyTheme.colors.status
    val (role: StatusRole, label: StringResource) = when (status) {
        null                  -> statuses.info to Res.string.build_status_running
        FinishStatus.SUCCESS   -> statuses.success to Res.string.build_status_success
        FinishStatus.FAILED    -> statuses.error to Res.string.build_status_failed
        FinishStatus.CANCELLED -> statuses.warning to Res.string.build_status_cancelled
    }
    Badge(
        modifier = modifier,
        containerColor = role.container,
        contentColor = role.onContainer,
    ) {
        Text(
            text = textResource(label),
            style = BuildNotifyTheme.typography.labelSmall,
        )
    }
}
