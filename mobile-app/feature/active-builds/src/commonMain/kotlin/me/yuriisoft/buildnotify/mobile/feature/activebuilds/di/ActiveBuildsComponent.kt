package me.yuriisoft.buildnotify.mobile.feature.activebuilds.di

import kotlinx.coroutines.CoroutineScope
import me.tatarka.inject.annotations.IntoSet
import me.tatarka.inject.annotations.Provides
import me.yuriisoft.buildnotify.mobile.core.di.AppScope
import me.yuriisoft.buildnotify.mobile.core.dispatchers.AppDispatchers
import me.yuriisoft.buildnotify.mobile.core.navigation.Screen
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.data.ActiveBuildCachedSource
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.data.ActiveBuildRepository
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.data.BuildLogCachedSource
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.data.local.ActiveBuildsDatabaseFactory
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.data.local.LocalActiveBuildSource
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.data.local.LocalBuildLogSource
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.data.mapper.BuildEventMapper
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.data.mapper.BuildSnapshotMapper
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.data.mapper.LogRecordMapper
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.data.remote.RemoteActiveBuildSource
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.data.remote.RemoteBuildLogSource
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.data.sync.ActiveBuildsCacheSync
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.data.sync.BuildCacheInvalidator
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
 * Sources are bound by concrete type on purpose: remote, local and cached sources share
 * the same generic interfaces, so interface bindings would be ambiguous.
 */
interface ActiveBuildsComponent {

    @AppScope
    @Provides
    fun activeBuildsDatabase(factory: ActiveBuildsDatabaseFactory): ActiveBuildsDatabase =
        factory.create()

    @AppScope
    @Provides
    fun remoteActiveBuildSource(
        session: ActiveSession,
        dispatchers: AppDispatchers,
        scope: CoroutineScope,
    ): RemoteActiveBuildSource = RemoteActiveBuildSource(
        session = session,
        mapper = BuildEventMapper(clock = ::currentTimeMillis),
        dispatchers = dispatchers,
        scope = scope,
    )

    @AppScope
    @Provides
    fun remoteBuildLogSource(
        session: ActiveSession,
        dispatchers: AppDispatchers,
        scope: CoroutineScope,
    ): RemoteBuildLogSource = RemoteBuildLogSource(
        session = session,
        mapper = BuildEventMapper(clock = ::currentTimeMillis),
        dispatchers = dispatchers,
        scope = scope,
    )

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
    fun localBuildLogSource(
        db: ActiveBuildsDatabase,
        dispatchers: AppDispatchers,
    ): LocalBuildLogSource = LocalBuildLogSource(
        queries = db.buildLogQueries,
        logMapper = LogRecordMapper(),
        dispatchers = dispatchers,
    )

    @AppScope
    @Provides
    fun activeBuildRepository(
        remoteBuilds: RemoteActiveBuildSource,
        localBuilds: LocalActiveBuildSource,
        remoteLogs: RemoteBuildLogSource,
        localLogs: LocalBuildLogSource,
    ): IActiveBuildRepository = ActiveBuildRepository(
        buildsCached = ActiveBuildCachedSource(remoteBuilds, localBuilds),
        logsCached = BuildLogCachedSource(remoteLogs, localLogs),
    )

    @AppScope
    @Provides
    fun buildCacheInvalidator(
        connectionManager: ConnectionManager,
        localBuilds: LocalActiveBuildSource,
        dispatchers: AppDispatchers,
        scope: CoroutineScope,
    ): BuildCacheInvalidator = BuildCacheInvalidator(connectionManager, localBuilds, dispatchers, scope)

    @AppScope
    @Provides
    fun activeBuildsCacheSync(
        repository: IActiveBuildRepository,
        dispatchers: AppDispatchers,
        scope: CoroutineScope,
    ): ActiveBuildsCacheSync = ActiveBuildsCacheSync(repository, dispatchers, scope)

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
