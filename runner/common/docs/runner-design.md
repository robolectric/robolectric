# Robolectric Runner Architecture — Design Doc

## 0. TL;DR — what changed for users

- **JUnit 4 users (`@RunWith(RobolectricTestRunner::class)`)**: nothing changes. The legacy
  runner is untouched; the two paths coexist.
- **JUnit Jupiter users**: adopt `@ExtendWith(RobolectricExtension::class)`, or rely on the
  Jupiter `TestEngine` being auto service-loaded. `@Before`/`@After` become
  `@BeforeEach`/`@AfterEach`; `@BeforeClass`/`@AfterClass` become `@BeforeAll`/`@AfterAll`.
- **Pure JUnit Platform (`@org.junit.Test` without `@RunWith`)**: the new
  `RobolectricTestEngine` discovers `@Test` directly.
- **Framework authors**: implement `RobolectricIntegration` (≤6 methods); everything else —
  sandbox lifecycle, SDK selection, environment bootstrap, manifest resolution, parameter
  injection, logging, metrics — is handled by `:runner:common`.
- **API stability**: every new public symbol is annotated `@ExperimentalRunnerApi`.

---

## 1. Motivation

Robolectric historically ships a single runner (`RobolectricTestRunner`) targeting JUnit 4
via `@RunWith`. That couples the sandbox lifecycle, SDK selection, manifest resolution,
classloader bootstrapping, and JUnit 4 specifics into one class. Consequences:

- **Framework lock-in.** Running Robolectric under the JUnit Platform, JUnit Jupiter, Kotest,
  TestNG, or a custom engine requires reimplementing the same sandbox plumbing against
  Robolectric internals.
- **Hidden lifecycle.** There is no supported surface for `@BeforeAll`/`@AfterAll`-style
  shared class environments, multi-SDK discovery, or parameter injection.
- **Opaque failures.** SDK filtering (`-Drobolectric.enabledSdks`) silently drops tests when
  no SDK survives the filter.
- **No observability.** No first-class logging/metrics for sandbox reuse, SDK selection, or
  per-phase timing.
- **No custom classloader bridge.** Frameworks that transform test bytecode cannot hand their
  byte stream to the Robolectric sandbox classloader.

This commit introduces a new runner architecture split across three Gradle modules that
addresses all of the above.

## 2. Goals / Non-goals

### Goals
- A **framework-agnostic core** (`:runner:common`): sandbox lifecycle, discovery helpers,
  parameter resolver, logging/metrics, classloader bridge.
- Two **reference integrations**: `:runner:junit-platform` (a native JUnit Platform
  `TestEngine`) and `:runner:junit-jupiter` (`RobolectricExtension` + `RobolectricJupiterEngine`).
- Keep the public surface **explicitly experimental** so the API can evolve.
- Make **SDK/sandbox discovery failures fail-fast** with diagnostic detail.
- Offer **configurable sandbox sharing** (`PER_TEST`, `PER_CLASS`, `PER_SDK`, `GLOBAL`).

### Non-goals
- Replace the existing JUnit 4 `RobolectricTestRunner`.
- Change Robolectric's bytecode instrumentation, shadow system, or Android bootstrap.
- Introduce new public API in `:robolectric` — `:runner:common` keeps an `implementation`
  dependency and reflects into `SdkCollection` where needed (see §7.3).

## 3. Module layout

```mermaid
graph LR
    root["settings.gradle.kts"]
    common[":runner:common<br/>framework-agnostic core<br/>no JUnit compile deps"]
    platform[":runner:junit-platform<br/>TestEngine for pure JUnit 4"]
    jupiter[":runner:junit-jupiter<br/>Extension + TestEngine<br/>for JUnit Jupiter"]
    root --> common
    root --> platform
    root --> jupiter
    platform -. depends on .-> common
    jupiter -. depends on .-> common
```

Cross-cutting changes outside these modules:

- **`:sandbox`** — `ClassBytesSource.java` (new thread-local hook) and
  `SandboxClassLoader.getClassBytesFromAlternateClassLoader` now consulting that hook. This
  is what lets custom-classloader frameworks hand their bytecode to the sandbox.
- **`:testapp`** — new fixtures used by the integration tests: `StatefulActivity`,
  `ConfigChangeTestActivity`, matching `layout/` and `layout-land/` resources,
  `AndroidManifest.xml` entries.
