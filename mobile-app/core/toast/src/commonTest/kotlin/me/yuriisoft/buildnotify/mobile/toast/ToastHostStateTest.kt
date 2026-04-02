package me.yuriisoft.buildnotify.mobile.toast

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import me.yuriisoft.buildnotify.mobile.toast.model.ToastData
import me.yuriisoft.buildnotify.mobile.toast.model.ToastDuration
import me.yuriisoft.buildnotify.mobile.toast.model.ToastResult
import me.yuriisoft.buildnotify.mobile.toast.model.ToastType
import me.yuriisoft.buildnotify.mobile.ui.resource.TextResource
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.seconds

@OptIn(ExperimentalCoroutinesApi::class)
class ToastHostStateTest {

    private fun TestScope.hostState() = ToastHostState(scope = backgroundScope)

    private fun toast(
        id: String = "test",
        type: ToastType = ToastType.Info,
        duration: ToastDuration = type.defaultDuration,
    ) = ToastData(
        id = id,
        type = type,
        message = TextResource.RawText("msg-$id"),
        duration = duration,
    )

    @Test
    fun initiallyNoToastIsVisible() = runTest {
        val state = hostState()
        advanceUntilIdle()
        assertNull(state.currentToast)
    }

    @Test
    fun showDisplaysToast() = runTest {
        val state = hostState()
        val data = toast(duration = ToastDuration.Indefinite)
        backgroundScope.async { state.show(data) }
        advanceUntilIdle()
        assertEquals(data, state.currentToast)
    }

    @Test
    fun dismissRemovesCurrentToast() = runTest {
        val state = hostState()
        val data = toast(duration = ToastDuration.Indefinite)
        val result = backgroundScope.async { state.show(data) }
        advanceUntilIdle()

        state.dismiss()
        advanceUntilIdle()

        assertNull(state.currentToast)
        assertEquals(ToastResult.Dismissed, result.await())
    }

    @Test
    fun dismissByIdRemovesSpecificToast() = runTest {
        val state = hostState()
        val data = toast(id = "target", duration = ToastDuration.Indefinite)
        val result = backgroundScope.async { state.show(data) }
        advanceUntilIdle()

        state.dismiss("target")
        advanceUntilIdle()

        assertNull(state.currentToast)
        assertEquals(ToastResult.Dismissed, result.await())
    }

    @Test
    fun dismissByIdIgnoresMismatch() = runTest {
        val state = hostState()
        val data = toast(id = "active", duration = ToastDuration.Indefinite)
        backgroundScope.async { state.show(data) }
        advanceUntilIdle()

        state.dismiss("other")
        advanceUntilIdle()

        assertEquals(data, state.currentToast)
    }

    @Test
    fun timedToastAutoDismissesAfterDuration() = runTest {
        val state = hostState()
        val data = toast(duration = ToastDuration.Timed(3.seconds))
        val result = backgroundScope.async { state.show(data) }
        advanceUntilIdle()

        assertEquals(data, state.currentToast)

        advanceTimeBy(3001)
        advanceUntilIdle()

        assertNull(state.currentToast)
        assertEquals(ToastResult.TimedOut, result.await())
    }

    @Test
    fun indefiniteToastDoesNotAutoDismiss() = runTest {
        val state = hostState()
        val data = toast(duration = ToastDuration.Indefinite)
        backgroundScope.async { state.show(data) }
        advanceUntilIdle()

        advanceTimeBy(60_000)
        advanceUntilIdle()

        assertEquals(data, state.currentToast)
    }

    @Test
    fun higherPriorityPreemptsCurrent() = runTest {
        val state = hostState()
        val low = toast(id = "low", type = ToastType.Info, duration = ToastDuration.Indefinite)
        val high = toast(id = "high", type = ToastType.Error, duration = ToastDuration.Indefinite)

        val lowResult = backgroundScope.async { state.show(low) }
        advanceUntilIdle()
        assertEquals(low, state.currentToast)

        backgroundScope.async { state.show(high) }
        advanceUntilIdle()

        assertEquals(high, state.currentToast)
        assertEquals(ToastResult.Replaced, lowResult.await())
    }

    @Test
    fun lowerPriorityIsQueuedFifo() = runTest {
        val state = hostState()
        val high = toast(id = "high", type = ToastType.Error, duration = ToastDuration.Indefinite)
        val low = toast(id = "low", type = ToastType.Info, duration = ToastDuration.Indefinite)

        backgroundScope.async { state.show(high) }
        advanceUntilIdle()
        assertEquals(high, state.currentToast)

        backgroundScope.async { state.show(low) }
        advanceUntilIdle()
        assertEquals(high, state.currentToast)

        state.dismiss()
        advanceUntilIdle()
        assertEquals(low, state.currentToast)
    }

