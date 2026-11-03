package me.yuriisoft.buildnotify.mobile.designsystem.molecules.fields

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import me.yuriisoft.buildnotify.mobile.designsystem.generated.resources.Res
import me.yuriisoft.buildnotify.mobile.designsystem.generated.resources.state_off
import me.yuriisoft.buildnotify.mobile.designsystem.generated.resources.state_on
import me.yuriisoft.buildnotify.mobile.designsystem.particles.style.styledClickable
import me.yuriisoft.buildnotify.mobile.designsystem.particles.style.styledToggle
import org.jetbrains.compose.resources.stringResource

/** On/off switch, 46×28. Prefer putting it in a [SettingRow] so the whole row toggles. */
@Composable
fun Switch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    stateDescriptionOn: String = stringResource(Res.string.state_on),
    stateDescriptionOff: String = stringResource(Res.string.state_off),
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
