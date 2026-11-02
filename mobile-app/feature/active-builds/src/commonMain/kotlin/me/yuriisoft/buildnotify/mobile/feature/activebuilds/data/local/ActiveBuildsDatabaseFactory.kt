package me.yuriisoft.buildnotify.mobile.feature.activebuilds.data.local

import me.yuriisoft.buildnotify.mobile.feature.activebuilds.db.ActiveBuildsDatabase

/**
 * Creates the platform SQLite-backed [ActiveBuildsDatabase].
 *
 * Every implementation must enable foreign keys: `build_issue` and `build_log`
 * rely on `ON DELETE CASCADE` to disappear together with their `active_build` row.
 */
expect class ActiveBuildsDatabaseFactory {
    fun create(): ActiveBuildsDatabase
}

internal const val ACTIVE_BUILDS_DB_NAME = "build_notify_active_builds.db"