- **`integration_tests/ctesque/.../PackageManagerTest.java`** — softened to
  `assertThat(activities.length).isAtLeast(…)` + `assertContainsActivity(…)`. This is a
  direct consequence of the new testapp activities: the old positional assertions were
  brittle and broke when `testapp`'s manifest grew. The doc records this so a future
  bisect doesn't treat it as an unrelated test edit.

## 4. High-level architecture

```mermaid
graph TD
    fw["Test framework<br/>JUnit Platform / Jupiter / custom"]

    subgraph facade[High-level facade]
        integration["RobolectricIntegration<br/>beforeClass / beforeTest<br/>afterTest / afterClass<br/>executeInSandbox"]
    end

    subgraph cfg[Configuration and construction]
        default["DefaultRobolectricIntegration<br/>+ SandboxSharingStrategy"]
        builder["RobolectricIntegrationBuilder"]
        runnercfg["RunnerConfiguration"]
    end

    subgraph core[Lifecycle primitives]
        slm["SandboxLifecycleManager"]
        rse["RobolectricSandboxExecutor<br/>low-level twin"]
    end

    subgraph collab[Collaborators in runner:common]
        deps["RobolectricDependencies"]
        clm["ClassLifecycleManager"]
        mr["ManifestResolver"]
        sc["SandboxConfigurator"]
        env["RobolectricEnvironment"]
        tb["TestBootstrapper"]
        lh["LifecycleHelper"]
        dh["DiscoveryHelpers"]
        pr["ParameterResolver<br/>ParameterResolutionHelper"]
        obs["RunnerLogger / RunnerMetrics"]
        sps["SystemPropertiesSupport"]
    end

    fw --> integration
    integration --> default
    builder -. builds .-> default
    runnercfg -. builds .-> default
    default --> slm
    default --> rse
    slm --> deps
    slm --> clm
    slm --> mr
    slm --> sc
    slm --> env
    rse --> slm
    env --> tb
    env --> lh
    clm --> env
    integration --> dh
    integration --> pr
    integration --> obs
    slm --> sps
```

## 5. Design decisions

### 5.1 `RobolectricIntegration`: the façade

The central abstraction is a **single interface** with five lifecycle callbacks and one
block-style execution method:

```kotlin
interface RobolectricIntegration {
  fun beforeClass(testClass: Class<*>)
  fun beforeTest(testClass: Class<*>, testMethod: Method)
  fun afterTest(testClass: Class<*>, testMethod: Method, success: Boolean)
  fun afterClass(testClass: Class<*>)
  fun getContext(testClass: Class<*>): TestExecutionContext?
  fun <T> executeInSandbox(
    testClass: Class<*>, testMethod: Method, block: (TestExecutionContext) -> T
  ): T
}
```

Every supported framework can be modeled as a sequence of class/test entry-and-exit events
plus "run this block inside the sandbox." Mapping a new framework becomes a small adapter
instead of a rewrite.

### 5.2 Two-level API: low-level executor + high-level integration

`:runner:common` deliberately exposes **two** entry points at different abstraction levels:

| Level        | Class                         | Used by                             | Scope                          |
| ------------ | ----------------------------- | ----------------------------------- | ------------------------------ |
| Low-level    | `RobolectricSandboxExecutor`  | `RobolectricTestEngine` (Platform)  | one test → one sandbox         |
| Low-level    | `SandboxLifecycleManager`     | `RobolectricJupiterEngine`, `RobolectricExtension` | sandbox create/execute primitives |
| High-level   | `RobolectricIntegration`      | framework adapters, most consumers  | full lifecycle, caching, sharing |

The Jupiter extension path uses the low-level primitives directly because it must
participate in Jupiter's own `ExtensionContext` and `InvocationInterceptor` protocols —
the façade's lifecycle calls would clash with them. The Platform engine (simpler model:
"given a test, run it") uses `RobolectricSandboxExecutor` directly. Framework authors
outside this commit are expected to start from the façade.

### 5.3 Configuration: `RobolectricIntegrationBuilder` + `RunnerConfiguration`

One canonical builder, one value DTO — no more overlap.

- **`RobolectricIntegrationBuilder`** is the canonical constructor. Fluent API with presets
  (`forJUnitPlatform()`, `forJUnitJupiter()`, `fromSystemProperties()`). Directly builds a
  `DefaultRobolectricIntegration`. Accepts `FrameworkClassLoadingBridge`, `LifecycleAnnotations`,
  and `TestFilter`. `build()` applies observability side-effects (`RunnerLogger.isDebugEnabled`,
  `RunnerMetrics.enable()`) — construction of the builder itself is side-effect-free.