    @Test
    fun fifoOrderIsPreservedForEqualPriority() = runTest {
        val state = hostState()
        val first = toast(id = "first", type = ToastType.Info, duration = ToastDuration.Indefinite)
        val second = toast(id = "second", type = ToastType.Info, duration = ToastDuration.Indefinite)
        val third = toast(id = "third", type = ToastType.Info, duration = ToastDuration.Indefinite)

        backgroundScope.async { state.show(first) }
        advanceUntilIdle()

        backgroundScope.async { state.show(second) }
        backgroundScope.async { state.show(third) }
        advanceUntilIdle()

        assertEquals(first, state.currentToast)

        state.dismiss()
        advanceUntilIdle()
        assertEquals(second, state.currentToast)

        state.dismiss()
        advanceUntilIdle()
        assertEquals(third, state.currentToast)
    }

    @Test
    fun dismissQueuedToastByIdRemovesFromQueue() = runTest {
        val state = hostState()
        val active = toast(id = "active", type = ToastType.Error, duration = ToastDuration.Indefinite)
        val queued = toast(id = "queued", type = ToastType.Info, duration = ToastDuration.Indefinite)
        val last = toast(id = "last", type = ToastType.Info, duration = ToastDuration.Indefinite)

        backgroundScope.async { state.show(active) }
        advanceUntilIdle()
        val queuedResult = backgroundScope.async { state.show(queued) }
        backgroundScope.async { state.show(last) }
        advanceUntilIdle()

        state.dismiss("queued")
        advanceUntilIdle()

        assertEquals(active, state.currentToast)
        assertEquals(ToastResult.Dismissed, queuedResult.await())

        state.dismiss()
        advanceUntilIdle()
        assertEquals(last, state.currentToast)
    }

    @Test
    fun timedToastPromotesNextFromQueue() = runTest {
        val state = hostState()
        val timed = toast(id = "timed", type = ToastType.Success, duration = ToastDuration.Timed(2.seconds))
        val next = toast(id = "next", type = ToastType.Info, duration = ToastDuration.Indefinite)

        backgroundScope.async { state.show(timed) }
        advanceUntilIdle()

        backgroundScope.async { state.show(next) }
        advanceUntilIdle()
        assertEquals(timed, state.currentToast)

        advanceTimeBy(2001)
        advanceUntilIdle()

        assertEquals(next, state.currentToast)
    }

    @Test
    fun showReturnsDismissedWhenManuallyDismissed() = runTest {
        val state = hostState()
        val data = toast(duration = ToastDuration.Indefinite)
        val result = backgroundScope.async { state.show(data) }
        advanceUntilIdle()

        state.dismiss()
        advanceUntilIdle()

        assertEquals(ToastResult.Dismissed, result.await())
    }

    @Test
    fun multiplePreemptionsReturnReplaced() = runTest {
        val state = hostState()
        val info = toast(id = "info", type = ToastType.Info, duration = ToastDuration.Indefinite)
        val warning = toast(id = "warning", type = ToastType.Warning, duration = ToastDuration.Indefinite)
        val error = toast(id = "error", type = ToastType.Error, duration = ToastDuration.Indefinite)

        val infoResult = backgroundScope.async { state.show(info) }
        advanceUntilIdle()

        val warningResult = backgroundScope.async { state.show(warning) }
        advanceUntilIdle()
        assertEquals(ToastResult.Replaced, infoResult.await())

        backgroundScope.async { state.show(error) }
        advanceUntilIdle()
        assertEquals(ToastResult.Replaced, warningResult.await())

        assertEquals(error, state.currentToast)
    }

    @Test
    fun dismissAllClearsQueueAndActive() = runTest {
        val state = hostState()
        val first = toast(id = "a", type = ToastType.Error, duration = ToastDuration.Indefinite)
        val second = toast(id = "b", type = ToastType.Info, duration = ToastDuration.Indefinite)

        backgroundScope.async { state.show(first) }
        backgroundScope.async { state.show(second) }
        advanceUntilIdle()

        state.dismiss("a")
        advanceUntilIdle()

        assertTrue(state.currentToast?.id == "b")

        state.dismiss("b")
        advanceUntilIdle()

        assertNull(state.currentToast)
    }
}
