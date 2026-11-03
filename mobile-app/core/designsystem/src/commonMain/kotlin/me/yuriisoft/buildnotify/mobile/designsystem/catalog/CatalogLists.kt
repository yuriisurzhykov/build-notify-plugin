package me.yuriisoft.buildnotify.mobile.designsystem.catalog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import me.yuriisoft.buildnotify.mobile.designsystem.molecules.console.LogLine
import me.yuriisoft.buildnotify.mobile.designsystem.molecules.console.LogTone
import me.yuriisoft.buildnotify.mobile.designsystem.molecules.fields.FilterChip
import me.yuriisoft.buildnotify.mobile.designsystem.molecules.texts.SectionHeader
import me.yuriisoft.buildnotify.mobile.designsystem.organisms.console.Console
import me.yuriisoft.buildnotify.mobile.designsystem.organisms.lists.HistoryRow
import me.yuriisoft.buildnotify.mobile.designsystem.organisms.lists.RunTaskRow
import me.yuriisoft.buildnotify.mobile.designsystem.particles.theme.BuildTone
import me.yuriisoft.buildnotify.mobile.designsystem.particles.theme.GraphiteTheme

@Composable
internal fun HistorySection() {
    Section("История") {
        Column {
            SectionHeader("Сегодня")
            HistoryRow(BuildTone.Success, "Успешно", "shop-android", ":app:assembleDebug", "1m 45s", "14:02")
            HistoryRow(
                BuildTone.Failure,
                "Ошибка",
                "shop-android",
                ":app:testDebugUnitTest",
                "1m 52s",
                "13:51",
                tag = "агент",
            )
            HistoryRow(
                BuildTone.Warning,
                "Отменена",
                "design-kit",
                ":lib:publishToMavenLocal",
                "12s",
                "13:40",
                showDivider = false,
            )
        }
        Column {
            SectionHeader("Запустить снова")
            RunTaskRow(":app:assembleDebug", "~1m 45s", onRun = {})
            RunTaskRow(":app:installDebug", "~2m 05s", onRun = {}, showDivider = false)
        }
    }
}

@Composable
internal fun LogSection() {
    val spacing = GraphiteTheme.spacing
    var filter by remember { mutableStateOf(0) }
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
}