- **`RunnerConfiguration`** is the transferable DTO. It carries the same fields plus an
  optional `classLoadingBridge`. `createIntegration()` / `createIntegration(deps)` delegate to
  `RobolectricIntegrationBuilder().fromConfig(this).build()`. Call `validate()` to check the
  `timingEnabled ⇒ metricsEnabled` invariant — validation is explicit rather than in an
  `init { }` block so DTO construction stays side-effect-free too.

Rule of thumb: if you're composing an integration imperatively, use the builder; if you're
producing a config that will be stashed, serialized, or passed across modules, use
`RunnerConfiguration` and hand it to the builder via `fromConfig`.

### 5.4 Sandbox sharing strategies + `SandboxSharingPolicy`

```kotlin
enum class SandboxSharingStrategy { PER_TEST, PER_CLASS, PER_SDK, GLOBAL }
```

| Strategy    | Cache key                          | Isolation | Speed   | Multi-SDK  | Use case                               |
| ----------- | ---------------------------------- | --------- | ------- | ---------- | -------------------------------------- |
| `PER_TEST`  | `"${class.name}.${method.name}"`   | maximum   | slowest | any        | diagnostic runs, state-heavy tests     |
| `PER_CLASS` | `Class<*>` identity                | per class | medium  | same class | **default** — balances isolation/speed |
| `PER_SDK`   | `sdk.apiLevel` (Int)               | per SDK   | fast    | yes        | large suites with many SDKs            |
| `GLOBAL`    | `@Volatile` single field           | none      | fastest | single SDK | micro-benchmarks, tight control        |

The strategy maps to an internal `sealed interface SandboxSharingPolicy` with one class per
value (`PerTestPolicy`, `PerClassPolicy`, `PerSdkPolicy`, `GlobalPolicy`). Each policy owns:

- Its own cache (`ConcurrentHashMap` keyed as in the table above; `GlobalPolicy` uses a
  `@Volatile` field + synchronized initializer).
- Its own `beforeClass` / `beforeTest` / `afterTest` / `afterClass` logic.
- Its own `RunnerMetrics` cache-hit / cache-miss / create / teardown events.

`DefaultRobolectricIntegration` now delegates each lifecycle callback straight to the policy —
no more four-way `when` branches spread across the class. The façade keeps only cross-strategy
concerns: `RunnerLogger.debug` events, `executeInSandbox` on the sandbox main thread, and
`InvocationTargetException` unwrapping. The policy is internal; framework adapters go through
the façade or the low-level primitives, never the policy directly.

### 5.5 Classloader bridge: three layers

Frameworks like Kotest transform test bytecode and load it through their own classloaders.
Robolectric's sandbox still needs to *define* those classes itself so instrumentation
applies. The commit introduces a three-layer bridge:

```mermaid
graph TD
    spi["FrameworkClassLoadingBridge<br/>public SPI in runner:common<br/>resolveSourceClassLoader<br/>openClassBytes"]
    ctx["FrameworkClassLoadingContext<br/>thread-local holder in runner:common<br/>withContext(bridge, cl)"]
    cbs["ClassBytesSource<br/>thread-local hook in sandbox<br/>setProvider / restoreProvider"]
    scl["SandboxClassLoader<br/>getClassBytesFromAlternateClassLoader"]

    spi -->|consumed by| ctx
    ctx -->|push on entry, pop in finally| cbs
    cbs -->|read during class definition| scl
```

Critically, **`FrameworkClassLoadingContext` is the only code that writes to
`ClassBytesSource`**. `RobolectricEnvironment.access` wraps every sandbox-side block in
`FrameworkClassLoadingContext.withContext(bridge, sourceClassLoader) { block() }`, so the
thread-local is always balanced with its caller frame. Framework integrators should not
touch `ClassBytesSource` directly.

### 5.6 `SandboxLifecycleManager` and contexts

`SandboxLifecycleManager` separates three responsibilities:

- **Creation.** `createSandbox` returns a single `SandboxContext`; `createSandboxes` returns
  one per selected SDK (multi-SDK parameterization). Inside creation,
  `withContextClassLoader(sourceClassLoader) { sandboxManager.getAndroidSandbox(…) }`
  temporarily installs the framework's source classloader as the thread context classloader
  so the sandbox manager can resolve classpath URLs from it. This is a *separate* mechanism
  from the byte-bridge in §5.5 — that one applies while the *test* runs, this one applies
  while the *sandbox* is being constructed.
- **Configuration.** A collaborator `SandboxConfigurator` handles the "how to configure it"
  question (looper mode, SQLite mode, graphics mode, resources mode, shadow map,
  interceptors), decoupled from "which sandbox" decisions.
