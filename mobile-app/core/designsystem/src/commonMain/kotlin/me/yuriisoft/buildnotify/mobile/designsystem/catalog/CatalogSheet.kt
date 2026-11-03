package me.yuriisoft.buildnotify.mobile.designsystem.catalog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import me.yuriisoft.buildnotify.mobile.designsystem.components.ChoiceCard
import me.yuriisoft.buildnotify.mobile.designsystem.components.PrimaryButton
import me.yuriisoft.buildnotify.mobile.designsystem.components.SettingRow
import me.yuriisoft.buildnotify.mobile.designsystem.components.SheetSurface
import me.yuriisoft.buildnotify.mobile.designsystem.components.Switch
import me.yuriisoft.buildnotify.mobile.designsystem.components.Text
import me.yuriisoft.buildnotify.mobile.designsystem.icons.GraphiteIcons
import me.yuriisoft.buildnotify.mobile.designsystem.theme.GraphiteTheme

@Composable
internal fun SheetSection() {
    val spacing = GraphiteTheme.spacing
    val colors = GraphiteTheme.colors
    val type = GraphiteTheme.typography
    var fullLog by remember { mutableStateOf(false) }
    var agent by remember { mutableStateOf(0) }
    Section("Шторка · передать агенту", padded = false) {
        SheetSurface {
            Column(verticalArrangement = Arrangement.spacedBy(spacing.xs)) {
                Text("Передать агенту", style = type.headline)
                Text("payment-sdk · 3 ошибки компиляции", style = type.bodySmall, color = colors.contentSecondary)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(spacing.s)) {
                ChoiceCard("Claude Code", "последний", selected = agent == 0, onClick = { agent = 0 }, modifier = Modifier.weight(1f))
                ChoiceCard("Codex", "установлен", selected = agent == 1, onClick = { agent = 1 }, modifier = Modifier.weight(1f))
                ChoiceCard("Копировать", "любой агент", selected = agent == 2, onClick = { agent = 2 }, modifier = Modifier.weight(1f))
            }
            SettingRow("Приложить полный лог", onClick = { fullLog = !fullLog }, subtitle = "218 строк, 14 КБ") {
                Switch(checked = fullLog, onCheckedChange = { fullLog = it })
            }
            PrimaryButton("Открыть в Claude Code", onClick = {}, modifier = Modifier.fillMaxWidth(), trailingIcon = GraphiteIcons.ArrowRight)
        }
    }
}
