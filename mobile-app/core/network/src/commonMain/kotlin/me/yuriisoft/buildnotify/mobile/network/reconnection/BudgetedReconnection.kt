package me.yuriisoft.buildnotify.mobile.network.reconnection

import kotlinx.coroutines.delay
import me.yuriisoft.buildnotify.mobile.network.error.ConnectionErrorReason
import me.yuriisoft.buildnotify.mobile.network.error.ErrorMapping
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds
import kotlin.time.TimeMark
import kotlin.time.TimeSource

/**
 * [ReconnectionStrategy] that retries within a fixed total time budget
 * using a constant interval between attempts.
 *
 * Unlike exponential backoff, this strategy prioritises **liveness**: it keeps
 * retrying at a short, fixed [retryInterval] for as long as the [totalBudget]
 * has not been exhausted. A new attempt is only started when the remaining
 * budget can accommodate both the [retryInterval] wait **and** a full
 * [attemptTimeout] window, avoiding partial attempts that waste time.
 *
 * ### Budget lifecycle
 *
 * The budget clock starts on the **first failure** of a retry sequence
 * (`attempt == 0`). Because `retryWhen` resets its counter to 0 whenever
 * the upstream Flow is re-collected, the budget automatically resets for
 * each new connection attempt — no explicit `reset()` method is needed.
 *
 * ### Permanent failures
 *
 * Errors mapped to [ConnectionErrorReason.ClientRejected] via [errorMapping]
 * are treated as permanent: retrying cannot resolve them, so [shouldRetry]
 * returns `false` immediately regardless of remaining budget.
 *
 * @param errorMapping   classifies a [Throwable] into a [ConnectionErrorReason].
 * @param totalBudget    maximum wall-clock time to keep retrying (default 1 minute).
 * @param attemptTimeout expected upper bound for a single connection attempt;
 *                       used to decide whether enough budget remains for one more try.
 * @param retryInterval  fixed delay between consecutive retries (default 2 seconds).
 * @param timeSource     clock used for budget tracking; injectable for testing.
 */
class BudgetedReconnection(
    private val errorMapping: ErrorMapping,
    private val totalBudget: Duration = 1.minutes,
    private val attemptTimeout: Duration = 15.seconds,
    private val retryInterval: Duration = 2.seconds,
    private val timeSource: TimeSource = TimeSource.Monotonic,
) : ReconnectionStrategy {

    @Volatile
    private var budgetStart: TimeMark? = null

    /**
     * Returns `true` if the connection should be retried.
     *
     * The decision is based on three criteria evaluated in order:
     * 1. **Permanent failure** — errors that no amount of retrying can fix
     *    (e.g. [ConnectionErrorReason.ClientRejected]) immediately return `false`.
     * 2. **Budget remaining** — if the elapsed time since the first failure
     *    leaves less than `[retryInterval] + [attemptTimeout]`, there is not
     *    enough room for another attempt and the method returns `false`.
     * 3. Otherwise, the method suspends for [retryInterval] and returns `true`.
     *
     * @param cause   the exception from the most recent failed connection attempt.
     * @param attempt 0-based index provided by the `retryWhen` operator.
     */
    override suspend fun shouldRetry(cause: Throwable, attempt: Long): Boolean {
        if (isPermanentFailure(cause)) return false

        val start = if (attempt == 0L) {
            timeSource.markNow().also { budgetStart = it }
        } else {
            budgetStart ?: return false
        }

        val remaining = totalBudget - start.elapsedNow()
        if (remaining < retryInterval + attemptTimeout) return false

        delay(retryInterval)
        return true
    }

    private fun isPermanentFailure(cause: Throwable): Boolean =
        errorMapping.map(cause) is ConnectionErrorReason.ClientRejected
}
