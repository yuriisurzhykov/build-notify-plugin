package me.yuriisoft.buildnotify.mobile.feature.activebuilds.data.local

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.db.ActiveBuildsDatabase

/**
 * In-memory [ActiveBuildsDatabase] for unit tests (JDBC SQLite).
 * Foreign keys are enabled so `build_log` / `build_issue` constraints match production SQLite usage.
 */
fun createTestActiveBuildsDatabase(): ActiveBuildsDatabase {
    val driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
    ActiveBuildsDatabase.Schema.create(driver)
    driver.execute(null, "PRAGMA foreign_keys=ON", 0)
    return ActiveBuildsDatabase(driver)
}
