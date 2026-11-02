package me.yuriisoft.buildnotify.mobile.designsystem.catalog

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import me.yuriisoft.buildnotify.mobile.designsystem.components.Button
import me.yuriisoft.buildnotify.mobile.designsystem.components.ButtonVariant
import me.yuriisoft.buildnotify.mobile.designsystem.components.ChoiceCard
import me.yuriisoft.buildnotify.mobile.designsystem.components.ConnectionBadge
import me.yuriisoft.buildnotify.mobile.designsystem.components.Console
import me.yuriisoft.buildnotify.mobile.designsystem.components.DangerButton
import me.yuriisoft.buildnotify.mobile.designsystem.components.FilterChip
import me.yuriisoft.buildnotify.mobile.designsystem.components.HeroPanel
import me.yuriisoft.buildnotify.mobile.designsystem.components.HistoryRow
import me.yuriisoft.buildnotify.mobile.designsystem.components.IconButton
import me.yuriisoft.buildnotify.mobile.designsystem.components.InsetPanel
import me.yuriisoft.buildnotify.mobile.designsystem.components.IssueText
import me.yuriisoft.buildnotify.mobile.designsystem.components.LogLine
import me.yuriisoft.buildnotify.mobile.designsystem.components.LogTone
import me.yuriisoft.buildnotify.mobile.designsystem.components.OutlineButton
import me.yuriisoft.buildnotify.mobile.designsystem.components.PrimaryButton
import me.yuriisoft.buildnotify.mobile.designsystem.components.ProgressBar
import me.yuriisoft.buildnotify.mobile.designsystem.components.RunTaskRow
import me.yuriisoft.buildnotify.mobile.designsystem.components.ScreenSurface
import me.yuriisoft.buildnotify.mobile.designsystem.components.SectionHeader
import me.yuriisoft.buildnotify.mobile.designsystem.components.SecondaryButton
import me.yuriisoft.buildnotify.mobile.designsystem.components.SettingRow
import me.yuriisoft.buildnotify.mobile.designsystem.components.SheetSurface
import me.yuriisoft.buildnotify.mobile.designsystem.components.StatusBadge
import me.yuriisoft.buildnotify.mobile.designsystem.components.StatusLabel
import me.yuriisoft.buildnotify.mobile.designsystem.components.StatusPill
import me.yuriisoft.buildnotify.mobile.designsystem.components.Switch
import me.yuriisoft.buildnotify.mobile.designsystem.components.Tag
import me.yuriisoft.buildnotify.mobile.designsystem.components.Text
import me.yuriisoft.buildnotify.mobile.designsystem.icons.GraphiteIcons
import me.yuriisoft.buildnotify.mobile.designsystem.theme.BuildTone
import me.yuriisoft.buildnotify.mobile.designsystem.theme.GraphiteTheme

