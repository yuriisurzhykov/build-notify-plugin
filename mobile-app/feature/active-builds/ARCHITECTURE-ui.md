# Active Builds — UI Architecture (Presentation Layer)

> This document covers the **presentation layer only** — Composables, ViewModel, UI state,
> screen intents, and one-shot events. For the data/domain layer (repositories, SQLDelight,
> WebSocket event folding, cache lifecycle), see [`README.md`](README.md).

---

## Table of Contents

1. [Module promotion: `kmp-library` → `cmp-library`](#1-module-promotion)
2. [Guiding principles](#2-guiding-principles)
3. [Layer diagram](#3-layer-diagram)
4. [UI State hierarchy](#4-ui-state-hierarchy)
    - 4.1 [Top-level `ActiveBuildsUiState`](#41-top-level-activebuildsuis tate)
    - 4.2 [
      `BuildListItem` — self-rendering sealed items](#42-buildlistitem--self-rendering-sealed-items)
    - 4.3 [Leaf value objects](#43-leaf-value-objects)
5. [Screen Intents](#5-screen-intents)
6. [One-shot Events](#6-one-shot-events)
7. [ViewModel](#7-viewmodel)
    - 7.1 [Reactive pipeline — `observeBuilds`](#71-reactive-pipeline--observebuilds)
    - 7.2 [Stop-build command path](#72-stop-build-command-path)
    - 7.3 [Error handling contract](#73-error-handling-contract)
8. [Composable architecture](#8-composable-architecture)
    - 8.1 [File structure](#81-file-structure)
    - 8.2 [`ActiveBuildsScreen` — entry point](#82-activebuildsscreen--entry-point)
    - 8.3 [
      `ActiveBuildsContent` — root stateless composable](#83-activebuildscontentRoot-stateless-composable)
    - 8.4 [Card components](#84-card-components)
    - 8.5 [Shared primitive components](#85-shared-primitive-components)
9. [Component contracts (full API)](#9-component-contracts-full-api)
10. [`@Stable` / `@Immutable` annotation map](#10-stable--immutable-annotation-map)
11. [DI wiring](#11-di-wiring)
12. [Key design decisions](#12-key-design-decisions)
13. [Mock reference](#13-mock-reference)

---

## 1. Module Promotion

`build.gradle.kts` must be promoted from `kmp-library` to `cmp-library` to unlock
Compose Multiplatform. New/changed dependencies:

```kotlin
plugins {
    id("cmp-library")          // was: kmp-library
    alias(libs.plugins.sqldelight)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            // existing data-layer deps remain unchanged
            implementation(project(":core:common"))
            implementation(project(":core:cache"))
            implementation(project(":core:data"))
            implementation(project(":core:network"))

            // presentation layer additions
            implementation(project(":core:ui"))
            implementation(project(":core:navigation"))
            implementation(libs.lifecycle.viewmodel.compose)
            implementation(libs.kotlin.inject.runtime)
            implementation(libs.kotlinx.collections.immutable)
        }
        commonMain.kspDependencies {
            ksp(libs.kotlin.inject.ksp)
        }
    }
}
```

`kotlinx-collections-immutable` is the sole new runtime dependency for the UI layer.
Every `List<T>` field in a UI model becomes `ImmutableList<T>` — this is what enables
Compose's smart recomposition skip when a list reference hasn't changed.

---

## 2. Guiding Principles

These principles from the team's code-quality rules are applied throughout this feature.

### Immutability by default

Every type that participates in Compose is either `data class` with all-`val` fields or a
`data object`. `MutableStateFlow` / `MutableSharedFlow` exist only inside `ViewModel` —
they are never passed into Composables.

### Reactive pipelines as the primary abstraction

The ViewModel exposes two `Flow`s and nothing else:

```
uiState:  StateFlow<ActiveBuildsUiState>   — current screen state
uiEvents: Flow<ActiveBuildsUiEvent>        — one-shot navigation / toast signals
```

State is derived by a `runningFold` / `flatMapLatest` / `combine` chain. There are no
imperative `var` accumulators outside that chain.

### Polymorphism over branching — `BuildListItem.Content()`

`LazyColumn` does **not** contain a `when` block. It calls one method:

```kotlin
item.Content(onIntent = onIntent)
```

Each sealed subtype (`RunningBuildItem`, `FailedBuildItem`, `RecentBuildItem`) dispatches
to its own Composable. Adding a new card type means adding a new subtype — zero changes
at the call site.

### Objects over data bags — sealed `ActiveTask` and `IssueLocation`

Instead of a nullable `String?` for the current running task, we use a sealed type:

```kotlin
sealed interface ActiveTask {
    data object None : ActiveTask
    data class Running(val taskName: String) : ActiveTask
}
```

`ActiveTaskRow` never receives a nullable argument. `None` renders nothing via early
return inside the component. This keeps the Composable signature honest about what it
can receive.

### No raw lambdas at the ViewModel boundary

All user actions flow through a single typed `onIntent(ActiveBuildsIntent)` entry point.
`ActiveBuildsScreen` passes `vm::onIntent` as one lambda all the way down. This makes the
contract between screen and ViewModel explicit and refactoring-safe.

---

## 3. Layer Diagram

```
┌──────────────────────────────────────────────────────────────┐
│  ActiveBuildsScreen  (lifecycle owner, event collector)       │
│    └─ ActiveBuildsContent  (stateless, state: UiState)        │
│         ├─ StreamErrorBanner                                  │
│         └─ LazyColumn                                         │
│              ├─ RunningBuildItem.Content()                    │
│              │    └─ RunningBuildCard                         │
│              │         ├─ BuildCardShell                      │
│              │         ├─ ActiveTaskRow                       │
│              │         ├─ InlineConsole                       │
│              │         │    └─ LogLine × N                    │
│              │         └─ SecondaryButton / GhostButton       │
│              ├─ FailedBuildItem.Content()                     │
│              │    └─ FailedBuildCard                          │
│              │         ├─ BuildCardShell                      │
│              │         ├─ BuildIssueRow × N                   │
│              │         └─ PrimaryButton (Ask AI)              │
│              └─ RecentBuildItem.Content()                     │
│                   └─ RecentBuildCard                          │
│                        ├─ BuildStatusBadge                    │
│                        └─ BuildElapsedBadge                   │
├──────────────────────────────────────────────────────────────┤
│  ActiveBuildsViewModel                                        │
│    observeBuilds() — flatMapLatest + combine + map            │
│    onIntent(ActiveBuildsIntent)                               │
├──────────────────────────────────────────────────────────────┤
│  IActiveBuildRepository  (domain contract)                    │
│    observeBuilds(): Flow<List<BuildSnapshot>>                 │
│    observeLogs(buildId): Flow<List<BuildLogEntry>>            │
│  CancelBuildUseCase  (domain contract)                        │
└──────────────────────────────────────────────────────────────┘
```

Data flows **down** (state → Composables), intents flow **up** (Composables → ViewModel).
Events flow out of the ViewModel via `uiEvents` and are collected in `ActiveBuildsScreen`.

---

## 4. UI State Hierarchy

### 4.1 Top-level `ActiveBuildsUiState`

```kotlin
@Immutable
sealed interface ActiveBuildsUiState {

    /** First load — before any data arrives from the repository. */
    @Immutable
    data object Loading : ActiveBuildsUiState

    /**
     * At least one build snapshot exists.
     *
     * [streamError] is always present — [StreamErrorUi.NoError] when the stream is healthy,
     * [StreamErrorUi.Error] when the stream failed while we already had data.
     * This avoids nullable fields and ensures [Loaded] is always fully constructed.
     */
    @Immutable
    data class Loaded(
        val items: ImmutableList<BuildListItem>,
        val autoSyncEnabled: Boolean,
        val streamError: StreamErrorUi,
    ) : ActiveBuildsUiState

    /** Connected to IDE, but no builds exist at all (not even finished ones). */
    @Immutable
    data object Empty : ActiveBuildsUiState
}
```

**Why three states instead of two?**

`Empty` is distinct from `Loading` so that the UI can show a meaningful "No active
builds" empty-body illustration rather than an indefinite spinner once the data has
loaded and the list is genuinely empty.

#### `StreamErrorUi`

```kotlin
@Immutable
sealed interface StreamErrorUi {
    data object NoError : StreamErrorUi

    @Immutable
    data class Error(val message: TextResource) : StreamErrorUi
}
```

`StreamErrorUi` is always present in `Loaded`. When the stream crashes after data was
already displayed, the ViewModel preserves the last `items` list and replaces `NoError`
with `Error`. The user sees stale data with an inline banner rather than a blank screen.
See §7.3 for the full error-handling contract.

---

### 4.2 `BuildListItem` — self-rendering sealed items

```kotlin
@Stable
sealed interface BuildListItem {

    /** Stable, globally unique key for LazyColumn item tracking. */
    val listKey: String

    /** Content-type hint — enables LazyColumn slot reuse across items of the same type. */
    val contentType: BuildItemContentType

    /**
     * The item renders itself. The caller never pattern-matches on the concrete type.
     * Adding a new card variant = adding a new subtype here. Zero call-site changes.
     */
    @Composable
    fun Content(onIntent: (ActiveBuildsIntent) -> Unit)
}

enum class BuildItemContentType { RUNNING, FAILED, RECENT }
```

The `listKey` prefixes guarantee uniqueness across types. A running build and a recent
build with the same `buildId` will have different keys: `"running_$buildId"` vs
`"recent_$buildId"`. This prevents the LazyColumn animation system from confusing items
that transition from one state to another.

#### `RunningBuildItem`

Represents a build currently in progress. `currentTask` uses `ActiveTask` (sealed) to
avoid a nullable `String?`.

```kotlin
@Immutable
data class RunningBuildItem(
    val buildId: String,
    val gradleTask: String,
    val projectName: String,
    val currentTask: ActiveTask,
    val recentLogs: ImmutableList<LogLineUi>,
    val elapsedMs: Long,
) : BuildListItem {
    override val listKey = "running_$buildId"
    override val contentType = BuildItemContentType.RUNNING

    @Composable
    override fun Content(onIntent: (ActiveBuildsIntent) -> Unit) {
        RunningBuildCard(item = this, onIntent = onIntent)
    }
}

@Immutable
sealed interface ActiveTask {
    data object None : ActiveTask
    @Immutable
    data class Running(val taskName: String) : ActiveTask
}
```

#### `FailedBuildItem`

Represents a build that finished with `FinishStatus.FAILED`. Carries a pre-mapped list
of `BuildIssueUi` — the ViewModel is responsible for mapping `BuildIssue` (domain) to
`BuildIssueUi` (UI).

```kotlin
@Immutable
data class FailedBuildItem(
    val buildId: String,
    val gradleTask: String,
    val projectName: String,
    val errors: ImmutableList<BuildIssueUi>,
    val durationMs: Long,
) : BuildListItem {
    override val listKey = "failed_$buildId"
    override val contentType = BuildItemContentType.FAILED

    @Composable
    override fun Content(onIntent: (ActiveBuildsIntent) -> Unit) {
        FailedBuildCard(item = this, onIntent = onIntent)
    }
}
```

#### `RecentBuildItem`

Represents any successfully completed or cancelled build in the recent history.

```kotlin
@Immutable
data class RecentBuildItem(
    val buildId: String,
    val gradleTask: String,
    val projectName: String,
    val status: FinishStatus,
    val startedAt: Long,
    val durationMs: Long,
) : BuildListItem {
    override val listKey = "recent_$buildId"
    override val contentType = BuildItemContentType.RECENT

    @Composable
    override fun Content(onIntent: (ActiveBuildsIntent) -> Unit) {
        RecentBuildCard(item = this, onIntent = onIntent)
    }
}
```

---

### 4.3 Leaf value objects

#### `LogLineUi`

```kotlin
@Immutable
data class LogLineUi(
    val text: String,
    val kind: LogKind,   // TASK | WARNING | ERROR — non-nullable enum
)
```

`LogKind` maps directly from the domain's `LogKind` enum. No transformation needed at
render time — the enum value carries the color decision.

#### `BuildIssueUi`

```kotlin
@Immutable
data class BuildIssueUi(
    val message: String,
    val location: IssueLocation,
)

@Immutable
sealed interface IssueLocation {
    data object Unknown : IssueLocation
    @Immutable
    data class Known(val filePath: String, val line: Int) : IssueLocation
}
```

`IssueLocation` replaces the domain's nullable `filePath: String?` / `line: Int?` pair.
The `BuildIssueRow` Composable receives a `BuildIssueUi` and never null-checks anything.
`IssueLocation.Unknown` renders nothing below the message via early return inside the
component.

---

## 5. Screen Intents

All user actions are modeled as a single sealed hierarchy. There are no ad-hoc lambdas
at the ViewModel boundary. The ViewModel exposes one method: `onIntent(ActiveBuildsIntent)`.

```kotlin
@Immutable
sealed interface ActiveBuildsIntent {

    /** User tapped "Stop Build" on a running card. */
    @Immutable
    data class StopBuild(val buildId: String) : ActiveBuildsIntent

    /** User tapped ">_ Logs" — navigate to full-screen log viewer. */
    @Immutable
    data class OpenLogs(val buildId: String) : ActiveBuildsIntent

    /** User tapped a recent build card — navigate to build detail. */
    @Immutable
    data class OpenBuildDetail(val buildId: String) : ActiveBuildsIntent

    /** User tapped "Retry" on the inline stream error banner. */
    data object RetryStream : ActiveBuildsIntent

    /** User tapped "Ask AI to Fix Error" on a failed card (future; no-op today). */
    @Immutable
    data class AskAiToFix(val buildId: String) : ActiveBuildsIntent
}
```

**Why a sealed intent hierarchy instead of individual lambdas?**

- A single `onIntent: (ActiveBuildsIntent) -> Unit` lambda is passed from
  `ActiveBuildsScreen` down through `ActiveBuildsContent` to every card. No lambda
  proliferation at card boundaries.
- The ViewModel's `when(intent)` is the single exhaustive dispatch point. Adding a new
  user action = adding one sealed subtype + one `when` branch.
- Intent objects are serializable as data classes — trivial to log, replay in tests, or
  record for analytics without modifying the ViewModel signature.

---

## 6. One-shot Events

Navigation and transient toasts are not part of `uiState`. They are modeled as one-shot
events emitted from the ViewModel and collected exactly once in `ActiveBuildsScreen`.

```kotlin
@Immutable
sealed interface ActiveBuildsUiEvent {

    @Immutable
    data class NavigateToLogs(val buildId: String) : ActiveBuildsUiEvent
    @Immutable
    data class NavigateToBuildDetail(val buildId: String) : ActiveBuildsUiEvent
    @Immutable
    data class ShowErrorToast(val message: TextResource) : ActiveBuildsUiEvent
}
```

**Why not put navigation in `uiState`?**

Navigation is a command, not state. If it were in `uiState`, a configuration change
would re-collect the state and re-navigate. One-shot events via `EventCommunication`
(backed by `Channel`) guarantee exactly-once delivery regardless of recomposition.

---

## 7. ViewModel

**File:** `presentation/ActiveBuildsViewModel.kt`

```kotlin
@Inject
class ActiveBuildsViewModel(
    private val repository: IActiveBuildRepository,
    private val cancelBuild: CancelBuildUseCase,
    private val dispatchers: AppDispatchers,
    private val logger: Logger,
    private val state: StateCommunication.Mutable<ActiveBuildsUiState> =
        StateCommunication(ActiveBuildsUiState.Loading),
    private val events: EventCommunication.Mutable<ActiveBuildsUiEvent> =
        EventCommunication(),
) : ViewModel() {

    val uiState: StateFlow<ActiveBuildsUiState> = state.observe
    val uiEvents: Flow<ActiveBuildsUiEvent> = events.observe

    init {
        observeBuilds()
    }

    fun onIntent(intent: ActiveBuildsIntent) = when (intent) {
        is StopBuild       -> stopBuild(intent.buildId)
        is OpenLogs        -> events.put(NavigateToLogs(intent.buildId))
        is OpenBuildDetail -> events.put(NavigateToBuildDetail(intent.buildId))
        is AskAiToFix      -> { /* future */
        }
        RetryStream        -> observeBuilds()
    }
}
```

`StateCommunication` and `EventCommunication` are thin wrappers from `core:common` that
expose `StateFlow` / `Channel`-backed `Flow` and a `put()` method. They are passed as
constructor parameters so tests can inject fakes without needing `Turbine` or
reflection.

### 7.1 Reactive pipeline — `observeBuilds`

```kotlin
private var observeJob: Job? = null

private fun observeBuilds() {
    observeJob?.cancel()
    observeJob = repository.observeBuilds()
        .flatMapLatest { snapshots ->
            // For each active build, open a logs sub-flow; combine all into one emission.
            val activeIds = snapshots
                .filterIsInstance<BuildSnapshot.Active>()
                .map { it.buildId }

            if (activeIds.isEmpty()) {
                flowOf(snapshots to emptyMap())
            } else {
                combine(activeIds.map { id ->
                    repository.observeLogs(id).map { logs -> id to logs }
                }) { pairs ->
                    snapshots to pairs.toMap()
                }
            }
        }
        .map { (snapshots, logsMap) ->
            if (snapshots.isEmpty()) {
                ActiveBuildsUiState.Empty
            } else {
                ActiveBuildsUiState.Loaded(
                    items = snapshots.toUiItems(logsMap).toImmutableList(),
                    autoSyncEnabled = true,
                    streamError = StreamErrorUi.NoError,
                )
            }
        }
        .catch { e ->
            logger.e(e, "Active builds stream failed")
            val errorBanner = StreamErrorUi.Error(
                TextResource.ResText(Res.string.error_builds_stream)
            )
            val current = state.observe.value
            state.put(
                if (current is ActiveBuildsUiState.Loaded)
                    current.copy(streamError = errorBanner)
                else
                    ActiveBuildsUiState.Loaded(
                        items = persistentListOf(),
                        autoSyncEnabled = true,
                        streamError = errorBanner,
                    )
            )
        }
        .onEach { newState -> state.put(newState) }
        .launchIn(viewModelScope)
}
```

**Pipeline stages in plain English:**

| Stage                              | What it does                                                                                          |
|------------------------------------|-------------------------------------------------------------------------------------------------------|
| `repository.observeBuilds()`       | Emits the current list of `BuildSnapshot` every time any build changes.                               |
| `flatMapLatest { snapshots -> … }` | Each new snapshot list tears down the previous inner flow. Combines log flows for every active build. |
| `combine(…)`                       | Merges N per-build log flows into one emission whenever any log updates.                              |
| `map { … }`                        | Transforms domain snapshots + logs into `ActiveBuildsUiState`.                                        |
| `.catch { … }`                     | Stream failure: preserve last `Loaded` state, replace `streamError`.                                  |
| `.onEach { state.put(it) }`        | Single side-effect point — writes to state.                                                           |

**Why `flatMapLatest` and not `switchMap` / `collectLatest`?**

`flatMapLatest` cancels the inner `combine` flow as soon as the upstream emits a new
snapshot list. If a build finishes (transitions from Active → Finished), the log
sub-flow for that build is immediately cancelled and a new inner flow without it is
started. This keeps the number of active coroutines bounded to the number of active
builds.

### 7.2 Stop-build command path

Commands (imperative actions, not state subscriptions) are handled with `try/catch`
rather than `catch {}` on a flow, because they are fire-and-forget, not streaming.

```kotlin
private fun stopBuild(buildId: String) {
    dispatchers.launchBackground(viewModelScope) {
        try {
            cancelBuild(buildId)
        } catch (e: CancellationException) {
            throw e        // always re-throw structured concurrency cancellation
        } catch (e: Exception) {
            logger.e(e, "Cancel build $buildId failed")
            events.put(ShowErrorToast(TextResource.ResText(Res.string.error_cancel_build)))
        }
    }
}
```

`CancellationException` is re-thrown unconditionally. Catching it would break structured
concurrency — the coroutine would become a zombie.

### 7.3 Error handling contract

```
┌──────────────────────────────────────────────────────────────┐
│  Error source           │  Where surfaced       │  Mechanism │
├─────────────────────────┼───────────────────────┼────────────┤
│  observeBuilds() crash  │  Inline StreamError   │ .catch →   │
│  observeLogs() crash    │  Banner inside Loaded │  state.put │
│  cancelBuild() throws   │  ShowErrorToast event │  try/catch │
│  Any throwable          │  Logger (always)      │  logger.e  │
└──────────────────────────────────────────────────────────────┘
```

- **Stream errors** — the banner appears above the list. Stale items remain visible.
  "Retry" intent (`RetryStream`) cancels the job and re-invokes `observeBuilds()`.
- **Command errors** — transient toast, no state change. The list continues to live-update.
- **Full `Throwable`** is always logged — even if the error is handled gracefully in the
  UI, the full stack trace is recorded for crash-reporting tooling.

---

## 8. Composable Architecture

### 8.1 File structure

```
feature/active-builds/src/commonMain/kotlin/…/activebuilds/
│
├── presentation/
│     ├── ActiveBuildsScreen.kt          ← lifecycle owner; collects events
│     ├── ActiveBuildsViewModel.kt
│     ├── ActiveBuildsUiState.kt         ← sealed UiState + BuildListItem + leaf models
│     ├── ActiveBuildsIntent.kt
│     └── ActiveBuildsUiEvent.kt
│
├── ui/
│     ├── ActiveBuildsContent.kt         ← root stateless composable
│     ├── RunningBuildCard.kt
│     ├── FailedBuildCard.kt
│     └── RecentBuildCard.kt
│
├── ui/components/
│     ├── BuildCardShell.kt              ← shared ElevatedSurface wrapper
│     ├── ActiveTaskRow.kt               ← running task name + LinearProgress
│     ├── BuildIssueRow.kt               ← error/warning row
│     ├── InlineConsole.kt               ← bounded-height CodeSurface + log list
│     ├── LogLine.kt                     ← single log line, color from LogKind
│     ├── BuildElapsedBadge.kt           ← "1m 45s" formatted duration
│     ├── BuildStatusBadge.kt            ← FinishStatus → status-colored icon surface
│     └── StreamErrorBanner.kt           ← inline error + Retry button
│
└── di/
      └── ActiveBuildsComponent.kt
```

### 8.2 `ActiveBuildsScreen` — entry point

`ActiveBuildsScreen` is the only Composable that knows the `ViewModel` exists. It
collects `uiEvents` and delegates rendering to the stateless `ActiveBuildsContent`.

```kotlin
@Composable
fun ActiveBuildsScreen(
    viewModel: ActiveBuildsViewModel = viewModel(),
    navigator: Navigator,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.uiEvents.collect { event ->
            when (event) {
                is NavigateToLogs        -> navigator.navigate(LogsRoute(event.buildId))
                is NavigateToBuildDetail -> navigator.navigate(BuildDetailRoute(event.buildId))
                is ShowErrorToast        -> /* show snackbar / toast */ Unit
            }
        }
    }

    ActiveBuildsContent(
        state = state,
        onIntent = viewModel::onIntent,
    )
}
```

**The `when` in `LaunchedEffect` is legitimate** — this is a UI boundary where one sealed
hierarchy must be dispatched to N different navigation actions. It is the single
`when` allowed per concept (rule §3).

### 8.3 `ActiveBuildsContent` — root stateless composable

```kotlin
@Composable
internal fun ActiveBuildsContent(
    state: ActiveBuildsUiState,
    onIntent: (ActiveBuildsIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    when (state) {
        is ActiveBuildsUiState.Loading -> {
            Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgress(Indeterminate)
            }
        }
        is ActiveBuildsUiState.Empty   -> {
            EmptyBody(
                icon = BuildIcon,
                message = stringResource(Res.string.no_active_builds),
                modifier = modifier,
            )
        }
        is ActiveBuildsUiState.Loaded  -> LoadedContent(state, onIntent, modifier)
    }
}
```

`LoadedContent` is a private composable that renders the section header, the optional
`StreamErrorBanner`, and the `LazyColumn`:

```kotlin
@Composable
private fun LoadedContent(
    state: ActiveBuildsUiState.Loaded,
    onIntent: (ActiveBuildsIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier) {
        if (state.streamError is StreamErrorUi.Error) {
            StreamErrorBanner(
                error = state.streamError,
                onRetry = { onIntent(ActiveBuildsIntent.RetryStream) },
            )
        }

        SectionHeader(
            title = stringResource(Res.string.active_builds),
            badge = if (state.autoSyncEnabled) stringResource(Res.string.auto_sync_on) else null,
        )

        LazyColumn {
            items(
                items = state.items,
                key = { it.listKey },
                contentType = { it.contentType },
            ) { item ->
                item.Content(onIntent = onIntent)
            }
        }
    }
}
```

The `LazyColumn` has no `when`. `item.Content()` dispatches polymorphically to the
correct card. `key` and `contentType` enable correct diff-and-animate behavior and
slot reuse.

### 8.4 Card components

`FlatRow` is used for every horizontal layout inside cards. It keeps the composable tree
completely flat — every child is a **direct descendant** of the `Layout` node with no
intermediate `Row` or `Column` wrappers. Column widths are fixed by `FlatRowSlot.weight`,
so conditionally present children (e.g. `CircularProgress` only during an active build)
do not shift sibling positions.

#### `RunningBuildCard`

Renders a build in progress. Visible in the first mock screenshot.

```
BuildCardShell (no status border)
  ├── FlatRow [header row]           slots: [0.85f Vertical] [0.15f Center/End]
  │     ├── Text: gradleTask         [titleMedium]         → slot(0)
  │     ├── Text: projectName        [bodySmall, secondary] → slot(0)
  │     └── CircularProgress(Indet.) [size = Small]         → slot(1)
  ├── ActiveTaskRow(item.currentTask)
  ├── InlineConsole(item.recentLogs)
  └── FlatRow [action row]           slots: [0.5f Center] [0.5f Center]
        ├── SecondaryButton("Stop Build", StopIcon, error tint) → slot(0)
        └── GhostButton(">_ Logs")                              → slot(1)
```

```kotlin
@Composable
internal fun RunningBuildCard(
    item: RunningBuildItem,
    onIntent: (ActiveBuildsIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    BuildCardShell(modifier = modifier) {
        // ── Header ──────────────────────────────────────────────────────────
        FlatRow(
            slots = listOf(
                FlatRowSlot(weight = 0.85f, arrangement = SlotArrangement.Vertical),
                FlatRowSlot(weight = 0.15f, horizontalAlignment = Alignment.End),
            ),
            horizontalSpacing = 8.dp,
            verticalSpacing = 2.dp,
        ) {
            Text(item.gradleTask, style = titleMedium, modifier = Modifier.slot(0))
            Text(
                item.projectName,
                style = bodySmall,
                color = secondary,
                modifier = Modifier.slot(0)
            )
            CircularProgress(Indeterminate, size = Small, modifier = Modifier.slot(1))
        }

        ActiveTaskRow(task = item.currentTask)
        InlineConsole(logs = item.recentLogs)

        // ── Actions ─────────────────────────────────────────────────────────
        FlatRow(
            slots = listOf(
                FlatRowSlot(weight = 0.5f, horizontalAlignment = Alignment.CenterHorizontally),
                FlatRowSlot(weight = 0.5f, horizontalAlignment = Alignment.CenterHorizontally),
            ),
            horizontalSpacing = 8.dp,
        ) {
            SecondaryButton(
                text = stringResource(Res.string.stop_build),
                icon = StopIcon,
                tint = colors.status.error.main,
                onClick = { onIntent(StopBuild(item.buildId)) },
                modifier = Modifier.slot(0),
            )
            GhostButton(
                text = stringResource(Res.string.logs),
                onClick = { onIntent(OpenLogs(item.buildId)) },
                modifier = Modifier.slot(1),
            )
        }
    }
}
```

**`SecondaryButton` for "Stop Build"** — destructive actions must not visually compete
with the constructive primary flow. `PrimaryButton` is reserved for constructive primary
actions (e.g., "Ask AI" on the failed card). The stop button is tinted `status.error.main`
for discoverability without dominating the card.

**Why two `FlatRow`s instead of one `Column` + two `Row`s?** Each `FlatRow` replaces one
`Row` + one `Column` pair, reducing the composable tree by two layout nodes per card.
The header and action rows are separate `FlatRow` instances because they have different
slot weight distributions; combining them into a single multi-section layout would couple
unrelated layout decisions.

---

#### `FailedBuildCard`

Renders a build that ended in `FAILED`. Visible in the first mock screenshot.

```
BuildCardShell (statusBorderColor = status.error.main)
  ├── FlatRow [header row]         slots: [0.85f Vertical] [0.15f Center/End]
  │     ├── Text: gradleTask       [titleMedium]            → slot(0)
  │     ├── Text: projectName      [bodySmall, secondary]   → slot(0)
  │     └── StatusIcon(error)      [size = Small]           → slot(1)
  ├── Divider
  ├── BuildIssueRow(errors[0..2])  ← max 3; "… N more" label when errors.size > 3
  └── PrimaryButton("Ask AI to Fix Error", AiIcon, fillMaxWidth)
```

```kotlin
@Composable
internal fun FailedBuildCard(
    item: FailedBuildItem,
    onIntent: (ActiveBuildsIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    BuildCardShell(
        modifier = modifier,
        statusBorderColor = colors.status.error.main,
    ) {
        // ── Header ──────────────────────────────────────────────────────────
        FlatRow(
            slots = listOf(
                FlatRowSlot(weight = 0.85f, arrangement = SlotArrangement.Vertical),
                FlatRowSlot(weight = 0.15f, horizontalAlignment = Alignment.End),
            ),
            horizontalSpacing = 8.dp,
            verticalSpacing = 2.dp,
        ) {
            Text(item.gradleTask, style = titleMedium, modifier = Modifier.slot(0))
            Text(
                item.projectName,
                style = bodySmall,
                color = secondary,
                modifier = Modifier.slot(0)
            )
            StatusIcon(StatusRole.Error, size = Small, modifier = Modifier.slot(1))
        }

        Divider()

        // ── Errors (max 3 visible) ───────────────────────────────────────
        item.errors.take(3).forEach { issue -> BuildIssueRow(issue) }
        if (item.errors.size > 3) {
            Text(
                text = stringResource(Res.string.n_more_errors, item.errors.size - 3),
                style = labelSmall,
                color = colors.content.tertiary,
            )
        }

        PrimaryButton(
            text = stringResource(Res.string.ask_ai_to_fix),
            icon = AiIcon,
            onClick = { onIntent(AskAiToFix(item.buildId)) },
            modifier = Modifier.fillMaxWidth(),
        )
    }
}
```

The left border (`statusBorderColor`) is drawn via `Modifier.drawBehind` on
`BuildCardShell`, **not** via a nested `Surface`. This avoids an extra composable node
and an extra draw pass.

---

#### `RecentBuildCard`

Renders a finished build (success or cancelled) in the "Recent Builds" section. Visible
in the second mock screenshot.

```
BuildCardShell (no border, onClick = OpenBuildDetail)
  └── FlatRow                      slots: [auto] [1f Vertical] [auto End] [auto End]
        ├── BuildStatusBadge       [fixed intrinsic width]   → slot(0)
        ├── Text: gradleTask       [titleSmall]               → slot(1)
        ├── Text: projectName      [bodySmall, secondary]     → slot(1)
        ├── BuildElapsedBadge      [end-aligned]              → slot(2)
        ├── Text: relative time    [labelSmall, tertiary]     → slot(2)
        └── Icon(ChevronRight)     [tertiary, fixed width]    → slot(3)
```

```kotlin
@Composable
internal fun RecentBuildCard(
    item: RecentBuildItem,
    onIntent: (ActiveBuildsIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    BuildCardShell(
        modifier = modifier,
        onClick = { onIntent(OpenBuildDetail(item.buildId)) },
    ) {
        FlatRow(
            slots = listOf(
                FlatRowSlot(weight = 0.10f, horizontalAlignment = Alignment.Start),
                FlatRowSlot(weight = 0.55f, arrangement = SlotArrangement.Vertical),
                FlatRowSlot(
                    weight = 0.28f, arrangement = SlotArrangement.Vertical,
                    horizontalAlignment = Alignment.End
                ),
                FlatRowSlot(weight = 0.07f, horizontalAlignment = Alignment.End),
            ),
            horizontalSpacing = 12.dp,
            verticalSpacing = 2.dp,
        ) {
            BuildStatusBadge(item.status, modifier = Modifier.slot(0))
            Text(item.gradleTask, style = titleSmall, modifier = Modifier.slot(1))
            Text(
                item.projectName,
                style = bodySmall,
                color = secondary,
                modifier = Modifier.slot(1)
            )
            BuildElapsedBadge(item.durationMs, modifier = Modifier.slot(2))
            Text(
                item.startedAt.toRelativeTimeString(), style = labelSmall,
                color = colors.content.tertiary, modifier = Modifier.slot(2)
            )
            Icon(ChevronRight, tint = colors.content.tertiary, modifier = Modifier.slot(3))
        }
    }
}
```

**Why four slots instead of `Row` + nested `Column`s?**
A conventional layout would require: outer `Row` → `BuildStatusBadge` + inner `Column`
(title/subtitle) + inner `Column` (badge/time) + chevron `Icon`. That is **5 composable
nodes** for a single list row. `FlatRow` collapses this to **1 layout node** — every
child (`Text`, `BuildStatusBadge`, `BuildElapsedBadge`, `Icon`) is placed directly.
For a `LazyColumn` that may render dozens of items, the node-count saving is significant.

---

### 8.5 Shared primitive components

These live in `ui/components/` and are reused across all three card types. Every
component that has an internal horizontal layout uses `FlatRow` instead of `Row` +
`Column` nesting.

#### `BuildCardShell`

The foundation composable for all cards. Wraps content in `ElevatedSurface` from
`core:ui` and optionally draws a colored left border. Its `content` lambda is
`ColumnScope` — cards stack their direct children (`FlatRow`, `ActiveTaskRow`,
`InlineConsole`, buttons) vertically inside the shell.

```kotlin
@Composable
fun BuildCardShell(
    modifier: Modifier = Modifier,
    statusBorderColor: Color = Color.Unspecified,   // Unspecified = no border rendered
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
)
```

The left border is drawn with `Modifier.drawBehind { drawRect(color, size = borderRect) }`.
`Color.Unspecified` is checked once before drawing — no border node, no extra paint call.

#### `ActiveTaskRow`

Displays the currently running Gradle task name and a `LinearProgress(Indeterminate)`
beneath it. Uses `FlatRow` with a single slot — both the task `Text` and the
`LinearProgress` share slot 0 in `Vertical` arrangement, so they stack without a
`Column` wrapper.

```kotlin
@Composable
fun ActiveTaskRow(
    task: ActiveTask,
    modifier: Modifier = Modifier,
)
```

```kotlin
// Implementation sketch
if (task == ActiveTask.None) return

FlatRow(
    slots = listOf(FlatRowSlot(weight = 1f, arrangement = SlotArrangement.Vertical)),
    modifier = modifier,
) {
    Text(
        text = (task as ActiveTask.Running).taskName,
        style = bodySmall,
        color = colors.status.info.main,
        modifier = Modifier.slot(0),
    )
    LinearProgress(Indeterminate, modifier = Modifier.slot(0).fillMaxWidth())
}
```

When `task == ActiveTask.None` the function returns immediately — no layout node is
emitted. No `if (task != null)` at the call site.

#### `InlineConsole`

A bounded-height `CodeSurface` containing a `LazyColumn` of `LogLine`s. The
`CodeSurface` itself is not a `FlatRow` participant — it is a standalone `Surface`
variant. Inside it, `LogLine`s are laid out by `LazyColumn`, not `FlatRow`, because they
are scrollable list items, not fixed-width columns.

```kotlin
@Composable
fun InlineConsole(
    logs: ImmutableList<LogLineUi>,
    modifier: Modifier = Modifier,
    maxVisibleLines: Int = 4,
)
```

Auto-scroll:

```kotlin
val listState = rememberLazyListState()
LaunchedEffect(logs.size) {
    if (logs.isNotEmpty()) listState.animateScrollToItem(logs.lastIndex)
}
```

`LaunchedEffect(logs.size)` fires only when a new line is appended — not on every
recomposition.

#### `LogLine`

A single `Text` composable with `code.regular` style. No layout wrapper needed — the
`LazyColumn` inside `InlineConsole` positions it directly.

| `LogKind` | Color token                  |
|-----------|------------------------------|
| `TASK`    | `colors.content.onCode`      |
| `WARNING` | `colors.status.warning.main` |
| `ERROR`   | `colors.status.error.main`   |

```kotlin
@Composable
fun LogLine(
    line: LogLineUi,
    modifier: Modifier = Modifier,
)
```

#### `BuildIssueRow`

Renders one compiler diagnostic. Uses `FlatRow` with two slots: a fixed-width icon slot
and a text slot. `IssueLocation.Known` places a second `Text` in the same text slot
(`Vertical` arrangement) — no `Column` wrapper is needed.

```kotlin
@Composable
fun BuildIssueRow(
    issue: BuildIssueUi,
    modifier: Modifier = Modifier,
)
```

```kotlin
// Implementation sketch
FlatRow(
    slots = listOf(
        FlatRowSlot(weight = 0.08f, horizontalAlignment = Alignment.CenterHorizontally),
        FlatRowSlot(weight = 0.92f, arrangement = SlotArrangement.Vertical),
    ),
    horizontalSpacing = 6.dp,
    verticalSpacing = 2.dp,
    modifier = modifier,
) {
    StatusIcon(StatusRole.Error, size = Small, modifier = Modifier.slot(0))
    Text(issue.message, style = bodySmall, modifier = Modifier.slot(1))
    if (issue.location is IssueLocation.Known) {
        Text(
            text = "${issue.location.filePath}:${issue.location.line}",
            style = labelSmall,
            color = colors.content.tertiary,
            modifier = Modifier.slot(1),
        )
    }
}
```

`IssueLocation.Unknown` → no second `Text` is emitted. The icon slot still reserves its
`0.08f` width, keeping the message column stable regardless of whether a location is present.

#### `BuildElapsedBadge`

Formats a duration in milliseconds and wraps it in the existing `Badge` component from
`core:ui`. A single leaf composable — no internal layout.

```kotlin
@Composable
fun BuildElapsedBadge(
    elapsedMs: Long,
    modifier: Modifier = Modifier,
)
```

Format: `"${minutes}m ${seconds}s"` when ≥ 60 seconds, `"${seconds}s"` otherwise.

#### `BuildStatusBadge`

Maps `FinishStatus` to a status-colored `Badge` with an icon. A single leaf composable —
no internal layout.

| `FinishStatus` | Icon         | Color role       |
|----------------|--------------|------------------|
| `SUCCESS`      | `CheckIcon`  | `status.success` |
| `FAILED`       | `CloseIcon`  | `status.error`   |
| `CANCELLED`    | `CancelIcon` | `status.warning` |

```kotlin
@Composable
fun BuildStatusBadge(
    status: FinishStatus,
    modifier: Modifier = Modifier,
)
```

Uses `StatusIcon` from `core:ui/icons/StatusIcons.kt`. The color decision is local to
this component — no `when` leaks to the call site.

#### `StreamErrorBanner`

Rendered at the top of the `Loaded` content area when `streamError is StreamErrorUi.Error`.
Uses `FlatRow` to keep the icon, message, and retry button flat — no `Surface` → `Row`
→ children nesting.

```kotlin
@Composable
fun StreamErrorBanner(
    error: StreamErrorUi.Error,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
)
```

```kotlin
// Implementation sketch
Surface(color = colors.status.error.container, modifier = modifier) {
    FlatRow(
        slots = listOf(
            FlatRowSlot(weight = 0.08f, horizontalAlignment = Alignment.CenterHorizontally),
            FlatRowSlot(weight = 0.72f, arrangement = SlotArrangement.Vertical),
            FlatRowSlot(weight = 0.20f, horizontalAlignment = Alignment.End),
        ),
        horizontalSpacing = 8.dp,
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp),
    ) {
        StatusIcon(StatusRole.Error, size = Small, modifier = Modifier.slot(0))
        Text(error.message.resolve(), style = bodySmall, modifier = Modifier.slot(1))
        GhostButton(
            stringResource(Res.string.retry),
            onClick = onRetry, modifier = Modifier.slot(2)
        )
    }
}
```

The `Surface` wraps a single `FlatRow` — one extra node for the background color is
unavoidable, but the internal content stays flat.

---

## 9. Component Contracts (full API)

| Component             | Parameters                                            | Emits intents           |
|-----------------------|-------------------------------------------------------|-------------------------|
| `ActiveBuildsContent` | `state`, `onIntent`, `modifier`                       | delegates to cards      |
| `RunningBuildCard`    | `item: RunningBuildItem`, `onIntent`, `modifier`      | `StopBuild`, `OpenLogs` |
| `FailedBuildCard`     | `item: FailedBuildItem`, `onIntent`, `modifier`       | `AskAiToFix`            |
| `RecentBuildCard`     | `item: RecentBuildItem`, `onIntent`, `modifier`       | `OpenBuildDetail`       |
| `BuildCardShell`      | `modifier`, `statusBorderColor`, `onClick`, `content` | —                       |
| `ActiveTaskRow`       | `task: ActiveTask`, `modifier`                        | —                       |
| `InlineConsole`       | `logs`, `modifier`, `maxVisibleLines`                 | —                       |
| `LogLine`             | `line: LogLineUi`, `modifier`                         | —                       |
| `BuildIssueRow`       | `issue: BuildIssueUi`, `modifier`                     | —                       |
| `BuildElapsedBadge`   | `elapsedMs: Long`, `modifier`                         | —                       |
| `BuildStatusBadge`    | `status: FinishStatus`, `modifier`                    | —                       |
| `StreamErrorBanner`   | `error: StreamErrorUi.Error`, `onRetry`, `modifier`   | calls `onRetry`         |

All `modifier` parameters default to `Modifier`. All components are `internal` except
`ActiveBuildsScreen` (public entry point for navigation graph registration).

---

## 10. `@Stable` / `@Immutable` Annotation Map

The Compose compiler uses these annotations to decide whether to skip recomposition for
a given parameter. Incorrect or missing annotations force Compose to always recompose.

| Type                                                     | Annotation   | Reason                                                    |
|----------------------------------------------------------|--------------|-----------------------------------------------------------|
| `ActiveBuildsUiState` (all variants)                     | `@Immutable` | Pure data, all fields `val`, never mutated after creation |
| `BuildListItem` sealed interface                         | `@Stable`    | Has a `@Composable` method; equality is well-defined      |
| `RunningBuildItem`, `FailedBuildItem`, `RecentBuildItem` | `@Immutable` | `data class`, all fields immutable                        |
| `ActiveBuildsIntent` (all variants)                      | `@Immutable` | Pure command objects, all-`val` data classes              |
| `ActiveBuildsUiEvent` (all variants)                     | `@Immutable` | Same                                                      |
| `LogLineUi`, `BuildIssueUi`                              | `@Immutable` | Value objects                                             |
| `IssueLocation` sealed + variants                        | `@Immutable` | Value objects                                             |
| `ActiveTask` sealed + variants                           | `@Immutable` | Value objects                                             |
| `StreamErrorUi` sealed + variants                        | `@Immutable` | Value objects                                             |

**Why `@Stable` and not `@Immutable` on `BuildListItem`?**

`@Immutable` requires that all publicly observable properties are immutable. Because
`BuildListItem` declares an abstract `@Composable fun Content(…)`, which is not a
property, using `@Immutable` would be semantically incorrect. `@Stable` correctly
expresses that equality is well-defined and the compiler may skip recomposition when
equality holds.

---

## 11. DI Wiring

`ActiveBuildsComponent` uses `kotlin-inject` and is added to `AppComponent`'s supertypes.

```kotlin
interface ActiveBuildsComponent {

    @Provides
    fun activeBuildsViewModel(
        repository: IActiveBuildRepository,
        cancelBuild: CancelBuildUseCase,
        dispatchers: AppDispatchers,
        logger: Logger,
    ): ActiveBuildsViewModel = ActiveBuildsViewModel(
        repository = repository,
        cancelBuild = cancelBuild,
        dispatchers = dispatchers,
        logger = logger,
    )

    @IntoSet
    @Provides
    fun activeBuildsScreen(screen: ActiveBuildsScreen): Screen = screen
}
```

`Logger` is injected as a constructor parameter — not accessed as a global singleton.
This is intentional: the ViewModel is fully testable without any static state. On KMP
today `Logger` wraps `println`; it can be swapped for Crashlytics `recordException`
without changing the ViewModel.

---

## 12. Key Design Decisions

### `BuildListItem.Content(onIntent)` — polymorphic self-rendering

The `LazyColumn` calls one method, `item.Content(onIntent)`. No `when` block exists at
the list level. Adding a new card variant (e.g., `CancelledBuildItem`) requires:

1. Add the new `BuildListItem` subtype.
2. Implement `Content()` in the new type.
3. Update the ViewModel's `toUiItems()` mapper.

Zero changes to `ActiveBuildsContent` or the `LazyColumn` call site.

### `ImmutableList<T>` throughout all UI models

`kotlinx-collections-immutable` provides `ImmutableList` which the Compose compiler
recognizes as stable. This enables smart recomposition skip when a list reference
hasn't changed. A plain `List<T>` is always treated as unstable by the Compose compiler,
even if its contents didn't change.

### `listKey` prefix prevents animation artifacts

When a build transitions from `FAILED` to `RECENT`, its `buildId` is the same but it
becomes a different `BuildListItem` subtype. Without prefixed keys, the `LazyColumn`
would attempt to animate the `FailedBuildCard` slot into a `RecentBuildCard` slot,
producing visual corruption. The prefix (`"failed_"` vs `"recent_"`) ensures the old
item is removed and the new item is inserted, triggering a clean appearance animation.

### `ActiveTask` sealed over nullable `String?`

`ActiveTaskRow` has no nullable parameter. `ActiveTask.None` renders nothing via early
return. This eliminates a class of null-pointer-at-render-time bugs and makes the
component signature self-documenting about what it accepts.

### `IssueLocation` sealed over nullable `filePath` / `line`

Same principle applied to `BuildIssueRow`. The absence of a file location is an explicit
domain concept, not a missing field.

### Stream errors preserve stale data

When `observeBuilds()` throws, the last known `items` are kept in `Loaded`. The user
continues to see the last known build state with a banner explaining the connection
problem. A blank screen on a connection blip would be a significantly worse UX.

### Intent-first API

One `onIntent(intent)` entry point. Every button, every tap, every keyboard action
goes through this function. `ActiveBuildsScreen` passes `vm::onIntent` as a single
lambda to `ActiveBuildsContent`. No callback proliferation across composables.

### Error split: stream → inline banner, command → toast

Stream errors are ongoing conditions — they deserve persistent visibility (inline banner).
Command errors (`cancelBuild` failure) are transient operational failures — a toast is
appropriate and clears itself. Conflating these into one mechanism would either make
transient errors too persistent or make ongoing conditions too dismissible.

### `Logger` as a constructor dependency

The ViewModel logs every error via an injected `Logger` interface. This ensures that
errors are always recorded even when they are handled gracefully in the UI, and that
the logging mechanism can be replaced (e.g., Crashlytics) without modifying the ViewModel.

---

## 13. Mock Reference

The two mock screenshots guide the visual design of the components.

**Mock 1 — Active Builds list (10:36)**

- `RunningBuildCard`: `app:assembleDebug` / Project: E-commerce App
    - Active task: `:app:compileDebugJavaWithJavac` at 68%
    - `InlineConsole` showing 3 log lines (TASK, TASK, WARNING)
    - `SecondaryButton("Stop Build")` + `GhostButton(">_ Logs")`
- `FailedBuildCard`: `core:testDebugUnitTest` / Project: Payment SDK
    - Left border in error red
    - `StatusIcon(error)` in top-right corner
    - Error message: "Unresolved reference: PaymentProcessor in PaymentService.kt:42"
    - `PrimaryButton("Ask AI to Fix Error")` full-width, purple gradient

**Mock 2 — AI Suggestions + Recent Builds (10:37)**

- `AI Suggestions` section with accept/reject diff card (out of scope for this feature)
- `Recent Builds` section:
    - `RecentBuildCard`: `app:assembleRelease` — green `BuildStatusBadge` (SUCCESS),
      duration `1m 45s`, relative time `2 mins ago`, chevron-right
    - `RecentBuildCard`: `ui:testDebug` — green `BuildStatusBadge` (SUCCESS),
      duration `45s`, relative time `15 mins ago`, chevron-right
- Bottom navigation: Builds (selected) / Network / AI Agent / Settings
