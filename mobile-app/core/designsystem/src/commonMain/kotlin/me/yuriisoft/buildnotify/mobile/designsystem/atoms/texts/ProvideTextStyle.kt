package me.yuriisoft.buildnotify.mobile.designsystem.atoms.texts

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.text.TextStyle
import me.yuriisoft.buildnotify.mobile.designsystem.particles.theme.LocalGraphiteTextStyle

/** Provides [LocalGraphiteTextStyle] for a subtree, e.g. mono for a block of build output. */
@Composable
fun ProvideTextStyle(
    style: TextStyle,
    content: @Composable () -> Unit,
) {
    CompositionLocalProvider(LocalGraphiteTextStyle provides style, content = content)
}
