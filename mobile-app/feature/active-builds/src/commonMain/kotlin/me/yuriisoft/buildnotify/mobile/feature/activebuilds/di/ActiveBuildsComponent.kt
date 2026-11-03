package me.yuriisoft.buildnotify.mobile.feature.activebuilds.di

import kotlinx.coroutines.CoroutineScope
import me.tatarka.inject.annotations.IntoSet
import me.tatarka.inject.annotations.Provides
import me.yuriisoft.buildnotify.mobile.core.di.AppScope
import me.yuriisoft.buildnotify.mobile.core.dispatchers.AppDispatchers
import me.yuriisoft.buildnotify.mobile.core.navigation.Screen
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.data.ActiveBuildRepository
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.data.local.ActiveBuildsDatabaseFactory
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.data.local.BuildEventStore
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.data.local.LocalActiveBuildSource
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.data.local.LocalBuildLogSource
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.data.local.LocalLogStatsSource
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.data.local.SqlBuildEventStore
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.data.mapper.BuildEventMapper
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.data.mapper.BuildSnapshotMapper
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.data.mapper.LogRecordMapper
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.data.sync.BuildCacheInvalidator
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.data.sync.BuildEventJournal
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.data.sync.DefaultCancelBuildUseCase
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.db.ActiveBuildsDatabase
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.domain.repository.IActiveBuildRepository
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.domain.usecase.CancelBuildUseCase
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.logs.BuildLogsScreen
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.presentation.ActiveBuildsScreen
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.presentation.ActiveBuildsViewModel
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.ui.currentTimeMillis
import me.yuriisoft.buildnotify.mobile.network.connection.ActiveSession
import me.yuriisoft.buildnotify.mobile.network.connection.ConnectionManager

/**
 * Contribution interface for the Active Builds feature.
 *
 * Needs from the parent graph: [ActiveBuildsDatabaseFactory], [ActiveSession],
 * [ConnectionManager], [AppDispatchers] and an app-lifetime [CoroutineScope].
 *
 * Network events flow one way: [BuildEventJournal] writes them in batches to the
 * [BuildEventStore], and the repository reads only from the store. Sources are bound
 * by concrete type: they share generic interfaces, so interface bindings would clash.
 */
interface ActiveBuildsComponent {

    @AppScope
    @Provides
    fun activeBuildsDatabase(factory: ActiveBuildsDatabaseFactory): ActiveBuildsDatabase =
        factory.create()

    @AppScope
    @Provides
    fun buildEventStore(
        db: ActiveBuildsDatabase,
        dispatchers: AppDispatchers,
    ): BuildEventStore = SqlBuildEventStore(db, dispatchers)

    @AppScope
    @Provides
    fun localActiveBuildSource(
        db: ActiveBuildsDatabase,
        dispatchers: AppDispatchers,
    ): LocalActiveBuildSource = LocalActiveBuildSource(
        activeBuildQueries = db.activeBuildQueries,
        issueQueries = db.buildIssueQueries,
        snapshotMapper = BuildSnapshotMapper(),
        dispatchers = dispatchers,
    )

    @AppScope
    @Provides
    fun activeBuildRepository(
        db: ActiveBuildsDatabase,
        localBuilds: LocalActiveBuildSource,
        dispatchers: AppDispatchers,
    ): IActiveBuildRepository = ActiveBuildRepository(
        builds = localBuilds,
        logs = LocalBuildLogSource(db.buildLogQueries, LogRecordMapper(), dispatchers),
        logStats = LocalLogStatsSource(db.buildLogCountQueries, dispatchers),
    )

    @AppScope
    @Provides
    fun buildEventJournal(
        session: ActiveSession,
        store: BuildEventStore,
        dispatchers: AppDispatchers,
        scope: CoroutineScope,
    ): BuildEventJournal = BuildEventJournal(
        session = session,
        mapper = BuildEventMapper(clock = ::currentTimeMillis),
        store = store,
        dispatchers = dispatchers,
        scope = scope,
    )

    @AppScope
    @Provides
    fun buildCacheInvalidator(
        connectionManager: ConnectionManager,
        store: BuildEventStore,
        dispatchers: AppDispatchers,
        scope: CoroutineScope,
    ): BuildCacheInvalidator = BuildCacheInvalidator(connectionManager, store, dispatchers, scope)

    @Provides
    fun cancelBuildUseCase(session: ActiveSession): CancelBuildUseCase =
        DefaultCancelBuildUseCase(session)

    @Provides
    fun activeBuildsViewModel(
        repository: IActiveBuildRepository,
        cancelBuild: CancelBuildUseCase,
        dispatchers: AppDispatchers,
        connectionManager: ConnectionManager,
    ): ActiveBuildsViewModel = ActiveBuildsViewModel(repository, cancelBuild, dispatchers, connectionManager)

    @IntoSet
    @Provides
    fun activeBuildsScreen(screen: ActiveBuildsScreen): Screen = screen

    @IntoSet
    @Provides
    fun buildLogsScreen(screen: BuildLogsScreen): Screen = screen
}
