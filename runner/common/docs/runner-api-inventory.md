# Robolectric Runner API Inventory

An index of the public API in `:runner:common`: what each type is for and where it sits.
Signatures live in the KDoc of each file and are not repeated here, because a copy of every
signature is exactly the kind of documentation that silently rots. When this file and the
code disagree, the code wins; fix the file.

Everything listed is `@ExperimentalRunnerApi` and may change without notice.

Paths are relative to `runner/common/src/main/kotlin/org/robolectric/runner/common/`.

## Core lifecycle

| Type | File | What it is |
| ---- | ---- | ---------- |
| `RobolectricDependencies` | `RobolectricDependencies.kt` | Container for the Robolectric core services (injector, sandbox manager, SDK picker, configuration strategy, and friends). Create once per run via `create()`; it is expensive. |
| `SandboxLifecycleManager` | `SandboxLifecycleManager.kt` | Creates sandboxes (`createSandbox`, `createSandboxes` for multi-SDK, `createSandboxForConfig` for the framework-free path) and executes blocks inside them. Fails fast with diagnostic detail when no SDK can be resolved. |
| `ClassLifecycleManager` | `ClassLifecycleManager.kt` | Class-scoped persistent environments for `@BeforeAll`/`@AfterAll`-style lifecycle: `setupForClass`, `executeInClassContext`, `tearDownForClass`. |
| `RobolectricSandboxExecutor` | `RobolectricSandboxExecutor.kt` | One-test-one-sandbox execution, plus class-lifecycle orchestration for engines that want it. `executeSandboxed` throws; `executeSandboxedSafe` returns an `ExecutionResult`. |
| `RobolectricEnvironment` | `RobolectricEnvironment.kt` | The per-sandbox Android environment: application state setup/teardown and the `access { }` block that swaps the context classloader and arms the classloader bridge. |
| `TestBootstrapper` | `TestBootstrapper.kt` | Loads test classes through the sandbox classloader ("twin" semantics) and instantiates them, including inner classes with their outer instance. |
| `TestMethodInvoker` | `TestMethodInvoker.kt` | The canonical bootstrap + `@Before*` + invoke + `@After*` sequence used by every execution path. Stateless; callers arrange sandbox-main-thread scheduling. |
| `LifecycleHelper` | `LifecycleHelper.kt` | Finds and invokes lifecycle methods in the right order, including inherited ones. |
| `LifecycleAnnotations` | `LifecycleAnnotations.kt` | Presets (`JUNIT4`, `JUNIT5`, `JUNIT_COMBINED`, `NONE`) that resolve annotation class names at runtime, keeping this module free of JUnit compile dependencies. |
| `TestExecutionContext` | `TestExecutionContext.kt` | The sandbox/environment handle an integration receives for a running test. |

## Execution policy and SDK selection

| Type | File | What it is |
| ---- | ---- | ---------- |
| `ExecutionPolicyResolver` | `ExecutionPolicyResolver.kt` | The canonical decision for where a test method runs: shared class environment, isolated per-method environment, or fail-fast conflict. Both Jupiter entry points consult it. |
| `MethodSdkResolver` | `MethodSdkResolver.kt` | Per-method SDK selection: `SdkPicker` first, `SdkFallbackResolver` when the picker comes back empty. |
| `SdkFallbackResolver` | `SdkFallbackResolver.kt` | The `-Drobolectric.enabledSdks` fallback: first `@Config(sdk)` entry, else the latest known SDK, with a warning either way. Reflects into `SdkCollection` until that class has a typed home in `:pluginapi`. |
| `SdkTestVariant` | `SdkTestVariant.kt` | One (test method, SDK) pair produced by discovery-time SDK expansion. |
| `SystemPropertiesSupport` | `SystemPropertiesSupport.kt` | Parses the `robolectric.*` system properties and formats per-SDK test names (`testFoo[33]`). |

## Discovery

| Type | File | What it is |
| ---- | ---- | ---------- |
| `DiscoveryHelpers` | `DiscoveryHelpers.kt` | Framework-agnostic discovery: `isTestMethod` (by annotation FQCN, so it works across classloaders), `discoverTestMethods`, `createSdkVariants`. Descriptor building for JUnit Platform lives in the engine modules, not here. |
| `TestFilter` | `TestFilter.kt` | Include/exclude filtering applied during discovery. |

