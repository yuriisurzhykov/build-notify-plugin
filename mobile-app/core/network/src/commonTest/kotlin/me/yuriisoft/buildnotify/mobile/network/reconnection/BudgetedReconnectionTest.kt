package me.yuriisoft.buildnotify.mobile.network.reconnection

import kotlinx.coroutines.test.runTest
import me.yuriisoft.buildnotify.mobile.network.error.ConnectionErrorReason
import me.yuriisoft.buildnotify.mobile.network.error.ErrorMapping
import me.yuriisoft.buildnotify.mobile.network.error.ErrorRecognizer
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds
import kotlin.time.ExperimentalTime
import kotlin.time.TestTimeSource

@OptIn(ExperimentalTime::class)
class BudgetedReconnectionTest {

    private val timeSource = TestTimeSource()
    private val cause = RuntimeException("test error")

    private fun reconnection(
        totalBudget: Duration = 1.minutes,
        attemptTimeout: Duration = 15.seconds,
        retryInterval: Duration = 100.milliseconds,
        recognizers: List<ErrorRecognizer> = emptyList(),
    ) = BudgetedReconnection(
        errorMapping = ErrorMapping(recognizers),
        totalBudget = totalBudget,
        attemptTimeout = attemptTimeout,
        retryInterval = retryInterval,
        timeSource = timeSource,
    )

    @Test
    fun retriesWhileBudgetRemains() = runTest {
        val strategy = reconnection(totalBudget = 1.minutes, attemptTimeout = 15.seconds)

        assertTrue(strategy.shouldRetry(cause, attempt = 0))

        timeSource += 20.seconds
        assertTrue(strategy.shouldRetry(cause, attempt = 1))

        timeSource += 10.seconds
        assertTrue(strategy.shouldRetry(cause, attempt = 2))
    }

    @Test
    fun stopsWhenBudgetExhausted() = runTest {
        val strategy = reconnection(totalBudget = 30.seconds, attemptTimeout = 15.seconds)

        assertTrue(strategy.shouldRetry(cause, attempt = 0))

        timeSource += 16.seconds
        assertFalse(strategy.shouldRetry(cause, attempt = 1))
    }

    @Test
    fun stopsWhenNotEnoughTimeForFullAttempt() = runTest {
        val strategy = reconnection(
            totalBudget = 30.seconds,
            attemptTimeout = 10.seconds,
            retryInterval = 5.seconds,
        )

        assertTrue(strategy.shouldRetry(cause, attempt = 0))

        timeSource += 16.seconds
        assertFalse(strategy.shouldRetry(cause, attempt = 1))
    }

    @Test
    fun stopsImmediatelyForPermanentFailure() = runTest {
        val rejectionRecognizer = ErrorRecognizer { throwable ->
            ConnectionErrorReason.ClientRejected(throwable.message ?: "rejected")
        }
        val strategy = reconnection(recognizers = listOf(rejectionRecognizer))

        assertFalse(strategy.shouldRetry(cause, attempt = 0))
    }

    @Test
    fun usesFixedRetryInterval() = runTest {
        val strategy = reconnection(retryInterval = 500.milliseconds)

        val before0 = testScheduler.currentTime
        strategy.shouldRetry(cause, attempt = 0)
        val delay0 = testScheduler.currentTime - before0

        val before1 = testScheduler.currentTime
        strategy.shouldRetry(cause, attempt = 1)
        val delay1 = testScheduler.currentTime - before1

        val before2 = testScheduler.currentTime
        strategy.shouldRetry(cause, attempt = 2)
        val delay2 = testScheduler.currentTime - before2

        assertTrue(delay0 == 500L, "attempt 0: expected 500ms, got ${delay0}ms")
        assertTrue(delay1 == 500L, "attempt 1: expected 500ms, got ${delay1}ms")
        assertTrue(delay2 == 500L, "attempt 2: expected 500ms, got ${delay2}ms")
    }

    @Test
    fun budgetResetsOnNewRetrySequence() = runTest {
        val strategy = reconnection(totalBudget = 30.seconds, attemptTimeout = 15.seconds)

        assertTrue(strategy.shouldRetry(cause, attempt = 0))
        timeSource += 16.seconds
        assertFalse(strategy.shouldRetry(cause, attempt = 1))

        assertTrue(strategy.shouldRetry(cause, attempt = 0))
    }

    @Test
    fun returnsFalseWithoutPriorStart() = runTest {
        val strategy = reconnection()

        assertFalse(strategy.shouldRetry(cause, attempt = 5))
    }

    @Test
    fun nonRejectionErrorsAreRetriable() = runTest {
        val timeoutRecognizer = ErrorRecognizer { throwable ->
            ConnectionErrorReason.Timeout(throwable.message ?: "timeout")
        }
        val strategy = reconnection(recognizers = listOf(timeoutRecognizer))

        assertTrue(strategy.shouldRetry(cause, attempt = 0))
    }

    @Test
    fun refusedErrorsAreRetriable() = runTest {
        val refusedRecognizer = ErrorRecognizer { throwable ->
            ConnectionErrorReason.Refused(throwable.message ?: "refused")
        }
        val strategy = reconnection(recognizers = listOf(refusedRecognizer))

        assertTrue(strategy.shouldRetry(cause, attempt = 0))
        assertTrue(strategy.shouldRetry(cause, attempt = 1))
    }
}
