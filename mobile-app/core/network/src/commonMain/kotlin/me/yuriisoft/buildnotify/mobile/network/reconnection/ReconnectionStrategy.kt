package me.yuriisoft.buildnotify.mobile.network.reconnection

/**
 * Decides whether a failed connection should be retried.
 *
 * The [attempt] parameter comes from the `retryWhen` Flow operator in
 * [ConnectionOrchestrator] and resets to 0 on each new flow collection,
 * so implementations need no explicit `reset()` method.
 * Implementations may track per-sequence state (e.g. a budget start mark)
 * keyed to `attempt == 0`, which auto-resets with each new retry sequence.
 *
 * Returning `true` causes the upstream Flow (transport) to restart.
 * The implementation may `delay()` before returning to apply backoff.
 * Returning `false` lets the error propagate to `onCompletion`.
 */
interface ReconnectionStrategy {
    suspend fun shouldRetry(cause: Throwable, attempt: Long): Boolean
}
