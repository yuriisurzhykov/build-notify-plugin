package me.yuriisoft.buildnotify.mobile.designsystem.molecules.status

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import me.yuriisoft.buildnotify.mobile.designsystem.atoms.texts.Text
import me.yuriisoft.buildnotify.mobile.designsystem.particles.style.styled
import me.yuriisoft.buildnotify.mobile.designsystem.particles.theme.BuildTone
import me.yuriisoft.buildnotify.mobile.designsystem.particles.theme.GraphiteTheme

/** Small source tag in list rows — "agent". */
@Composable
fun Tag(
    text: String,
    modifier: Modifier = Modifier,
    tone: BuildTone = BuildTone.Running,
) {
    val status = GraphiteTheme.status(tone)
    Text(
        text = text,
        modifier = modifier.styled(rememberTagStyle(tone)),
        style = GraphiteTheme.typography.tag,
        color = status.main,
    )
}
