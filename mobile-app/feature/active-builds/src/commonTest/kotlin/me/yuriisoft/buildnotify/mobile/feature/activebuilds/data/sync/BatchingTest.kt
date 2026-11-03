package me.yuriisoft.buildnotify.mobile.feature.activebuilds.data.sync

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.time.Duration.Companion.milliseconds

class BatchingTest {

    @Test
    fun burst_isEmittedAsOneBatch() = runTest {
        val batches = flow { repeat(5) { emit(it) } }.batched(100.milliseconds, maxSize = 50).toList()
        assertEquals(listOf(listOf(0, 1, 2, 3, 4)), batches)
    }

    @Test
    fun batches_neverExceedMaxSize_andKeepEveryValueInOrder() = runTest {
        val batches = flow { repeat(1_000) { emit(it) } }.batched(10.milliseconds, maxSize = 64).toList()

        assertEquals((0 until 1_000).toList(), batches.flatten())
        assertEquals(true, batches.all { it.size <= 64 })
    }

    @Test
    fun valuesFurtherApartThanTheWindow_goToSeparateBatches() = runTest {
        val batches = flow {
            emit(1)
            delay(500)
            emit(2)
        }.batched(100.milliseconds, maxSize = 50).toList()

        assertEquals(listOf(listOf(1), listOf(2)), batches)
    }

    @Test
    fun nonPositiveMaxSize_isRejected() {
        assertFailsWith<IllegalArgumentException> { flow<Int> { }.batched(10.milliseconds, maxSize = 0) }
    }
}
