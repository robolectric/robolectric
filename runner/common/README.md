# Robolectric Runner Integration Guide

This guide explains how to integrate Robolectric with custom test frameworks using the `runner:common` module.

## Overview

The `runner:common` module provides shared components for building Robolectric test runners. It handles:
- Dependency injection (`RobolectricDependencies`)
- Sandbox configuration (`SandboxConfigurator`)
- Environment lifecycle (`RobolectricEnvironment`)
- Test bootstrapping (`TestBootstrapper`)
- Test discovery (`DiscoveryHelpers`)
- Parameter resolution (`ParameterResolver`)
- Class-level sandbox sharing (`ClassLifecycleManager`)
- Centralized test execution (`RobolectricSandboxExecutor`)
- Manifest resolution (`ManifestResolver`)
- Lifecycle method handling (`LifecycleHelper`)

## Architecture

```
┌─────────────────────────────────────────────────────────────────────┐
│                         Test Framework                               │
│  (JUnit Platform, JUnit Jupiter, Kotest, Custom)                    │
└─────────────────────────────────────────────────────────────────────┘
                                    │
                                    ▼
┌─────────────────────────────────────────────────────────────────────┐
│                        runner:common                                 │
│  ┌──────────────────────┐  ┌──────────────────────┐                 │
│  │ RobolectricDependencies│  │ SandboxLifecycleManager│              │
│  │ (Dependency Injection) │  │ (Sandbox Creation)    │              │
│  └──────────────────────┘  └──────────────────────┘                 │
│  ┌──────────────────────┐  ┌──────────────────────┐                 │
│  │ TestBootstrapper     │  │ LifecycleHelper      │                 │
│  │ (Class/Method Load)  │  │ (@Before/@After)     │                 │
│  └──────────────────────┘  └──────────────────────┘                 │
│  ┌──────────────────────┐  ┌──────────────────────┐                 │
│  │ ParameterResolver    │  │ DiscoveryHelpers     │                 │
│  │ (Injection Support)  │  │ (Test Discovery)     │                 │
│  └──────────────────────┘  └──────────────────────┘                 │
└─────────────────────────────────────────────────────────────────────┘
                                    │
                                    ▼
┌─────────────────────────────────────────────────────────────────────┐
│                     Robolectric Core                                 │
│  (AndroidSandbox, TestEnvironment, ShadowProviders)                 │
└─────────────────────────────────────────────────────────────────────┘
```

## Basic Integration Steps

### 1. Add Dependency

Add the `runner:common` dependency to your module's `build.gradle.kts`:

```kotlin
dependencies {
  implementation(project(":runner:common"))
}
```

### 2. Initialize Dependencies

In your test runner or engine, initialize Robolectric dependencies and the lifecycle manager:

```kotlin
val deps = RobolectricDependencies.create()
val lifecycleManager = SandboxLifecycleManager(deps)
```

### 3. Discover Tests

Use `DiscoveryHelpers` to build a test descriptor tree (for JUnit Platform engines):

```kotlin
override fun discover(request: EngineDiscoveryRequest, uniqueId: UniqueId): TestDescriptor {
  val engineDescriptor = EngineDescriptor(uniqueId, "My Engine")

  request.getSelectorsByType(ClassSelector::class.java).forEach { selector ->
    DiscoveryHelpers.appendTestClass(
      engineDescriptor,
      selector.javaClass,
      DiscoveryHelpers::isTestMethod  // Use shared helper
    )
  }
  return engineDescriptor
}
```

### 4. Execute Tests

Execute tests within the Robolectric sandbox using `SandboxLifecycleManager`:

