package me.yuriisoft.buildnotify.mobile.designsystem.particles.motion

import androidx.compose.animation.animateColorAsState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import me.yuriisoft.buildnotify.mobile.designsystem.particles.theme.BuildTone
import me.yuriisoft.buildnotify.mobile.designsystem.particles.theme.GraphiteTheme
import me.yuriisoft.buildnotify.mobile.designsystem.particles.theme.StatusStyle
import me.yuriisoft.buildnotify.mobile.designsystem.particles.theme.ToneColors

/**
 * [GraphiteTheme.status] whose colours glide to the new tone instead of jumping when
 * [tone] changes. The icon switches immediately.
 */
@Composable
fun animateStatus(tone: BuildTone): StatusStyle {
    val target = GraphiteTheme.status(tone)
    val motion = GraphiteTheme.motion
    val main by animateColorAsState(target.main, motion.slow(), label = "statusMain")
    val container by animateColorAsState(target.container, motion.slow(), label = "statusContainer")
    val hero by animateColorAsState(target.hero, motion.slow(), label = "statusHero")
    val onMain by animateColorAsState(target.onMain, motion.slow(), label = "statusOnMain")
    return target.copy(colors = ToneColors(main = main, container = container, hero = hero, onMain = onMain))
}
