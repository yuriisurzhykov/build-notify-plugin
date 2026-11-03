package me.yuriisoft.buildnotify.mobile.designsystem.molecules.buttons

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector

@Composable
fun DangerButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    leadingIcon: ImageVector? = null,
    enabled: Boolean = true,
) = Button(text, onClick, modifier, ButtonVariant.Danger, ButtonSize.Regular, leadingIcon, null, enabled)