/**
 * Living reference of every Graphite token and component, in the current theme.
 * Host it on any platform inside [GraphiteTheme][me.yuriisoft.buildnotify.mobile.designsystem.theme.GraphiteTheme]
 * (e.g. a desktop `Window { GraphiteTheme { GraphiteCatalog() } }`).
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun GraphiteCatalog(modifier: Modifier = Modifier) {
    val spacing = GraphiteTheme.spacing
    val colors = GraphiteTheme.colors
    val type = GraphiteTheme.typography

    var filter by remember { mutableStateOf(0) }
    var fullLog by remember { mutableStateOf(false) }
    var agent by remember { mutableStateOf(0) }

    ScreenSurface(modifier.fillMaxSize()) {
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(vertical = spacing.xxxl),
            verticalArrangement = Arrangement.spacedBy(spacing.section),
        ) {
            Column(Modifier.padding(horizontal = spacing.screen), verticalArrangement = Arrangement.spacedBy(spacing.s)) {
                Text("Graphite", style = type.display)
                Text(
                    "Тихий интерфейс, громкие сигналы. Цвет появляется только там, где есть статус.",
                    style = type.bodySmall,
                    color = colors.contentSecondary,
                )
            }

            Section("Цвет") {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(spacing.s), verticalArrangement = Arrangement.spacedBy(spacing.s)) {
                    Swatch("background", colors.background)
                    Swatch("surface", colors.surface)
                    Swatch("raised", colors.surfaceRaised)
                    Swatch("console", colors.console)
                    Swatch("running", colors.running.main)
                    Swatch("success", colors.success.main)
                    Swatch("failure", colors.failure.main)
                    Swatch("warning", colors.warning.main)
                }
            }

            Section("Типографика") {
                TypeSample("display", type.display, "Всё собрано")
                TypeSample("headline", type.headline, "Передать агенту")
                TypeSample("title", type.title, "shop-android")
                TypeSample("body", type.body, "Unresolved reference: PaymentProcessor")
                TypeSample("meta", type.meta, "Успешно · 1m 45s")
                TypeSample("label", type.label, "Сегодня")
                TypeSample("timer", type.timer, "1:12")
                TypeSample("mono", type.mono, ":app:compileDebugKotlin")
                TypeSample("log", type.log, "w: Parameter 'context' is never used")
            }

            Section("Статусы") {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(spacing.s), verticalArrangement = Arrangement.spacedBy(spacing.s)) {
                    StatusPill(BuildTone.Running, "Идёт")
                    StatusPill(BuildTone.Success, "Успешно")
                    StatusPill(BuildTone.Failure, "Ошибка")
                    StatusPill(BuildTone.Warning, "Отменена")
                    Tag("агент")
                }
                Row(horizontalArrangement = Arrangement.spacedBy(spacing.m)) {
                    StatusBadge(BuildTone.Success, contentDescription = "Успешно")
                    StatusBadge(BuildTone.Failure, contentDescription = "Ошибка")
                    StatusBadge(BuildTone.Running, contentDescription = "Идёт")
                }
                ConnectionBadge("MyProject · Android Studio", connected = true)
            }

            Section("Кнопки") {
                PrimaryButton("Передать агенту", onClick = {}, modifier = Modifier.fillMaxWidth(), trailingIcon = GraphiteIcons.ArrowRight)
                Row(horizontalArrangement = Arrangement.spacedBy(spacing.s)) {
                    OutlineButton("Перезапустить", onClick = {}, modifier = Modifier.weight(1f), leadingIcon = GraphiteIcons.Rerun)
                    SecondaryButton("Лог", onClick = {}, modifier = Modifier.weight(1f), leadingIcon = GraphiteIcons.Terminal)
                }
                Row(horizontalArrangement = Arrangement.spacedBy(spacing.s)) {
                    DangerButton("Остановить", onClick = {}, modifier = Modifier.weight(1f), leadingIcon = GraphiteIcons.Stop)
                    Button("Недоступно", onClick = {}, modifier = Modifier.weight(1f), variant = ButtonVariant.Secondary, enabled = false)
                }
                Row {
                    IconButton(GraphiteIcons.ArrowBack, contentDescription = "Назад", onClick = {})
                    IconButton(GraphiteIcons.Copy, contentDescription = "Скопировать лог", onClick = {})
                    IconButton(GraphiteIcons.Settings, contentDescription = "Настройки", onClick = {})
                }
            }

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
                    PrimaryButton("Передать агенту", onClick = {}, modifier = Modifier.fillMaxWidth(), trailingIcon = GraphiteIcons.ArrowRight)
                }
            }

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
                        DangerButton("Остановить", onClick = {}, modifier = Modifier.weight(1f), leadingIcon = GraphiteIcons.Stop)
                        SecondaryButton("Лог", onClick = {}, modifier = Modifier.weight(1f), leadingIcon = GraphiteIcons.Terminal)
                    }
                }
            }

            Section("История") {
                Column {
                    SectionHeader("Сегодня")
                    HistoryRow(BuildTone.Success, "Успешно", "shop-android", ":app:assembleDebug", "1m 45s", "14:02")
                    HistoryRow(BuildTone.Failure, "Ошибка", "shop-android", ":app:testDebugUnitTest", "1m 52s", "13:51", tag = "агент")
                    HistoryRow(BuildTone.Warning, "Отменена", "design-kit", ":lib:publishToMavenLocal", "12s", "13:40", showDivider = false)
                }
                Column {
                    SectionHeader("Запустить снова")
                    RunTaskRow(":app:assembleDebug", "~1m 45s", onRun = {})
                    RunTaskRow(":app:installDebug", "~2m 05s", onRun = {}, showDivider = false)
                }
            }

            Section("Лог") {
                Row(horizontalArrangement = Arrangement.spacedBy(spacing.s)) {
                    FilterChip("Все", selected = filter == 0, onClick = { filter = 0 }, count = 12)
                    FilterChip("Проблемы", selected = filter == 1, onClick = { filter = 1 }, count = 6)
                    FilterChip("Ошибки", selected = filter == 2, onClick = { filter = 2 }, count = 4)
                }
                Console {
                    if (filter == 0) LogLine(":payment:compileKotlin", offset = "+7.0s")
                    if (filter < 2) LogLine("w: 'getColor(Int): Int' is deprecated", tone = LogTone.Warning, offset = "+21.6s")
                    LogLine("e: Unresolved reference: PaymentProcessor", tone = LogTone.Error, offset = "+24.1s")
                    LogLine("BUILD FAILED in 48s", tone = LogTone.Error, offset = "+48.0s")
                }
            }

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
    }
}

@Composable
private fun Section(
    title: String,
    padded: Boolean = true,
    content: @Composable ColumnScope.() -> Unit,
) {
    val spacing = GraphiteTheme.spacing
    Column(verticalArrangement = Arrangement.spacedBy(spacing.m)) {
        SectionHeader(title, Modifier.padding(horizontal = spacing.screen))
        Column(
            modifier = if (padded) Modifier.padding(horizontal = spacing.screen) else Modifier,
            verticalArrangement = Arrangement.spacedBy(spacing.m),
            content = content,
        )
    }
}

@Composable
private fun Swatch(name: String, color: Color) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Box(
            Modifier
                .size(width = 72.dp, height = 48.dp)
                .background(color, GraphiteTheme.shapes.control)
                .border(GraphiteTheme.sizes.hairline, GraphiteTheme.colors.line, GraphiteTheme.shapes.control),
        )
        Text(name, style = GraphiteTheme.typography.caption, color = GraphiteTheme.colors.contentSecondary)
    }
}

@Composable
private fun TypeSample(name: String, style: TextStyle, sample: String) {
    Row(horizontalArrangement = Arrangement.spacedBy(GraphiteTheme.spacing.l)) {
        Text(name, modifier = Modifier.width(72.dp), style = GraphiteTheme.typography.log, color = GraphiteTheme.colors.contentTertiary)
        Text(sample, style = style, maxLines = 1)
    }
}
