package me.yuriisoft.buildnotify.mobile.feature.activebuilds.data.local

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.db.ActiveBuildsDatabase
import kotlin.test.Test
import kotlin.test.assertEquals

/** Upgrading an installed v1 cache must keep its logs and backfill the new counters. */
class MigrationTest {

    @Test
    fun v1ToV2_backfillsLineCountsFromCachedLines() {
        val driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
        V1_SCHEMA.forEach { driver.execute(null, it, 0) }
        driver.execute(null, "INSERT INTO active_build VALUES ('b1', 'app', 'FAILED', 1, NULL, 10)", 0)
        driver.insertLine(timestamp = 5, kind = "TASK")
        driver.insertLine(timestamp = 7, kind = "TASK")
        driver.insertLine(timestamp = 9, kind = "ERROR")

        ActiveBuildsDatabase.Schema.migrate(driver, oldVersion = 1, newVersion = ActiveBuildsDatabase.Schema.version)

        val counts = ActiveBuildsDatabase(driver).buildLogCountQueries.selectByBuildId("b1").executeAsList()
            .associate { it.kind to (it.line_count to it.first_timestamp) }
        assertEquals(mapOf("TASK" to (2L to 5L), "ERROR" to (1L to 9L)), counts)
    }

    private fun JdbcSqliteDriver.insertLine(timestamp: Long, kind: String) {
        val sql = "INSERT INTO build_log (build_id, timestamp, message, kind) VALUES ('b1', $timestamp, 'x', '$kind')"
        execute(null, sql, 0)
    }

    private companion object {
        /** The schema as shipped in v1, before `build_log_count` existed. */
        val V1_SCHEMA = listOf(
            """
            CREATE TABLE active_build (
                build_id TEXT PRIMARY KEY, project_name TEXT NOT NULL, status TEXT NOT NULL,
                started_at INTEGER NOT NULL, current_task TEXT, duration_ms INTEGER
            )
            """,
            """
            CREATE TABLE build_issue (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                build_id TEXT NOT NULL REFERENCES active_build(build_id) ON DELETE CASCADE,
                message TEXT NOT NULL, file_path TEXT, line INTEGER, severity TEXT NOT NULL
            )
            """,
            """
            CREATE TABLE build_log (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                build_id TEXT NOT NULL REFERENCES active_build(build_id) ON DELETE CASCADE,
                timestamp INTEGER NOT NULL, message TEXT NOT NULL, kind TEXT NOT NULL
            )
            """,
        )
    }
}
