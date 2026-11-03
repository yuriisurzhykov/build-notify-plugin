package me.yuriisoft.buildnotify.mobile.designsystem.catalog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import me.yuriisoft.buildnotify.mobile.designsystem.components.Console
import me.yuriisoft.buildnotify.mobile.designsystem.components.DangerButton
import me.yuriisoft.buildnotify.mobile.designsystem.components.HeroPanel
import me.yuriisoft.buildnotify.mobile.designsystem.components.InsetPanel
import me.yuriisoft.buildnotify.mobile.designsystem.components.IssueText
import me.yuriisoft.buildnotify.mobile.designsystem.components.LogLine
import me.yuriisoft.buildnotify.mobile.designsystem.components.LogTone
import me.yuriisoft.buildnotify.mobile.designsystem.components.PrimaryButton
import me.yuriisoft.buildnotify.mobile.designsystem.components.ProgressBar
import me.yuriisoft.buildnotify.mobile.designsystem.components.SecondaryButton
import me.yuriisoft.buildnotify.mobile.designsystem.components.StatusLabel
import me.yuriisoft.buildnotify.mobile.designsystem.components.Text
import me.yuriisoft.buildnotify.mobile.designsystem.icons.GraphiteIcons
import me.yuriisoft.buildnotify.mobile.designsystem.theme.BuildTone
import me.yuriisoft.buildnotify.mobile.designsystem.theme.GraphiteTheme

@Composable
internal fun FailedHeroSection() {
    val colors = GraphiteTheme.colors
    val type = GraphiteTheme.typography
    Section("Герой · сборка упала", padded = false) {
        HeroPanel(BuildTone.Failure) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                StatusLabel(BuildTone.Failure, "Сборка упала · 2 мин назад")
                Text("payment-sdk не собрался", style = type.display)
                Text(":payment:compileKotlin · 48s", style = type.mono, color = colors.contentSecondary)
            }
            InsetPanel {
                IssueText("Unresolved reference: PaymentProcessor", "PaymentService.kt:42")
                Text("и ещё 2 ошибки компиляции", style = type.meta, color = colors.contentSecondary)
            }
            PrimaryButton(
                "Передать агенту",
                onClick = {},
                modifier = Modifier.fillMaxWidth(),
                trailingIcon = GraphiteIcons.ArrowRight,
            )
        }
    }
}

@Composable
internal fun RunningHeroSection() {
    val spacing = GraphiteTheme.spacing
    val colors = GraphiteTheme.colors
    val type = GraphiteTheme.typography
    Section("Герой · идёт сборка", padded = false) {
        HeroPanel(BuildTone.Running) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                StatusLabel(BuildTone.Running, "Идёт сборка")
                Text("1:12", style = type.timer)
                Text("shop-android · assembleDebug", style = type.headline)
            }
            Column(verticalArrangement = Arrangement.spacedBy(spacing.s)) {
                Text(":app:compileDebugKotlin", style = type.mono, color = colors.running.main)
                ProgressBar(progress = null)
            }
            Console(translucent = true) {
                LogLine(":app:mergeDebugResources UP_TO_DATE", tone = LogTone.Muted, singleLine = true)
                LogLine(":app:kaptDebugKotlin SUCCESS", tone = LogTone.Muted, singleLine = true)
                LogLine("w: Parameter 'context' is never used", tone = LogTone.Warning, singleLine = true)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(spacing.s)) {
                DangerButton(
                    "Остановить",
                    onClick = {},
                    modifier = Modifier.weight(1f),
                    leadingIcon = GraphiteIcons.Stop,
                )
                SecondaryButton(
                    "Лог",
                    onClick = {},
                    modifier = Modifier.weight(1f),
                    leadingIcon = GraphiteIcons.Terminal,
                )
            }
        }
    }
}
