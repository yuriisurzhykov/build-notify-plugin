package me.yuriisoft.buildnotify.mobile.feature.activebuilds.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.logs.BuildLogsIntent
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.logs.BuildLogsUiState

/** Filters on top, the log below, and a "to the end" button while the reader scrolls back. */
@Composable
internal fun LoadedLogs(
    state: BuildLogsUiState.Loaded,
    onIntent: (BuildLogsIntent) -> Unit,
) {
    val listState = rememberLazyListState()
    val atBottom by rememberTailFollow(listState, lineCount = state.lines.size)

    LogFilterBar(selected = state.filter, counts = state.filterCounts, onIntent = onIntent)
    Box(Modifier.fillMaxSize()) {
        LogsList(state = state, listState = listState)
        JumpToEndButton(
            visible = !atBottom,
            listState = listState,
            modifier = Modifier.align(Alignment.BottomEnd),
        )
    }
}