- **Execution.** `executeInSandbox(context, testName, configuration?)` builds (or reuses) a
  `RobolectricEnvironment` and forwards to `environment.executeInSandbox`, which acquires a
  re-entrant lock via `access { }`.

### 5.7 `ClassLifecycleManager`: class-scoped environments with per-method override

JUnit Jupiter's `@BeforeAll`/`@AfterAll` and JUnit 4's `@BeforeClass`/`@AfterClass` need a
sandbox that survives across a whole class. `ClassLifecycleManager` owns one map of
`ClassState(context, environment)` records (a single `ConcurrentHashMap` — the two maps that
used to drift out of sync are gone) and exposes:

- `setupForClass` — creates or reuses a class-level `SandboxContext`, initializes the
  Android environment once.
- `tearDownForClass` — tears the environment down on the sandbox main thread.
- `executeInClassContext(testClass, methodName, configuration = null, block)` — the key
  primitive:
  - `configuration == null` → block runs inside the **persistent** class environment
    (reused across `@BeforeAll` / tests / `@AfterAll`).
  - `configuration != null` → block runs in an **isolated per-method environment** that
    still reuses the class-level sandbox/classloader. This is how the Jupiter engine
    honors method-level `@Config(...)` overrides without leaking state into the class
    environment.

### 5.8 `LifecycleAnnotations`: runtime-resolved, compile-decoupled

`LifecycleAnnotations` is an enum (`JUNIT4`, `JUNIT5`, `JUNIT_COMBINED`, `NONE`) that stores
annotation **class names** and resolves them to `Class<out Annotation>` on first use via
`Class.forName(name)`, swallowing `ClassNotFoundException`. This is the mechanism that keeps
`:runner:common` free of JUnit compile dependencies while still shipping JUnit presets —
without it, the module couldn't reference `org.junit.jupiter.api.BeforeEach.class` at all.

### 5.9 Discovery & SDK parameterization

`DiscoveryHelpers` is the **only** discovery surface in `:runner:common` and deliberately
framework-agnostic:

- `isTestMethod` / `discoverTestMethods(testClass, testAnnotations)` / `discoverTestMethodsByName`
  — the last accepts annotation **class names** so Jupiter and JUnit 4 share the same
  discovery path.
- `createSdkVariants(testClass, method, deps, baseUniqueId, alwaysIncludeSdkInName)` —
  expands each test method into one `SdkTestVariant` per SDK the `SdkPicker` returns.

JUnit-Platform-specific descriptor construction (`TestDescriptor`, `UniqueId`,
`MethodSource`, `EngineDescriptor`) lives **in the engine modules**
(`PlatformDescriptorBuilders`, `JupiterDescriptorBuilders`), not in `:runner:common`.

**SDK test name format.** `SystemPropertiesSupport.formatTestName` appends `[<sdk>]` when
either (a) `-Drobolectric.alwaysIncludeVariantMarkersInTestName=true` or (b) this is not
the last SDK. Concretely, with SDKs `[30, 33, 34]` selected and default properties:

| Variant | Display name       | Unique-id segment |
| ------- | ------------------ | ----------------- |
| SDK 30  | `testFoo[30]`      | `sdk:30`          |
| SDK 33  | `testFoo[33]`      | `sdk:33`          |
| SDK 34  | `testFoo`          | `sdk:34`          |

The last-variant omission is deliberate — it preserves IDE click-to-navigate on single-SDK
runs. Setting `alwaysIncludeVariantMarkersInTestName=true` forces all three to carry the
marker.

### 5.10 Observability: `RunnerLogger` and `RunnerMetrics`

Two zero-dependency singletons keyed on system properties:

- `robolectric.runner.debug` → `RunnerLogger` emits structured events (test start/end, SDK
  selection, sandbox creation, class context create/reuse/teardown, SDK fallback warnings).
- `robolectric.runner.metrics` → `RunnerMetrics` counts sandbox creations, teardowns, cache
  hits/misses, test successes/failures.
- `robolectric.runner.metrics.timing` → per-phase timings
  (`PHASE_SANDBOX_CREATION`, `PHASE_ENVIRONMENT_SETUP`, `PHASE_ENVIRONMENT_TEARDOWN`,
  `PHASE_CLASS_SETUP`, `PHASE_CLASS_TEARDOWN`, `PHASE_TEST_EXECUTION`). A `timed(phase) { … }`
  helper wraps blocks so callers don't manage clock state.

