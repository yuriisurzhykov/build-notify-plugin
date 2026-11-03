package me.yuriisoft.buildnotify.mobile.feature.activebuilds.ui

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.withFrameNanos

/**
 * Follows the tail like `tail -f`, but only while the reader is already at the bottom.
 * Returns whether the list is currently at the bottom.
 */
@Composable
internal fun rememberTailFollow(listState: LazyListState, lineCount: Int): State<Boolean> {
    val atBottom = remember(listState) {
        derivedStateOf {
            val info = listState.layoutInfo
            val last = info.visibleItemsInfo.lastOrNull()
            last == null || last.index >= info.totalItemsCount - BOTTOM_SLACK_ITEMS
        }
    }
    LaunchedEffect(lineCount) {
        val follow = atBottom.value
        withFrameNanos { } // let the list lay out the new lines first
        if (follow) listState.scrollToEnd()
    }
    return atBottom
}

/** Animates to the last item, if there is one. */
internal suspend fun LazyListState.scrollToEnd() {
    val total = layoutInfo.totalItemsCount
    if (total > 0) animateScrollToItem(total - 1)
}

private const val BOTTOM_SLACK_ITEMS = 2