```kotlin
fun executeTest(descriptor: CommonMethodDescriptor) {
  val testClass = descriptor.method.declaringClass
  
  // Create sandbox
  val sandboxContext = lifecycleManager.createSandbox(testClass, descriptor.method)
  val sandbox = sandboxContext.sandbox

  // Run in sandbox
  sandbox.runOnMainThread {
    lifecycleManager.executeInSandbox(sandboxContext, descriptor.method.name) {
      // Bootstrap test class
      val bootstrappedClass = TestBootstrapper.bootstrapClass<Any>(sandbox, testClass)
      val testInstance = TestBootstrapper.createTestInstance(bootstrappedClass)

      // Invoke lifecycle methods
      LifecycleHelper.invokeLifecycleMethods(bootstrappedClass, testInstance, BeforeEach::class.java)

      // Invoke test method with parameter resolution
      val method = descriptor.method
      if (method.parameterCount > 0) {
        val bootstrappedParams = TestBootstrapper.bootstrapParameterTypes(sandbox, method.parameterTypes)
        val bootstrappedMethod = bootstrappedClass.getMethod(method.name, *bootstrappedParams)
        
        val args = method.parameters.map { param ->
          DefaultRobolectricParameterResolver.resolveParameter(param, sandbox)
        }.toTypedArray()
        
        bootstrappedMethod.invoke(testInstance, *args)
      } else {
        TestBootstrapper.invokeTestMethod(testInstance, method, emptyArray())
      }
      
      // Invoke teardown
      LifecycleHelper.invokeLifecycleMethods(bootstrappedClass, testInstance, AfterEach::class.java)
    }
  }
}
```

## Advanced Features

### Using RobolectricSandboxExecutor

For simpler integration, use `RobolectricSandboxExecutor` which handles sandbox setup/teardown:

```kotlin
val executor = RobolectricSandboxExecutor(lifecycleManager)

executor.executeSandboxed(testClass, testMethod, "testName") { sandbox ->
  // Test code runs here with sandbox already set up
  val bootstrappedClass = TestBootstrapper.bootstrapClass<Any>(sandbox, testClass)
  // ... execute test
}
```

### Class-Level Sandbox Sharing

Use `ClassLifecycleManager` for efficient sharing of sandboxes across tests in the same class:

```kotlin
val classLifecycleManager = ClassLifecycleManager(lifecycleManager)

// Setup once per class
classLifecycleManager.setupForClass(testClass)

// Execute tests using shared sandbox
classLifecycleManager.executeInClassContext(testClass, "testName") {
  // Test code runs here
}

// Cleanup after all tests
classLifecycleManager.tearDownForClass(testClass)
```

### Nested Classes

`DiscoveryHelpers` automatically handles `@Nested` classes if you provide the annotation class:

```kotlin
DiscoveryHelpers.appendTestClass(
  engineDescriptor,
  testClass,
  DiscoveryHelpers::isTestMethod,
  Nested::class.java
)
```

`TestBootstrapper.createTestInstance` automatically handles instantiation of non-static inner classes by creating the enclosing instance first.

### Parameter Injection

`DefaultRobolectricParameterResolver` supports injection of:
- `android.content.Context`
- `android.app.Application`
- `ActivityController<T>`
- `ServiceController<T>`

You can implement `ParameterResolver` to support custom types:

```kotlin
class MyParameterResolver : ParameterResolver {
  override fun resolveParameter(parameter: Parameter, sandbox: AndroidSandbox): Any? {
    return when (parameter.type.name) {
      "com.example.MyService" -> createMyService(sandbox)
      else -> null
    }
  }
}
```

### Lifecycle Method Handling

`LifecycleHelper` provides utilities to invoke methods annotated with lifecycle annotations across the class hierarchy:

```kotlin
// Static lifecycle methods (@BeforeAll, @AfterAll)
LifecycleHelper.invokeStaticLifecycleMethods(bootstrappedClass, BeforeAll::class.java)

// Instance lifecycle methods (@BeforeEach, @AfterEach)
LifecycleHelper.invokeLifecycleMethods(bootstrappedClass, testInstance, BeforeEach::class.java)
```

**Ordering guarantees (per JUnit 5 spec):**
- `@Before*` methods: superclass methods run before subclass methods
- `@After*` methods: subclass methods run before superclass methods
- Overridden methods are only executed once (in the subclass)

### Multi-SDK Test Execution

Use `createSandboxes()` to get contexts for all selected SDKs when `-Drobolectric.enabledSdks` is set:

```kotlin
val contexts = lifecycleManager.createSandboxes(testClass, testMethod)
contexts.forEach { context ->
  // Execute test with this SDK
  lifecycleManager.executeInSandbox(context, "${testMethod.name}[sdk=${context.sdk.apiLevel}]") {
    // Test code
  }
}
```

## System Properties

The runner supports these system properties:

| Property | Description |
|----------|-------------|
| `robolectric.enabledSdks` | Comma-separated list of SDK API levels to test (e.g., `23,29,34`) |
| `robolectric.alwaysIncludeVariantMarkersInTestName` | If `true`, always append SDK info to test names |
| `robolectric.runner.debug` | If `true`, enable verbose debug logging |
| `robolectric.runner.metrics` | If `true`, enable metrics collection |
| `robolectric.runner.metrics.timing` | If `true`, enable timing metrics (requires `metrics=true`) |
| `robolectric.runner.logLevel` | Set log level: `DEBUG`, `INFO`, `WARN`, `ERROR` |

## Monitoring and Logging

The runner includes built-in observability features to help diagnose issues and measure performance.

### Debug Logging

Enable debug logging to see detailed information about sandbox lifecycle:

```bash
./gradlew test -Drobolectric.runner.debug=true
```

Example output:
```
2026-01-02T10:30:00Z [Robolectric] [DEBUG] [main] SDK selection for MyTest: selected=[29], configured=[28, 29, 30]
2026-01-02T10:30:00Z [Robolectric] [DEBUG] [main] Creating sandbox for MyTest.testMethod [SDK=29, Looper=PAUSED, SQLite=NATIVE, Graphics=NATIVE]
2026-01-02T10:30:01Z [Robolectric] [DEBUG] [main] Class context created for MyTest [SDK=29]
2026-01-02T10:30:01Z [Robolectric] [DEBUG] [main] Test started: MyTest.testMethod [SDK=29]
2026-01-02T10:30:01Z [Robolectric] [DEBUG] [main] Test ended: MyTest.testMethod [PASSED, 150ms]
```

### Metrics Collection

Enable metrics to track sandbox cache efficiency and test execution statistics:

```bash
./gradlew test -Drobolectric.runner.metrics=true
```

At the end of the test run, a summary is printed:
```
============================================================
Robolectric Runner Metrics Summary
============================================================

--- Sandbox Metrics ---
  Sandbox creations:  5
  Sandbox teardowns:  5
  Cache hits:         45
  Cache misses:       5
  Cache hit rate:     90.0%

--- Test Metrics ---
  Test executions:    50
  Test failures:      2
  Success rate:       96.0%
============================================================
```

### Timing Metrics

Enable timing metrics for performance analysis:

```bash
./gradlew test -Drobolectric.runner.metrics=true -Drobolectric.runner.metrics.timing=true
```

This adds timing statistics for each execution phase:
```
--- Timing Metrics ---
  sandbox_creation: count=5, total=2500ms, avg=500.00ms, min=400ms, max=600ms, p50=500ms, p90=580ms, p99=600ms
  environment_setup: count=5, total=1000ms, avg=200.00ms, min=150ms, max=250ms, p50=200ms, p90=240ms, p99=250ms
  test_execution: count=50, total=5000ms, avg=100.00ms, min=20ms, max=500ms, p50=80ms, p90=200ms, p99=450ms
```

### Programmatic Access

You can also enable and access metrics programmatically:

```kotlin
// Enable metrics
RunnerMetrics.enable()
RunnerMetrics.enableTiming()

// Get current stats
val cacheHitRate = RunnerMetrics.getSummary()
val timingStats = RunnerMetrics.getTimingStats(RunnerMetrics.PHASE_TEST_EXECUTION)

// Reset for a new test run
RunnerMetrics.reset()
```

### CI Integration Tips

1. **Enable metrics in CI** to track performance regressions over time
2. **Use debug logging** when investigating test failures
3. **Monitor cache hit rate** - low rates indicate sandbox sharing isn't working effectively
4. **Track p99 timing** to catch outlier tests that slow down the suite

## Experimental API

All APIs in `runner:common` are marked with `@ExperimentalRunnerApi` to indicate they may change in future versions. To use them:

```kotlin
@OptIn(ExperimentalRunnerApi::class)
class MyTestEngine : TestEngine {
  // ...
}
```

## Example Implementations

See the following modules for complete integration examples:
- `runner:junit-platform` - JUnit Platform TestEngine implementation
- `runner:junit-jupiter` - JUnit Jupiter Extension implementation
- `runner:kotest` - Kotest Extension implementation