Both singletons are framework-agnostic and used uniformly by `DefaultRobolectricIntegration`,
`SandboxLifecycleManager`, and both engines.

### 5.11 `SdkFallbackResolver`: one place for the enabledSdks fallback

When `-Drobolectric.enabledSdks` filters every SDK out of `SdkPicker.selectSdks`, the runner
needs a fallback. The same reflection-into-`SdkCollection` logic used to live (byte-for-byte)
in both `SandboxLifecycleManager.createSandboxes` and the Jupiter engine's
`createFallbackSdk`. It now lives in one place:

```kotlin
SdkFallbackResolver.resolveFallbackSdk(deps, testClass, testMethod, config): Sdk?
```

- Returns the first `@Config(sdk = …)` entry looked up through `SdkCollection.getSdk(int)`.
- If `@Config(sdk)` is empty, returns the latest known SDK from `SdkCollection.getKnownSdks()`.
- Returns `null` only if `SdkCollection` itself cannot be loaded (hard failure — callers
  should throw with diagnostic detail).
- Emits a stderr `WARNING:` and a `RunnerLogger.logSdkFallback` entry in the first two cases
  so the divergence is observable.

`SandboxLifecycleManager.createSandboxes`, `RobolectricJupiterEngine.selectMethodSdkWithFallback`,
and `RobolectricTestEngine`'s legacy-method branch all call this helper now. When
`SdkCollection` moves to `:pluginapi`, the reflection inside the helper goes away; the call
sites stay the same.

### 5.12 `TestMethodInvoker`: one "bootstrap + @Before + invoke + @After" helper

Three separate copies of the same bootstrap/invoke/lifecycle pattern used to live in the
Platform engine, the Jupiter engine, and `RobolectricSandboxExecutor.executeTest`. They are
now a single call:

```kotlin
TestMethodInvoker.invoke(
  sandbox, testClass, testMethod,
  beforeEachAnnotations, afterEachAnnotations,
  parameterResolver,
)
```

The helper:
1. Bootstraps `testClass` through `TestBootstrapper`.
2. Invokes every `@BeforeEach`/`@Before`-style method the caller lists (order handled by
   `LifecycleHelper`).
3. Invokes the test method, resolving parameters via the supplied `ParameterResolver` (which
   defaults to `DefaultRobolectricParameterResolver`). `InvocationTargetException` is
   unwrapped so assertion errors propagate naturally.
4. Runs `@AfterEach`/`@After`-style methods in reverse order inside a `finally` block.
   Failures here are logged via `RunnerLogger.error` and do not mask a test-body failure.

Callers are responsible for arranging execution on the sandbox main thread — the helper is
stateless and doesn't schedule on `sandbox.runOnMainThread` so callers can decide whether to
wrap one method or many.

### 5.13 `ParameterResolver`: now end-to-end

Previously the `ParameterResolver` SPI existed but was effectively dead: the Jupiter engine
hardcoded `DefaultRobolectricParameterResolver.resolveParameter(...)` directly, and
`ParameterResolutionHelper.resolveParameters` did not accept a resolver argument.

Now every link in the chain respects the injected resolver:

- `ParameterResolutionHelper.resolveParameters(parameters, sandbox, resolver)` takes a
  `ParameterResolver` parameter (defaulting to the Robolectric built-in). Unknown parameter
  types produce a clear `IllegalArgumentException` that names the offending type and points
  users at the builder.
- `TestMethodInvoker.invoke(..., parameterResolver = …)` forwards the resolver.
- Both engines accept a constructor-level `parameterResolver` (defaulting to the built-in) and
  pass it to `TestMethodInvoker`. Framework authors using a custom `RobolectricExtension`-ish
  adapter can plug in their own resolver.

### 5.14 `DiscoveryHelpers.isTestMethod`: extensible for non-JUnit frameworks

`DiscoveryHelpers.isTestMethod(method, annotationNames)` accepts a `Set<String>` of
fully-qualified annotation class names. The default is `DEFAULT_TEST_ANNOTATION_NAMES` (JUnit 4
+ Jupiter). Kotest/TestNG-style frameworks can union their own names with the default:

```kotlin
val names = DiscoveryHelpers.DEFAULT_TEST_ANNOTATION_NAMES + "io.kotest.core.annotation.Test"
DiscoveryHelpers.isTestMethod(method, names)
```

Matching is by FQCN, not `Class` identity — this makes discovery robust across classloader
boundaries (framework-transformed classes still resolve).

## 6. Implementation details

### 6.1 Thread/concurrency model

