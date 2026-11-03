package me.yuriisoft.buildnotify.mobile.designsystem.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.unit.dp
import me.yuriisoft.buildnotify.mobile.designsystem.theme.GraphiteTheme

/**
 * Thin progress track. `progress = null` animates an indeterminate segment —
 * Gradle does not report a total task count, so this is the default for builds.
 */
@Composable
fun ProgressBar(
    progress: Float?,
    modifier: Modifier = Modifier,
    color: Color = GraphiteTheme.colors.running.main,
    trackColor: Color = GraphiteTheme.colors.content.copy(alpha = 0.1f),
) {
    val transition = rememberInfiniteTransition(label = "progress")
    // Determinate bars ignore the phase; the transition is cheap and keeps the call stable.
    val phase by transition.animateFloat(
        initialValue = -0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1_400, easing = LinearEasing), RepeatMode.Restart),
        label = "phase",
    )
    val range = if (progress == null) ProgressBarRangeInfo.Indeterminate
    else ProgressBarRangeInfo(progress.coerceIn(0f, 1f), 0f..1f)

    Canvas(
        modifier
            .fillMaxWidth()
            .height(GraphiteTheme.sizes.progressTrack)
            .semantics { progressBarRangeInfo = range },
    ) {
        val radius = CornerRadius(size.height / 2, size.height / 2)
        drawRoundRect(trackColor, cornerRadius = radius)
        if (progress != null) {
            drawRoundRect(
                color,
                size = Size(size.width * progress.coerceIn(0f, 1f), size.height),
                cornerRadius = radius,
            )
        } else {
            val start = (size.width * phase).coerceAtLeast(0f)
            val end = (size.width * (phase + 0.3f)).coerceAtMost(size.width)
            if (end > start) {
                drawRoundRect(
                    color,
                    topLeft = Offset(start, 0f),
                    size = Size(end - start, size.height),
                    cornerRadius = radius,
                )
            }
        }
    }
}

/** Filter chip: "Все 12", "Ошибки 4". Selected is a solid inverse pill. */
@Composable
fun FilterChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    count: Int? = null,
) {
    val colors = GraphiteTheme.colors
    val content = if (selected) colors.onInverse else colors.contentSecondary
    Row(
        modifier = modifier
            .semantics { this.selected = selected }
            .styledClickable(rememberChipStyle(), onClick = onClick, selected = selected, role = Role.Tab),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(text, style = GraphiteTheme.typography.chip, color = content)
        if (count != null) Text(
            count.toString(),
            style = GraphiteTheme.typography.mono,
            color = content.copy(alpha = 0.8f),
        )
    }
}

/** On/off switch, 46×28. Prefer putting it in a [SettingRow] so the whole row toggles. */
@Composable
fun Switch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    stateDescriptionOn: String = "On",
    stateDescriptionOff: String = "Off",
) {
    val styles = rememberSwitchStyles()
    Box(
        modifier = modifier
            .semantics { stateDescription = if (checked) stateDescriptionOn else stateDescriptionOff }
            .styledClickable(
                styles.track,
                onClick = { onCheckedChange(!checked) },
                checked = checked,
                role = Role.Switch,
            ),
    ) {
        Box(Modifier.styledToggle(styles.thumb, checked))
    }
}

/**
 * Selectable option card for one-of-N choices with a hint, e.g. which agent to hand off to.
 */
@Composable
fun ChoiceCard(
    title: String,
    hint: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = GraphiteTheme.colors
    val content = if (selected) colors.onInverse else colors.content
    Column(
        modifier = modifier
            .semantics { this.selected = selected }
            .styledClickable(
                rememberChoiceCardStyle(),
                onClick = onClick,
                selected = selected,
                role = Role.RadioButton,
            ),
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Text(title, style = GraphiteTheme.typography.chip, color = content, maxLines = 1)
        Text(hint, style = GraphiteTheme.typography.caption, color = content.copy(alpha = 0.75f), maxLines = 1)
    }
}
