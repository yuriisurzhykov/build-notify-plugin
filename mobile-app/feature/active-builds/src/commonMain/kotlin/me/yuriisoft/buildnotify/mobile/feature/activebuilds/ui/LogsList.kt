package me.yuriisoft.buildnotify.mobile.feature.activebuilds.ui

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import me.yuriisoft.buildnotify.mobile.designsystem.theme.BuildTone
import me.yuriisoft.buildnotify.mobile.designsystem.theme.GraphiteTheme
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.domain.model.LogKind
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.generated.resources.Res
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.generated.resources.logs_section_errors
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.generated.resources.logs_section_warnings
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.logs.BuildLogsUiState

/** Errors, warnings (as far as the filter allows) and the log itself, in one lazy list. */
@Composable
internal fun LogsList(
    state: BuildLogsUiState.Loaded,
    listState: LazyListState,
    onLoadOlder: () -> Unit,
) {
    val spacing = GraphiteTheme.spacing
    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = spacing.l, end = spacing.l, bottom = BottomInsetForButton),
    ) {
        if (state.filter.accepts(LogKind.ERROR)) {
            issueSection(Res.string.logs_section_errors, state.errors, BuildTone.Failure)
        }
        if (state.filter.accepts(LogKind.WARNING)) {
            issueSection(Res.string.logs_section_warnings, state.warnings, BuildTone.Warning)
        }
        outputSection(state, onLoadOlder)
    }
}

/** Room for the floating "to the end" button below the last line. */
private val BottomInsetForButton = 96.dp