| Site                                                | Thread requirement                                                                                              |
| --------------------------------------------------- | --------------------------------------------------------------------------------------------------------------- |
| `RobolectricDependencies.create()`                  | any thread                                                                                                      |
| `SandboxLifecycleManager.createSandbox(es)`         | any thread; installs source classloader as thread CCL during `sandboxManager.getAndroidSandbox`                 |
| `SandboxLifecycleManager.executeInSandbox`          | **must** be called from the sandbox main thread — callers wrap with `sandbox.runOnMainThread(Callable)`         |
| `AndroidSandbox.runOnMainThread`                    | single-threaded executor **per sandbox**; serializes all work on that sandbox                                   |
| `RobolectricEnvironment`                            | **not** thread-safe internally — relies on the sandbox main thread being single-threaded                        |
| `RobolectricEnvironment.access { }`                 | sets CCL to sandbox CL + pushes `FrameworkClassLoadingContext`; always balanced in `finally`                    |
| `ClassLifecycleManager` maps                        | `ConcurrentHashMap` — engines may discover/execute classes in parallel                                          |
| `SandboxSharingPolicy` (per-strategy caches)        | `ConcurrentHashMap` + one `@Volatile` — see §5.4. Each sealed type owns its own cache; no shared mutable state  |
| `ClassBytesSource`                                  | thread-local — safe **only** when every entry goes through `FrameworkClassLoadingContext.withContext` push/pop  |
| `RunnerLogger` / `RunnerMetrics`                    | internally synchronized; safe from any thread                                                                   |
| `RobolectricExtension` state                        | stored in `ExtensionContext.Store` with namespace `RobolectricExtension::class`; root-store caches `RobolectricDependencies` + `SandboxLifecycleManager` across the whole run |

Invariants worth preserving:

- A sandbox main thread is **pinned** — reflective invocation of the test body, lifecycle
  methods, and environment setup/teardown all run on it.
- The `ClassBytesSource` provider is thread-local and the *entering* frame pushes and the
  *same* frame pops in `finally`. A leaked provider would cause the next unrelated sandbox
  load on that thread to consume framework bytes.

### 6.2 Error handling

- **SDK resolution.** `createSandboxes` throws `IllegalStateException` when `SdkPicker`
  returns empty **and** the reflective fallback into `SdkCollection` also fails; the message
  carries the `fallbackReflectionFailure` cause. The previous "silently zero tests" failure
  mode is gone.
- **SDK fallback.** When `-Drobolectric.enabledSdks` filters out configured SDKs, the code
  falls back to the first `@Config(sdk)` (or latest known SDK) and both emits
  `WARNING:` to stderr and records `RunnerLogger.logSdkFallback`. The run doesn't silently
  succeed on the wrong SDK.
- **Test execution.** `RobolectricSandboxExecutor.executeSandboxed` throws;
  `executeSandboxedSafe` returns an `ExecutionResult` sealed class
  (`Success(duration)` / `Failure(error, duration)` / `Skipped(reason)`).
  `InvocationTargetException` is unwrapped so the framework sees the real test failure.

### 6.3 Reflection into `SdkCollection`

`:runner:common` depends on `:robolectric` with `implementation` rather than `api`, to keep
internal Robolectric classes out of the experimental public API. `SandboxLifecycleManager`
therefore reflects into `org.robolectric.plugins.SdkCollection` (`getSdk(int)` and
`getKnownSdks`) only for the fallback path described above. The comment in source flags
this as a future refactor target: if `SdkCollection` moves to `:pluginapi`, the reflection
is removed.

### 6.4 Engine coexistence

`MultiEngineCoexistenceTest` (in `:runner:junit-platform/src/test`) exercises the case
where both the Platform engine and the Jupiter engine are on the classpath — the common
IDE/Gradle setup. The invariants:

- A JUnit 4 `@org.junit.Test` without `@RunWith` is picked up by the Platform engine only.
- The Jupiter engine ignores non-Jupiter tests.
- `@RunWith(RobolectricTestRunner::class)` is handled by the JUnit 4 vintage engine, not by
  the new engines.
- There is no double-discovery: the engines are distinct `TestEngine` implementations with
  different `getId()` values and service declarations.

## 7. Sequence diagram — the hot path

Multi-SDK `PER_CLASS` under the Jupiter engine with method-level `@Config` override. This
is the most complex path; every other scenario is a degenerate version of this one.

