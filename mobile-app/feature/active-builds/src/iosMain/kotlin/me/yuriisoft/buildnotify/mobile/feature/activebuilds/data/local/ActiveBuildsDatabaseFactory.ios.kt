package me.yuriisoft.buildnotify.mobile.feature.activebuilds.data.local

import app.cash.sqldelight.driver.native.NativeSqliteDriver
import co.touchlab.sqliter.DatabaseConfiguration
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.db.ActiveBuildsDatabase

actual class ActiveBuildsDatabaseFactory {

    actual fun create(): ActiveBuildsDatabase = ActiveBuildsDatabase(
        NativeSqliteDriver(
            schema = ActiveBuildsDatabase.Schema,
            name = ACTIVE_BUILDS_DB_NAME,
            onConfiguration = { config ->
                config.copy(
                    extendedConfig = DatabaseConfiguration.Extended(foreignKeyConstraints = true),
                )
            },
        ),
    )
}
