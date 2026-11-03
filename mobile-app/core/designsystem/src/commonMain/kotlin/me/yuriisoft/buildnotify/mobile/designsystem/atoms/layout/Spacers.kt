package me.yuriisoft.buildnotify.mobile.designsystem.atoms.layout

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp

/** Horizontal gap helper for rows. */
@Composable
fun HSpace(width: Dp) = Spacer(Modifier.width(width))

/** Vertical gap helper for columns. */
@Composable
fun VSpace(height: Dp) = Spacer(Modifier.height(height))