```mermaid
sequenceDiagram
    autonumber
    participant JE as Jupiter engine
    participant DI as DefaultRoboIntegration
    participant CLM as ClassLifecycleManager
    participant SLM as SandboxLifecycleManager
    participant RE as RobolectricEnvironment
    participant SB as AndroidSandbox

    rect rgb(240,245,255)
    note over JE,SB: Discovery phase
    JE->>SLM: createSdkVariants(testClass, method, deps)
    SLM-->>JE: list of SdkTestVariant (one per SDK)
    end

    rect rgb(240,255,240)
    note over JE,SB: Class setup (per variant)
    JE->>DI: beforeClass(testClass)
    DI->>CLM: setupForClass(testClass)
    CLM->>SLM: createSandbox via withContextClassLoader
    SLM->>SB: sandboxManager.getAndroidSandbox
    SB-->>SLM: AndroidSandbox
    SLM-->>CLM: SandboxContext
    CLM->>SLM: getEnvironment(context)
    SLM-->>CLM: new RobolectricEnvironment
    CLM->>SB: runOnMainThread -- env.access -- setupApplicationState
    note right of RE: access swaps CCL and pushes ClassBytesSource provider
    SB-->>CLM: persistent class environment ready
    end

    rect rgb(255,250,235)
    note over JE,SB: Per-test execution
    JE->>DI: beforeEach (cache hit, no-op for PER_CLASS)
    JE->>DI: executeInSandbox(testClass, testMethod, block)
    DI->>SB: runOnMainThread
    alt methodCfg is not null -- method-level Config override
        DI->>CLM: executeInClassContext(methodCfg, block)
        CLM->>SLM: executeInSandbox(ctx, name, methodCfg)
        SLM->>RE: new isolated RobolectricEnvironment
        RE->>RE: access -- CCL swap + ClassBytesSource push
        RE->>RE: setupApplicationState(testName)
        RE->>SB: bootstrap and invoke test method
        RE->>RE: tearDownApplicationState
    else methodCfg is null
        DI->>RE: env.access reuses persistent env
    end
    JE->>DI: afterEach (record metrics)
    end

    rect rgb(255,240,245)
    note over JE,SB: Class teardown
    JE->>DI: afterClass(testClass)
    DI->>CLM: tearDownForClass(testClass)
    CLM->>SB: runOnMainThread -- tearDownApplication + resetState
    end
```

Two things to notice:

1. **Persistent vs method environment.** Step `beforeClass` opens the persistent class
   environment; the `executeInSandbox(..., configuration = methodCfg, …)` branch spins up a
   throwaway method environment that shares the sandbox and classloader. That is how
   method-level `@Config` is honored without destroying the class-level state.
2. **Two classloader mechanisms.** During sandbox construction (inside `createSandbox`) the
   thread CCL is temporarily the framework's source loader. During sandbox execution
   (inside `access`) the thread CCL is the sandbox loader **and** `ClassBytesSource` has a
   provider set. These do not overlap.

## 8. Reference integrations

### 8.1 `:runner:junit-platform` — `RobolectricTestEngine`

A native `org.junit.platform.engine.TestEngine` (service-loaded via
`META-INF/services/org.junit.platform.engine.TestEngine`). Accepts an optional
`parameterResolver` constructor argument (defaults to `DefaultRobolectricParameterResolver`).

1. **Discovery.** Accepts `ClassSelector`/`MethodSelector`. For each test method, asks
   `SandboxLifecycleManager.createSandboxes` for SDK contexts and emits one descriptor per
   SDK via `PlatformDescriptorBuilders`. Legacy non-parameterized descriptors flow through
   `SdkFallbackResolver` when `SdkPicker` returns empty.
2. **Execution.** Delegates to `TestMethodInvoker` (which handles bootstrap, `@Before`/
   `@BeforeEach`, parameter resolution via the injected `ParameterResolver`, invocation, and
   `@After`/`@AfterEach`). Class-level lifecycle is driven through `ClassLifecycleManager`,
   detected via `LifecycleHelper.hasLifecycleMethods(...)`. `ExecutionResult` carries
   pass/fail/skipped plus duration back to the platform listener.

Platform engine now honors both JUnit 4 `@Before`/`@After` and Jupiter
`@BeforeEach`/`@AfterEach` per-test lifecycle, keyed via `LifecycleAnnotations.JUNIT_COMBINED`
— an improvement over the pre-refactor behavior, which bootstrapped+invoked the test method
without per-test lifecycle (a latent bug documented by `BeforeAllAfterAllPlatformTest`).

### 8.2 `:runner:junit-jupiter` — two entry points