## Parameters

| Type | File | What it is |
| ---- | ---- | ---------- |
| `ParameterResolver` / `DefaultRobolectricParameterResolver` / `ParameterResolutionHelper` | `ParameterResolver.kt` | The parameter-injection SPI, the built-in resolver (`Context`, `Application`, `ActivityController`, `ServiceController`), and the helper that applies a resolver to a method's parameter list. Unknown types fail with the offending type named. |

## Façade and configuration

| Type | File | What it is |
| ---- | ---- | ---------- |
| `RobolectricIntegration` | `RobolectricIntegration.kt` | The intended adapter surface: four lifecycle callbacks, a context accessor, `executeInSandbox`. Note: no shipped integration uses it yet (design doc §5.2). |
| `DefaultRobolectricIntegration` | `DefaultRobolectricIntegration.kt` | The standard implementation, delegating sharing decisions to a `SandboxSharingPolicy`. |
| `RobolectricIntegrationBuilder` | `RobolectricIntegrationBuilder.kt` | Fluent construction with presets (`forJUnitPlatform()`, `forJUnitJupiter()`, `fromSystemProperties()`). |
| `RunnerConfiguration` | `RunnerConfiguration.kt` | The transferable configuration DTO; `createIntegration()` routes through the builder. `validate()` checks the `timingEnabled ⇒ metricsEnabled` invariant. |
| `SandboxSharingStrategy` | `SandboxSharingStrategy.kt` | `PER_TEST`, `PER_CLASS`, `PER_SDK`, `GLOBAL`. Applies to façade users only; the shipped engines hard-wire their own sharing. |

## Classloader bridge

| Type | File | What it is |
| ---- | ---- | ---------- |
| `FrameworkClassLoadingBridge` | `FrameworkClassLoadingBridge.kt` | Public SPI for frameworks that load transformed test bytecode through their own classloaders. |
| `FrameworkClassLoadingContext` | `FrameworkClassLoadingContext.kt` | Thread-local plumbing between the bridge and the sandbox's `ClassBytesSource` hook. The only code allowed to write that thread-local; integrators never touch `ClassBytesSource` directly. |

## Framework-free entry point

| Type | File | What it is |
| ---- | ---- | ---------- |
| `RobolectricRuntime` | `RobolectricRuntime.kt` | Boots an Android environment with no test framework (`launch { sdk = 34 }`), for REPLs and preview renderers. `execute` runs a block on the sandbox main thread; `executeLoaded` invokes a sandbox-loaded twin; `close()` tears down. Design doc §8.3. |

## Results and observability

| Type | File | What it is |
| ---- | ---- | ---------- |
| `ExecutionResult` | `ExecutionResult.kt` | Sealed result of a safe execution: `Success(duration)`, `Failure(error, duration)`, `Skipped(reason)`. |
| `RunnerLogger` | `RunnerLogger.kt` | Structured debug logging, off unless `-Drobolectric.runner.debug=true`. |
| `RunnerMetrics` | `RunnerMetrics.kt` | Counters and per-phase timings, off unless `-Drobolectric.runner.metrics=true` (timings additionally need `robolectric.runner.metrics.timing`). |

## Support types

| Type | File | What it is |
| ---- | ---- | ---------- |
| `ManifestResolver` | `ManifestResolver.kt` | Resolves the Android manifest for a configuration via the build-system properties and manifest factory. |
| `ExperimentalRunnerApi` | `ExperimentalRunnerApi.kt` | The opt-in marker every public type here carries. |

## Internal (on the classpath, not for external use)

| Type | File | Why it is internal |
| ---- | ---- | ------------------ |
| `SandboxConfigurator` | `SandboxConfigurator.kt` | How a sandbox is configured (looper/SQLite/graphics modes, shadows, interceptors); collaborator of `SandboxLifecycleManager`. |
| `SandboxSharingPolicy` | `SandboxSharingPolicy.kt` | The sealed per-strategy cache/lifecycle implementations behind `SandboxSharingStrategy`. Go through the façade instead. |

## Threading and error handling

Not duplicated here. The thread/concurrency table is design doc §6.1; the error-handling
contract is §6.2. Both were written against the code and are kept current there.
