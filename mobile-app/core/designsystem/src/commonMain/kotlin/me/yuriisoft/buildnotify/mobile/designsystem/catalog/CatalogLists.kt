package me.yuriisoft.buildnotify.mobile.designsystem.catalog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import me.yuriisoft.buildnotify.mobile.designsystem.generated.resources.Res
import me.yuriisoft.buildnotify.mobile.designsystem.generated.resources.catalog_filter_all
import me.yuriisoft.buildnotify.mobile.designsystem.generated.resources.catalog_filter_errors
import me.yuriisoft.buildnotify.mobile.designsystem.generated.resources.catalog_filter_problems
import me.yuriisoft.buildnotify.mobile.designsystem.generated.resources.catalog_section_history
import me.yuriisoft.buildnotify.mobile.designsystem.generated.resources.catalog_section_log
import me.yuriisoft.buildnotify.mobile.designsystem.generated.resources.catalog_section_run_again
import me.yuriisoft.buildnotify.mobile.designsystem.generated.resources.catalog_section_today
import me.yuriisoft.buildnotify.mobile.designsystem.generated.resources.catalog_status_cancelled
import me.yuriisoft.buildnotify.mobile.designsystem.generated.resources.catalog_status_failed
import me.yuriisoft.buildnotify.mobile.designsystem.generated.resources.catalog_status_success
import me.yuriisoft.buildnotify.mobile.designsystem.generated.resources.catalog_tag_agent
import me.yuriisoft.buildnotify.mobile.designsystem.molecules.console.LogLine
import me.yuriisoft.buildnotify.mobile.designsystem.molecules.console.LogTone
import me.yuriisoft.buildnotify.mobile.designsystem.molecules.fields.FilterChip
import me.yuriisoft.buildnotify.mobile.designsystem.molecules.texts.SectionHeader
import me.yuriisoft.buildnotify.mobile.designsystem.organisms.console.Console
import me.yuriisoft.buildnotify.mobile.designsystem.organisms.lists.HistoryRow
import me.yuriisoft.buildnotify.mobile.designsystem.organisms.lists.RunTaskRow
import me.yuriisoft.buildnotify.mobile.designsystem.particles.theme.BuildTone
import me.yuriisoft.buildnotify.mobile.designsystem.particles.theme.GraphiteTheme
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun HistorySection() {
    Section(stringResource(Res.string.catalog_section_history)) {
        Column {
            SectionHeader(stringResource(Res.string.catalog_section_today))
            HistoryRow(
                BuildTone.Success,
                stringResource(Res.string.catalog_status_success),
                CatalogSamples.SHOP_ANDROID,
                CatalogSamples.TASK_ASSEMBLE,
                CatalogSamples.DURATION_SUCCESS,
                CatalogSamples.TIME_SUCCESS,
            )
            HistoryRow(
                BuildTone.Failure,
                stringResource(Res.string.catalog_status_failed),
                CatalogSamples.SHOP_ANDROID,
                CatalogSamples.TASK_UNIT_TEST,
                CatalogSamples.DURATION_TEST,
                CatalogSamples.TIME_FAILED,
                tag = stringResource(Res.string.catalog_tag_agent),
            )
            HistoryRow(
                BuildTone.Warning,
                stringResource(Res.string.catalog_status_cancelled),
                CatalogSamples.DESIGN_KIT,
                CatalogSamples.TASK_PUBLISH,
                CatalogSamples.DURATION_PUBLISH,
                CatalogSamples.TIME_CANCELLED,
                showDivider = false,
            )
        }
        Column {
            SectionHeader(stringResource(Res.string.catalog_section_run_again))
            RunTaskRow(CatalogSamples.TASK_ASSEMBLE, CatalogSamples.DURATION_ASSEMBLE_ESTIMATE, onRun = {})
            RunTaskRow(
                CatalogSamples.TASK_INSTALL,
                CatalogSamples.DURATION_INSTALL_ESTIMATE,
                onRun = {},
                showDivider = false,
            )
        }
    }
}

@Composable
internal fun LogSection() {
    val spacing = GraphiteTheme.spacing
    var filter by remember { mutableStateOf(0) }
    Section(stringResource(Res.string.catalog_section_log)) {
        Row(horizontalArrangement = Arrangement.spacedBy(spacing.s)) {
            FilterChip(
                stringResource(Res.string.catalog_filter_all),
                selected = filter == 0,
                onClick = { filter = 0 },
                count = CatalogSamples.FILTER_ALL,
            )
            FilterChip(
                stringResource(Res.string.catalog_filter_problems),
                selected = filter == 1,
                onClick = { filter = 1 },
                count = CatalogSamples.FILTER_PROBLEMS,
            )
            FilterChip(
                stringResource(Res.string.catalog_filter_errors),
                selected = filter == 2,
                onClick = { filter = 2 },
                count = CatalogSamples.FILTER_ERRORS,
            )
        }
        Console {
            if (filter == 0) LogLine(CatalogSamples.TASK_COMPILE_PAYMENT, offset = CatalogSamples.OFFSET_COMPILE)
            if (filter < 2) LogLine(
                    CatalogSamples.LOG_DEPRECATED,
                    tone = LogTone.Warning,
                    offset = CatalogSamples.OFFSET_WARNING,
                )
            LogLine(CatalogSamples.LOG_UNRESOLVED, tone = LogTone.Error, offset = CatalogSamples.OFFSET_ERROR)
            LogLine(CatalogSamples.LOG_FAILED, tone = LogTone.Error, offset = CatalogSamples.OFFSET_FAILED)
        }
    }
}