- **Extension path** (`@ExtendWith(RobolectricExtension::class)`). Implements
  `BeforeAllCallback`, `AfterAllCallback`, `BeforeEachCallback`, `AfterEachCallback`,
  `TestInstanceFactory`, `InvocationInterceptor`. State lives in `ExtensionContext.Store`
  (namespaced to `RobolectricExtension::class`), keyed `CLASS_LIFECYCLE_KEY`, `SANDBOX_KEY`,
  `DEPENDENCIES_KEY`, `SANDBOX_MANAGER_KEY`. `TestInstanceFactory` bootstraps the test class
  through `TestBootstrapper`; `InvocationInterceptor` reroutes test-method invocation onto
  the sandbox main thread. Lifecycle ordering (`@BeforeAll`/`@AfterAll`) is delegated to
  Jupiter's native callbacks — this path does **not** call `LifecycleHelper`.
- **Engine path** (`RobolectricJupiterEngine`). A full `TestEngine` with Jupiter-specific
  discovery (including `@Nested`) and per-method `@Config` merging via
  `ClassLifecycleManager.executeInClassContext(..., configuration = …)`. This path *does*
  use `LifecycleHelper` for `@BeforeAll`/`@AfterAll` because it is itself driving the
  lifecycle. Per-method test execution delegates to `TestMethodInvoker`. SDK selection goes
  through `SdkFallbackResolver` (shared with the common core).

The two paths are mutually exclusive and this is now documented on `RobolectricExtension`'s
KDoc — registering the extension on a test run that is also executed by
`RobolectricJupiterEngine` causes both to attempt class-level sandbox setup. Pick one per
run.

**Known gap: multi-SDK discovery.** The Platform engine emits one JUnit Platform descriptor
per `(method, sdk)` pair under `-Drobolectric.enabledSdks`. The Jupiter engine currently
emits one descriptor per method and selects a single SDK per invocation via
`SdkFallbackResolver`. Matching Platform's parameterization is possible but requires building
Jupiter-side SDK-variant descriptors in `JupiterDescriptorBuilders`; that work is deferred.

Both paths share `RobolectricDependencies`, `SandboxLifecycleManager`, `ClassLifecycleManager`,
`TestBootstrapper`, `TestMethodInvoker`, `SdkFallbackResolver`, and `ParameterResolver`.

## 9. Trade-offs and known limitations

- **API instability.** Every public symbol is `@ExperimentalRunnerApi`; the doc commits to
  that annotation staying in place until the API stabilizes.
- **`SdkCollection` reflection.** See §6.3. Acceptable short-term; replace when a typed API
  exists in `:pluginapi`.
- **Two Jupiter entry points.** Doubles the test surface. The integration matrix doc
  (`runner-integration-matrix.md`) states which components each path uses; the test tree
  exercises both.
- **`PER_SDK`/`GLOBAL` sharing.** Fast but fragile for tests that mutate static state. Both
  the README and the `SandboxSharingStrategy` KDoc call this out explicitly.
- **Thread-local `ClassBytesSource`.** Requires bridges to push/pop through
  `FrameworkClassLoadingContext`. Direct use is strongly discouraged.
- **`RunnerConfiguration` lacks a classloader bridge setter.** Mirror it or route
  custom-classloader integrations through `RobolectricIntegrationBuilder`.

## 10. Files to read first (before the rest)

If reviewing the runner modules, read these seven files in order — they encode the entire
design. Everything else is plumbing or tests.

1. `runner/common/.../RobolectricIntegration.kt` — the façade contract.
2. `runner/common/.../SandboxLifecycleManager.kt` + `SdkFallbackResolver.kt` — creation,
   SDK selection, shared fallback.
3. `runner/common/.../ClassLifecycleManager.kt` — the persistent-vs-per-method mode, now
   backed by a single `ClassState` record.
4. `runner/common/.../SandboxSharingPolicy.kt` — the sealed-interface split that replaced
   `DefaultRobolectricIntegration`'s four-way `when` branches.
5. `runner/common/.../TestMethodInvoker.kt` — the canonical bootstrap + lifecycle + invoke
   helper that both engines now call.
6. `runner/common/.../FrameworkClassLoadingContext.kt` + `sandbox/.../ClassBytesSource.java`
   — the bytecode bridge (two short files, read together).
7. Either `runner/junit-platform/.../RobolectricTestEngine.kt` **or**
   `runner/junit-jupiter/.../RobolectricJupiterEngine.kt` — see one end-to-end use.

Companion docs: `runner/common/README.md`,
`runner/common/docs/runner-api-inventory.md`,
`runner/common/docs/runner-integration-matrix.md`.
