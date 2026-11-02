package me.yuriisoft.buildnotify.mobile.feature.activebuilds.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import me.yuriisoft.buildnotify.mobile.designsystem.components.LogTone
import me.yuriisoft.buildnotify.mobile.designsystem.components.Text
import me.yuriisoft.buildnotify.mobile.designsystem.theme.BuildTone
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.domain.model.FinishStatus
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.domain.model.LogKind
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.generated.resources.Res
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.generated.resources.build_status_cancelled
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.generated.resources.build_status_failed
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.generated.resources.build_status_running
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.generated.resources.build_status_success
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.generated.resources.time_hours
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.generated.resources.time_minutes
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.generated.resources.time_now
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

internal fun FinishStatus?.tone(): BuildTone = when (this) {
    null                   -> BuildTone.Running
    FinishStatus.SUCCESS   -> BuildTone.Success
    FinishStatus.FAILED    -> BuildTone.Failure
    FinishStatus.CANCELLED -> BuildTone.Warning
}

internal fun FinishStatus?.label(): StringResource = when (this) {
    null                   -> Res.string.build_status_running
    FinishStatus.SUCCESS   -> Res.string.build_status_success
    FinishStatus.FAILED    -> Res.string.build_status_failed
    FinishStatus.CANCELLED -> Res.string.build_status_cancelled
}

internal fun LogKind.tone(): LogTone = when (this) {
    LogKind.TASK    -> LogTone.Default
    LogKind.WARNING -> LogTone.Warning
    LogKind.ERROR   -> LogTone.Error
}

/** `m:ss` for the live timer, `h:mm:ss` past an hour. */
internal fun formatTimer(elapsedMs: Long): String {
    val total = elapsedMs.coerceAtLeast(0) / 1_000
    val h = total / 3_600
    val m = (total % 3_600) / 60
    val s = (total % 60).toString().padStart(2, '0')
    return if (h > 0) "$h:${m.toString().padStart(2, '0')}:$s" else "$m:$s"
}

/** "сейчас", "5 мин назад", "2 ч назад" — re-evaluated every second while shown. */
@Composable
internal fun relativeTime(atMillis: Long): String {
    val minutes = rememberElapsedMs(atMillis) / 60_000
    return when {
        minutes < 1  -> stringResource(Res.string.time_now)
        minutes < 60 -> stringResource(Res.string.time_minutes, minutes.toInt())
        else         -> stringResource(Res.string.time_hours, (minutes / 60).toInt())
    }
}

/**
 * Text whose characters roll like a split-flap board: only the characters that
 * changed slide in from above. Used for the running-build timer.
 */
@Composable
internal fun RollingText(
    text: String,
    style: TextStyle,
    modifier: Modifier = Modifier,
) {
    Row(modifier) {
        text.forEachIndexed { index, char ->
            // Keying by position-from-end keeps seconds rolling in place when "9:59" becomes "10:00".
            androidx.compose.runtime.key(text.length - index) {
                AnimatedContent(
                    targetState = char,
                    transitionSpec = {
                        (slideInVertically { -it } + fadeIn()) togetherWith (slideOutVertically { it } + fadeOut())
                    },
                    label = "rollingChar",
                ) { value ->
                    Text(value.toString(), style = style)
                }
            }
        }
    }
}
