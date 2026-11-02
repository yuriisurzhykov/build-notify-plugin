package me.yuriisoft.buildnotify.mobile.feature.activebuilds.data.local

import android.content.Context
import androidx.sqlite.db.SupportSQLiteDatabase
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.db.ActiveBuildsDatabase

actual class ActiveBuildsDatabaseFactory(private val context: Context) {

    actual fun create(): ActiveBuildsDatabase = ActiveBuildsDatabase(
        AndroidSqliteDriver(
            schema = ActiveBuildsDatabase.Schema,
            context = context,
            name = ACTIVE_BUILDS_DB_NAME,
            callback = object : AndroidSqliteDriver.Callback(ActiveBuildsDatabase.Schema) {
                override fun onConfigure(db: SupportSQLiteDatabase) {
                    super.onConfigure(db)
                    db.setForeignKeyConstraintsEnabled(true)
                }
            },
        ),
    )
}
