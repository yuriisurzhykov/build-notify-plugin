package me.yuriisoft.buildnotify.mobile.feature.activebuilds.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import me.yuriisoft.buildnotify.mobile.designsystem.molecules.status.StatusPill
import org.jetbrains.compose.resources.stringResource

/** Status pill for a build: tone and label both come from [BuildStatusAppearance]. */
@Composable
internal fun BuildStatusPill(
    appearance: BuildStatusAppearance,
    modifier: Modifier = Modifier,
) {
    StatusPill(tone = appearance.tone, label = stringResource(appearance.label), modifier = modifier)
}
