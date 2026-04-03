# feature:active-builds — Data / Cache Layer Architecture

Design document for the active builds feature of Build Notify Mobile.
Follows **Scenario C** from `core:cache` README — per-entity typed SQL tables
behind `MutableDataSource`, composed with `CachedReadableDataSource`.

---

## Table of Contents

1. [Purpose](#1-purpose)
2. [Abstraction Layers](#2-abstraction-layers)
3. [Data Flow Overview](#3-data-flow-overview)
4. [Model Layers](#4-model-layers)
5. [SQL Schema](#5-sql-schema)
6. [Domain Events](#6-domain-events)
7. [Data Sources](#7-data-sources)
8. [Repository](#8-repository)
9. [Mappers](#9-mappers)
10. [Use Case — Cancel Build](#10-use-case--cancel-build)
11. [Cache Lifecycle](#11-cache-lifecycle)
12. [Protocol Change (IDE Plugin)](#12-protocol-change-ide-plugin)
13. [Design Patterns Summary](#13-design-patterns-summary)
14. [Package Structure](#14-package-structure)

---

## 1. Purpose

This feature provides a persistent, reactive view of all builds currently known
to the connected IDE instance. The user sees every active build with its running
task, a chronological log of task events and compiler diagnostics, and the
ability to cancel a build. When a build finishes — successfully or not — the
card transitions to its final state with errors and timing.

The data layer is designed so that **any screen** in the application can observe
builds through a single repository interface, and the build list survives an app
process death for as long as the IDE session is alive.

**In scope:**

- Active builds list (real-time updates via WebSocket)
- Per-build log (task events + compiler diagnostics)
- Cancel build command
- Session-scoped persistence (survives app restart, cleared on permanent
  disconnect)

**Out of scope:**

- "Ask AI to Fix Error" functionality (future feature)
- UI / Presentation layer design (separate document)
- Permanent build history across IDE sessions (`:feature:history` module)

---

## 2. Abstraction Layers

This feature follows the three-tier architecture defined in `core:cache`:

```
┌─────────────────────────────────────────────────────────────────┐
│  Tier 3 — Feature Layer                                         │
│                                                                 │
│  ActiveBuildRepository : CachedReadableDataSource               │
│  BuildLogRepository    : CachedReadableDataSource               │
│  RemoteActiveBuildSource  : ReadableDataSource                  │
│  RemoteBuildLogSource     : ReadableDataSource                  │
│  LocalActiveBuildSource   : MutableDataSource                   │
│  LocalBuildLogSource      : MutableDataSource                   │
├─────────────────────────────────────────────────────────────────┤
│  Tier 2 — Data Source Contracts  (from core:cache)              │
│                                                                 │
│  ReadableDataSource<P, T>                                       │
│  WritableDataSource<P, T>                                       │
│  MutableDataSource<P, T>                                        │
│  CachedReadableDataSource<P, T>                                 │
├─────────────────────────────────────────────────────────────────┤
│  Tier 1 — Storage Engine                                        │
│                                                                 │
│  SQLDelight DAO (ActiveBuildQueries, BuildLogQueries)           │
│  (direct DAO — Scenario C, no CacheStore)                       │
└─────────────────────────────────────────────────────────────────┘
```

Per Scenario C from the `core:cache` README: `LocalXxxSource` works with
SQLDelight-generated DAOs directly, while still implementing `MutableDataSource`.
The Repository and everything above it sees only `MutableDataSource<P, T>` — the
storage engine is invisible to the domain layer.

The feature uses **two independent `CachedReadableDataSource` pipelines**:

1. **Builds pipeline** — `CachedReadableDataSource<Unit, List<BuildSnapshot>>`
2. **Logs pipeline** — `CachedReadableDataSource<String, List<BuildLogEntry>>`
   (where `String` param = `buildId`)

Each pipeline has its own remote source, local source, and cached composition.

---

## 3. Data Flow Overview

### Builds pipeline

```
ActiveSession.incoming
        │
        ▼
RemoteActiveBuildSource : ReadableDataSource<Unit, List<BuildSnapshot>>
        │  (maps WsPayload → BuildEvent via BuildEventMapper,
        │   folds events into Map<buildId, BuildSnapshot>,
        │   exposes as StateFlow with SharingStarted.Eagerly)
        │
        ▼
CachedReadableDataSource  ←──  local.save(params, data)
        │                              │
        │                              ▼
        │                  LocalActiveBuildSource : MutableDataSource
        │                  (observe = selectAll from SQL,
        │                   save = persist builds to SQL,
        │                   delete = deleteAll)
        │
        ▼
ActiveBuildRepository : IActiveBuildRepository
        │
        ▼
    ViewModel
```

### Logs pipeline

```
ActiveSession.incoming
        │
        ▼
RemoteBuildLogSource : ReadableDataSource<String, List<BuildLogEntry>>
        │  (maps WsPayload → log entries per buildId,
        │   folds into Map<buildId, List<BuildLogEntry>>,
        │   exposes per-build Flow with SharingStarted.Eagerly)
        │
        ▼
CachedReadableDataSource  ←──  local.save(buildId, data)
        │                              │
        │                              ▼
        │                  LocalBuildLogSource : MutableDataSource
        │                  (observe = selectByBuildId from SQL,
        │                   save = append new log entries,
        │                   delete = deleteByBuildId)
        │
        ▼
BuildLogRepository (or composed inside ActiveBuildRepository)
        │
        ▼
    ViewModel
```

### Side channel — cache invalidation

```
ConnectionManager.state
        │
        ▼
BuildCacheInvalidator
        │
   ┌────┴─────┐
   ▼          ▼
LocalActive   LocalBuildLog
BuildSource   Source
.delete()     .delete()
```

**Critical boundary rule:** `core:data` protocol types (`WsPayload`,
`BuildResult`, `BuildStartedPayload`, `TaskStatus`, etc.) are accepted
**exclusively** in `BuildEventMapper`. Every class downstream operates on
feature-local models only. If the wire protocol changes, only the mapper
is affected.

**Why RemoteSources use `SharingStarted.Eagerly`:**
`ActiveSession.incoming` is a `SharedFlow(replay = 0)`. If the remote source
only collected when `CachedReadableDataSource.observe()` has an active
subscriber, events arriving before the first subscriber would be lost. Eager
sharing ensures the fold runs continuously from app start, regardless of
whether any UI is currently observing.

---

## 4. Model Layers

The feature defines three independent model layers. Each layer has its own
types; no layer imports types from another.

### 4a. Data Layer Models

Package: `data/model/`

These models mirror SQL table rows. They exist solely for persistence and never
leak into domain or presentation code.

```kotlin
data class BuildRecord(
    val buildId: String,
    val projectName: String,
    val status: BuildRecordStatus,
    val startedAt: Long,
    val currentTask: String?,
    val resultJson: String?,
)

enum class BuildRecordStatus {
    ACTIVE,
    SUCCESS,
    FAILED,
    CANCELLED,
}

data class BuildLogRecord(
    val buildId: String,
    val timestamp: Long,
    val message: String,
    val kind: LogRecordKind,
)

enum class LogRecordKind {
    TASK,
    WARNING,
    ERROR,
}
```

### 4b. Domain Layer Models

Package: `domain/model/`

These models represent the business view of a build. The repository exposes
them, ViewModels consume them. They know nothing about SQL, JSON, or the wire
protocol.

```kotlin
sealed interface BuildSnapshot {

    val buildId: String
    val projectName: String
    val startedAt: Long

    data class Active(
        override val buildId: String,
        override val projectName: String,
        override val startedAt: Long,
        val currentTask: String?,
    ) : BuildSnapshot

    data class Finished(
        override val buildId: String,
        override val projectName: String,
        override val startedAt: Long,
        val outcome: BuildOutcome,
    ) : BuildSnapshot
}
```

```kotlin
data class BuildOutcome(
    val status: FinishStatus,
    val durationMs: Long,
    val errors: List<BuildIssue>,
    val warnings: List<BuildIssue>,
)

enum class FinishStatus {
    SUCCESS,
    FAILED,
    CANCELLED,
}

data class BuildIssue(
    val message: String,
    val filePath: String?,
    val line: Int?,
)
```

```kotlin
data class BuildLogEntry(
    val timestamp: Long,
    val message: String,
    val kind: LogKind,
)

enum class LogKind {
    TASK,
    WARNING,
    ERROR,
}
```

### 4c. Presentation Layer Models

Presentation models (`BuildCardState`, `BuildLogLineState`, etc.) will be
documented in a separate presentation-layer design document. They are mapped
from domain models via dedicated mappers and are completely decoupled from
both the data layer and the domain layer.

---

## 5. SQL Schema

Two tables in the shared `CacheDatabase`. The `build_log` table references
`active_build` with `ON DELETE CASCADE` — deleting a build automatically
removes all its log entries.

### active_build

```sql
CREATE TABLE active_build (
    build_id     TEXT PRIMARY KEY,
    project_name TEXT NOT NULL,
    status       TEXT NOT NULL,
    started_at   INTEGER NOT NULL,
    current_task TEXT,
    result       TEXT
);

CREATE INDEX idx_active_build_status ON active_build(status);

selectAll:
SELECT * FROM active_build ORDER BY started_at DESC;

selectById:
SELECT * FROM active_build WHERE build_id = ?;

upsert:
INSERT OR REPLACE INTO active_build
    (build_id, project_name, status, started_at, current_task, result)
VALUES (?, ?, ?, ?, ?, ?);

deleteAll:
DELETE FROM active_build;

deleteById:
DELETE FROM active_build WHERE build_id = ?;

countByStatus:
SELECT status, COUNT(*) AS count FROM active_build GROUP BY status;
```

Column notes:

| Column | Type | Description |
|---|---|---|
| `build_id` | TEXT PK | Unique build identifier from the IDE |
| `project_name` | TEXT | Human-readable project name |
| `status` | TEXT | One of: `ACTIVE`, `SUCCESS`, `FAILED`, `CANCELLED` |
| `started_at` | INTEGER | Epoch milliseconds when the build started |
| `current_task` | TEXT | Path of the currently running task, null when finished |
| `result` | TEXT | JSON-serialized `BuildOutcome`, null while active |

### build_log

```sql
CREATE TABLE build_log (
    id        INTEGER PRIMARY KEY AUTOINCREMENT,
    build_id  TEXT NOT NULL REFERENCES active_build(build_id) ON DELETE CASCADE,
    timestamp INTEGER NOT NULL,
    message   TEXT NOT NULL,
    kind      TEXT NOT NULL
);

CREATE INDEX idx_build_log_build ON build_log(build_id);

selectByBuildId:
SELECT * FROM build_log WHERE build_id = ? ORDER BY id ASC;

insert:
INSERT INTO build_log (build_id, timestamp, message, kind)
VALUES (?, ?, ?, ?);

logCount:
SELECT COUNT(*) FROM build_log WHERE build_id = ?;

deleteByBuildId:
DELETE FROM build_log WHERE build_id = ?;
```

Column notes:

| Column | Type | Description |
|---|---|---|
| `id` | INTEGER PK | Auto-increment, preserves insertion order |
| `build_id` | TEXT FK | References `active_build.build_id`, CASCADE delete |
| `timestamp` | INTEGER | Epoch milliseconds when the event occurred |
| `message` | TEXT | Human-readable log line (e.g. `"> Task :app:compile"`) |
| `kind` | TEXT | One of: `TASK`, `WARNING`, `ERROR` |

---

## 6. Domain Events

Build-related `WsPayload` messages are mapped into a feature-local `BuildEvent`
sealed hierarchy via `BuildEventMapper` at the system boundary (the only `when`
in the feature). Each event carries polymorphic fold behavior — it knows how
to fold itself into the accumulator maps used by the remote sources.

### BuildEvent sealed hierarchy

```kotlin
sealed interface BuildEvent {
    fun foldBuilds(
        builds: Map<String, BuildSnapshot>,
    ): Map<String, BuildSnapshot>

    fun foldLogs(
        logs: Map<String, List<BuildLogEntry>>,
    ): Map<String, List<BuildLogEntry>>
}
```

Each subtype implements both fold methods. Events that do not affect a
particular accumulator return it unchanged.

| Event | `foldBuilds` effect | `foldLogs` effect |
|---|---|---|
| `BuildStartedEvent` | Adds `Active` entry to the map | No change |
| `TaskStartedEvent` | Updates `currentTask` on existing `Active` | Appends `TASK` log entry |
| `TaskFinishedEvent` | No change | Appends `TASK` log entry (with status) |
| `DiagnosticEvent` | No change | Appends `WARNING` or `ERROR` log entry |
| `BuildResultEvent` | Replaces `Active` with `Finished` | No change |
| `SnapshotEvent` | Replaces entire map with snapshot data | Clears all logs (fresh start) |

Subtypes with their properties:

```kotlin
data class BuildStartedEvent(
    val buildId: String,
    val projectName: String,
    val startedAt: Long,
) : BuildEvent

data class TaskStartedEvent(
    val buildId: String,
    val taskPath: String,
    val timestamp: Long,
) : BuildEvent

data class TaskFinishedEvent(
    val buildId: String,
    val taskPath: String,
    val status: String,
    val timestamp: Long,
) : BuildEvent

data class DiagnosticEvent(
    val buildId: String,
    val severity: String,
    val message: String,
    val filePath: String?,
    val line: Int?,
    val timestamp: Long,
) : BuildEvent

data class BuildResultEvent(
    val buildId: String,
    val status: String,
    val durationMs: Long,
    val startedAt: Long,
    val finishedAt: Long,
    val errors: List<BuildIssue>,
    val warnings: List<BuildIssue>,
) : BuildEvent

data class SnapshotEvent(
    val activeBuilds: List<SnapshotBuild>,
    val recentResults: List<BuildResultEvent>,
) : BuildEvent
```

All property types are primitives or feature-local data classes. No imports
from `core:data`.

---

## 7. Data Sources

Each pipeline has a **remote source** (`ReadableDataSource`) and a **local
source** (`MutableDataSource`), following Scenario C from `core:cache` README.

### 7a. Builds — Remote Source

```kotlin
class RemoteActiveBuildSource(
    private val session: ActiveSession,
    private val mapper: Mapper<WsPayload, BuildEvent?>,
    dispatchers: AppDispatchers,
) : ReadableDataSource<Unit, List<BuildSnapshot>>
```

Internally maintains a `StateFlow` with `SharingStarted.Eagerly`:

```kotlin
private val builds: StateFlow<List<BuildSnapshot>> =
    session.incoming
        .mapNotNull { mapper.map(it) }
        .runningFold(emptyMap<String, BuildSnapshot>()) { acc, event ->
            event.foldBuilds(acc)
        }
        .map { it.values.toList() }
        .stateIn(scope, SharingStarted.Eagerly, emptyList())

override fun observe(params: Unit): Flow<List<BuildSnapshot>> = builds
```

The fold is driven by polymorphic dispatch on `BuildEvent.foldBuilds()` — no
`when` in the source itself.

### 7b. Builds — Local Source

```kotlin
class LocalActiveBuildSource(
    private val queries: ActiveBuildQueries,
    private val mapper: Mapper<BuildRecord, BuildSnapshot>,
    private val outcomeMapper: Mapper<BuildOutcome, String>,
) : MutableDataSource<Unit, List<BuildSnapshot>>
```

```kotlin
override fun observe(params: Unit): Flow<List<BuildSnapshot>> =
    queries.selectAll()
        .asFlow()
        .mapToList(Dispatchers.IO)
        .map { rows -> rows.map(mapper::map) }

override suspend fun save(params: Unit, data: List<BuildSnapshot>) {
    queries.transaction {
        data.forEach { snapshot ->
            queries.upsert(
                build_id = snapshot.buildId,
                project_name = snapshot.projectName,
                status = snapshot.sqlStatus(),
                started_at = snapshot.startedAt,
                current_task = (snapshot as? Active)?.currentTask,
                result = (snapshot as? Finished)
                    ?.let { outcomeMapper.map(it.outcome) },
            )
        }
    }
}

override suspend fun delete(params: Unit) {
    queries.deleteAll()
}
```

`save()` upserts every build from the remote emission. Builds that are no
longer present in subsequent emissions will be overwritten on the next snapshot
or cleaned up by `BuildCacheInvalidator`.

### 7c. Logs — Remote Source

```kotlin
class RemoteBuildLogSource(
    private val session: ActiveSession,
    private val mapper: Mapper<WsPayload, BuildEvent?>,
    dispatchers: AppDispatchers,
) : ReadableDataSource<String, List<BuildLogEntry>>
```

Internally maintains a `StateFlow` of all logs keyed by `buildId`:

```kotlin
private val allLogs: StateFlow<Map<String, List<BuildLogEntry>>> =
    session.incoming
        .mapNotNull { mapper.map(it) }
        .runningFold(emptyMap<String, List<BuildLogEntry>>()) { acc, event ->
            event.foldLogs(acc)
        }
        .stateIn(scope, SharingStarted.Eagerly, emptyMap())

override fun observe(params: String): Flow<List<BuildLogEntry>> =
    allLogs
        .map { it[params].orEmpty() }
        .distinctUntilChanged()
```

### 7d. Logs — Local Source

```kotlin
class LocalBuildLogSource(
    private val queries: BuildLogQueries,
    private val mapper: Mapper<BuildLogRecord, BuildLogEntry>,
) : MutableDataSource<String, List<BuildLogEntry>>
```

```kotlin
override fun observe(params: String): Flow<List<BuildLogEntry>> =
    queries.selectByBuildId(params)
        .asFlow()
        .mapToList(Dispatchers.IO)
        .map { rows -> rows.map(mapper::map) }

override suspend fun save(params: String, data: List<BuildLogEntry>) {
    val existingCount = queries.logCount(params).executeAsOne()
    val newEntries = data.drop(existingCount.toInt())
    if (newEntries.isNotEmpty()) {
        queries.transaction {
            newEntries.forEach { entry ->
                queries.insert(
                    build_id = params,
                    timestamp = entry.timestamp,
                    message = entry.message,
                    kind = entry.kind.name,
                )
            }
        }
    }
}

override suspend fun delete(params: String) {
    queries.deleteByBuildId(params)
}
```

`save()` is **append-only**: it counts existing rows and inserts only the
tail that is new. Logs are ordered and never modified, so `drop(existingCount)`
is safe. No deletion inside `save()`.

---

## 8. Repository

Two `CachedReadableDataSource` instances are composed inside a single
repository that implements the domain interface.

### Domain interface

```kotlin
interface IActiveBuildRepository {

    fun observeBuilds(): Flow<List<BuildSnapshot>>

    fun observeLogs(buildId: String): Flow<List<BuildLogEntry>>
}
```

### CachedReadableDataSource — builds

```kotlin
class ActiveBuildCachedSource(
    remote: RemoteActiveBuildSource,
    local: LocalActiveBuildSource,
) : CachedReadableDataSource<Unit, List<BuildSnapshot>>(remote, local)
```

### CachedReadableDataSource — logs

```kotlin
class BuildLogCachedSource(
    remote: RemoteBuildLogSource,
    local: LocalBuildLogSource,
) : CachedReadableDataSource<String, List<BuildLogEntry>>(remote, local)
```

### Repository

```kotlin
class ActiveBuildRepository(
    private val buildSource: ActiveBuildCachedSource,
    private val logSource: BuildLogCachedSource,
) : IActiveBuildRepository {

    override fun observeBuilds(): Flow<List<BuildSnapshot>> =
        buildSource.observe(Unit)

    override fun observeLogs(buildId: String): Flow<List<BuildLogEntry>> =
        logSource.observe(buildId)
}
```

**How it works at runtime:**

1. ViewModel calls `repository.observeBuilds()`.
2. `ActiveBuildCachedSource.observe(Unit)` starts a `channelFlow`:
   - Background: collects from `RemoteActiveBuildSource` → calls
     `LocalActiveBuildSource.save()` on every emission.
   - Foreground: collects from `LocalActiveBuildSource.observe()` → emits
     to the caller.
3. UI only ever sees data from the local source (SQLite). Remote data appears
   after it has been persisted and local re-emits.
4. On cold start, local already has persisted data — UI renders immediately
   while the remote reconnects in the background.

---

## 9. Mappers

All mappers implement `Mapper<T, S>` from `core:common`.

| Mapper class | Signature | Responsibility |
|---|---|---|
| `BuildEventMapper` | `Mapper<WsPayload, BuildEvent?>` | System boundary: maps wire protocol types to feature-local domain events. Contains the **single** `when` in the entire feature. Returns `null` for non-build payloads. |
| `BuildRecordMapper` | `Mapper<BuildRecord, BuildSnapshot>` | SQL row → domain model. Uses `JsonToOutcomeMapper` to deserialize the `result` column when status is not `ACTIVE`. |
| `LogRecordMapper` | `Mapper<BuildLogRecord, BuildLogEntry>` | SQL row → domain model. Maps `LogRecordKind` string to `LogKind` enum. |
| `OutcomeToJsonMapper` | `Mapper<BuildOutcome, String>` | Domain → JSON string for the `active_build.result` column. Used by `LocalActiveBuildSource.save()`. |
| `JsonToOutcomeMapper` | `Mapper<String, BuildOutcome>` | JSON string from `active_build.result` → domain model. Used by `BuildRecordMapper`. |

---

## 10. Use Case — Cancel Build

Cancelling a build is a command (action), not data access. It lives as a
separate use case, not inside the repository.

### Domain interface

```kotlin
interface CancelBuildUseCase {
    suspend operator fun invoke(buildId: String)
}
```

The interface lives in the domain layer and knows nothing about the network.

### Implementation (data layer)

```kotlin
class DefaultCancelBuildUseCase(
    private val session: ActiveSession,
) : CancelBuildUseCase {

    override suspend fun invoke(buildId: String) {
        session.send(WsEnvelope(payload = CancelBuildCommand(buildId)))
    }
}
```

ViewModels depend on `CancelBuildUseCase` (interface), never on the
implementation.

---

## 11. Cache Lifecycle

### App cold start

1. SQLite contains builds from the previous app session.
2. ViewModel calls `repository.observeBuilds()` →
   `ActiveBuildCachedSource.observe(Unit)`.
3. `LocalActiveBuildSource.observe()` emits the persisted list immediately.
4. UI renders the last known build cards with no delay.
5. WebSocket reconnects in the background.
6. `RemoteActiveBuildSource` receives `build.snapshot` → fold updates →
   `CachedReadableDataSource` calls `local.save()` → SQLite updates →
   local re-emits → UI refreshes.

### Connection lost — reconnecting

Cache is untouched. UI continues to show the last known state. The user sees
the builds as they were before the connection dropped.

### Reconnect success

The server sends `build.snapshot` with two lists:

- `activeBuilds` — builds currently in-flight on the IDE side.
- `recentResults` — builds that **completed while the client was disconnected**.

`SnapshotEvent.foldBuilds()` replaces the entire build map with snapshot data.
`SnapshotEvent.foldLogs()` clears the log map (fresh session).

`RemoteActiveBuildSource` emits the new list → `CachedReadableDataSource`
calls `local.save()` → SQLite updates → UI sees the authoritative server state.

Builds that were in the local cache but do not appear in either list are
considered vanished and will not be refreshed.

### Permanent disconnect

`BuildCacheInvalidator` observes `ConnectionManager.state`:

```kotlin
class BuildCacheInvalidator(
    connectionManager: ConnectionManager,
    private val localBuilds: MutableDataSource<Unit, List<BuildSnapshot>>,
    private val localLogs: MutableDataSource<String, List<BuildLogEntry>>,
    dispatchers: AppDispatchers,
)
```

When the connection transitions to a terminal state (`Disconnected`, `Failed`),
the invalidator calls `localBuilds.delete(Unit)`. CASCADE delete in SQL
removes all log entries. UI sees an empty list.

The invalidator does **not** clear during `Reconnecting` — builds remain
visible while the system attempts to restore the connection.

---

## 12. Protocol Change (IDE Plugin)

### Problem

When the mobile client is disconnected and a build completes on the IDE side,
the `build.result` message is lost (no client to receive it). On reconnect, the
server sends `build.snapshot` — but completed builds are no longer "active", so
they are absent from the snapshot. The client's cached build simply disappears
from the UI with no explanation.

### Solution

Extend `BuildSnapshotPayload` with a `recentResults` field:

```kotlin
@Serializable
@SerialName("build.snapshot")
data class BuildSnapshotPayload(
    val activeBuilds: List<ActiveBuildInfo>,
    val recentResults: List<BuildResult> = emptyList(),
) : WsPayload()
```

**Server-side (`BuildSnapshotProvider`):** when a build completes, keep the
`BuildResult` in a "recently completed" list. On `sys.hello` from the client,
include both `activeBuilds` and `recentResults` in the snapshot. Results are
retained for a configurable duration (e.g. 10 minutes) or until the next build
starts for the same project.

**Client-side (`BuildEventMapper`):** `SnapshotEvent` carries both lists. The
`SnapshotEvent.foldBuilds()` inserts recent results as `Finished` entries,
so the user sees the outcome instead of a vanishing card.

**Files to update:**

- `ide-plugin/.../serialization/WsPayload.kt` — add `recentResults` field
- `ide-plugin/.../build/BuildSnapshotProvider.kt` — retain recently completed
  builds
- `mobile-app/core/data/.../protocol/WsPayload.kt` — add `recentResults` field
  (mirror of plugin)

---

## 13. Design Patterns Summary

| Pattern | Where applied | Why |
|---|---|---|
| **CachedReadableDataSource** (project) | `ActiveBuildCachedSource`, `BuildLogCachedSource` | Standard composition from `core:cache`: remote writes into local, local is single source of truth, UI only sees local. Two independent pipelines for builds and logs. |
| **Scenario C** (project) | `LocalActiveBuildSource`, `LocalBuildLogSource` | Per-entity typed SQL tables behind `MutableDataSource`. SQLDelight DAOs used directly, no `CacheStore`. Follows `core:cache` README exactly. |
| **Polymorphic fold** | `BuildEvent.foldBuilds()`, `BuildEvent.foldLogs()` | Each event type carries its own fold behavior. Remote sources accumulate state via pure functional fold without `when`. |
| **Mapper** (project convention) | `BuildEventMapper`, `BuildRecordMapper`, `LogRecordMapper`, `OutcomeToJsonMapper`, `JsonToOutcomeMapper` | All boundary transformations via `Mapper<T, S>` from `core:common`. |
| **ISP** (SOLID) | `ReadableDataSource` / `WritableDataSource` / `MutableDataSource` / `CancelBuildUseCase` | Consumers depend only on the contract they need. Repository sees `ReadableDataSource`. CachedReadableDataSource sees `MutableDataSource`. Invalidator sees `WritableDataSource.delete()`. |
| **DIP** (SOLID) | Repository depends on `CachedReadableDataSource` abstractions | The repository never sees `ActiveBuildQueries`, `BuildLogQueries`, or any SQLDelight type. |
| **SRP** (SOLID) | Mapper maps. Remote folds. Local persists. CachedReadableDataSource composes. Repository delegates. | Each class has exactly one reason to change. |
| **OCP** (SOLID) | `BuildEvent` sealed hierarchy | Adding a new build event type: (1) add sealed subtype with fold methods, (2) add mapping case in `BuildEventMapper`. No existing code is modified. |

---

## 14. Package Structure

```
feature/active-builds/
│
├── README.md                       ← this document
│
├── build.gradle.kts
│
└── src/
    ├── commonMain/
    │   ├── kotlin/me/yuriisoft/buildnotify/mobile/feature/activebuilds/
    │   │   │
    │   │   ├── data/
    │   │   │   ├── model/
    │   │   │   │   ├── BuildRecord.kt              data class — active_build row
    │   │   │   │   ├── BuildRecordStatus.kt         enum — ACTIVE, SUCCESS, FAILED, CANCELLED
    │   │   │   │   ├── BuildLogRecord.kt            data class — build_log row
    │   │   │   │   └── LogRecordKind.kt             enum — TASK, WARNING, ERROR
    │   │   │   │
    │   │   │   ├── remote/
    │   │   │   │   ├── RemoteActiveBuildSource.kt   ReadableDataSource — folds events into builds
    │   │   │   │   └── RemoteBuildLogSource.kt      ReadableDataSource — folds events into logs
    │   │   │   │
    │   │   │   ├── local/
    │   │   │   │   ├── LocalActiveBuildSource.kt    MutableDataSource — SQL read/write for builds
    │   │   │   │   ├── LocalBuildLogSource.kt       MutableDataSource — SQL read/write for logs
    │   │   │   │   ├── ActiveBuildCachedSource.kt   CachedReadableDataSource — composes builds
    │   │   │   │   └── BuildLogCachedSource.kt      CachedReadableDataSource — composes logs
    │   │   │   │
    │   │   │   ├── sync/
    │   │   │   │   ├── BuildCacheInvalidator.kt     @AppScope — clears cache on disconnect
    │   │   │   │   └── DefaultCancelBuildUseCase.kt CancelBuildUseCase impl — sends command
    │   │   │   │
    │   │   │   └── mapper/
    │   │   │       ├── BuildEventMapper.kt          Mapper<WsPayload, BuildEvent?> — boundary
    │   │   │       ├── BuildRecordMapper.kt         Mapper<BuildRecord, BuildSnapshot>
    │   │   │       ├── LogRecordMapper.kt           Mapper<BuildLogRecord, BuildLogEntry>
    │   │   │       ├── OutcomeToJsonMapper.kt       Mapper<BuildOutcome, String>
    │   │   │       └── JsonToOutcomeMapper.kt       Mapper<String, BuildOutcome>
    │   │   │
    │   │   ├── domain/
    │   │   │   ├── model/
    │   │   │   │   ├── BuildSnapshot.kt             sealed interface — Active / Finished
    │   │   │   │   ├── BuildOutcome.kt              data class — final build result
    │   │   │   │   ├── FinishStatus.kt              enum — SUCCESS, FAILED, CANCELLED
    │   │   │   │   ├── BuildIssue.kt                data class — error/warning entry
    │   │   │   │   ├── BuildLogEntry.kt             data class — single log line
    │   │   │   │   ├── LogKind.kt                   enum — TASK, WARNING, ERROR
    │   │   │   │   ├── BuildEvent.kt                sealed interface + all subtypes
    │   │   │   │   └── SnapshotBuild.kt             data class — build info from snapshot
    │   │   │   │
    │   │   │   ├── repository/
    │   │   │   │   └── IActiveBuildRepository.kt    observeBuilds(), observeLogs()
    │   │   │   │
    │   │   │   └── usecase/
    │   │   │       └── CancelBuildUseCase.kt        interface — invoke(buildId)
    │   │   │
    │   │   └── di/
    │   │       └── ActiveBuildsComponent.kt         kotlin-inject bindings
    │   │
    │   └── sqldelight/me/yuriisoft/buildnotify/mobile/feature/activebuilds/
    │       ├── ActiveBuild.sq                       active_build table + queries
    │       └── BuildLog.sq                          build_log table + queries
    │
    └── commonTest/
        └── kotlin/me/yuriisoft/buildnotify/mobile/feature/activebuilds/
            ├── FakeRemoteActiveBuildSource.kt       fake remote for build tests
            ├── FakeRemoteBuildLogSource.kt          fake remote for log tests
            └── ...                                  unit tests per class
```
