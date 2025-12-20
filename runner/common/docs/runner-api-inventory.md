# Robolectric Runner API Inventory

This document provides a comprehensive inventory of all public APIs in the `:runner:common` module.

## Overview

The `runner:common` module provides shared components for building Robolectric test runners across different testing frameworks.

**API Stability**: All APIs are marked `@ExperimentalRunnerApi` and may change in future versions.

---

## Core Components

### RobolectricDependencies

**Purpose**: Dependency injection container for Robolectric core services.

```kotlin
@ExperimentalRunnerApi
data class RobolectricDependencies(
  val injector: Injector,
  val sandboxManager: SandboxManager,
  val sdkPicker: SdkPicker,
  val configurationStrategy: ConfigurationStrategy,
  val androidConfigurer: AndroidConfigurer,
  val shadowProviders: ShadowProviders,
  val classHandlerBuilder: ClassHandlerBuilder,
)
```

**Key Methods**:
| Method | Parameters | Returns | Thread Safety |
|--------|------------|---------|---------------|
| `create()` | `properties: Properties = System.getProperties()` | `RobolectricDependencies` | Thread-safe |

**Usage**:
```kotlin
val deps = RobolectricDependencies.create()
```

---

### SandboxLifecycleManager

**Purpose**: Manages the complete lifecycle of Robolectric sandboxes.

```kotlin
@ExperimentalRunnerApi
class SandboxLifecycleManager(dependencies: RobolectricDependencies)
```

**Key Methods**:
| Method | Parameters | Returns | Thread Safety |
|--------|------------|---------|---------------|
| `createSandbox()` | `testClass: Class<*>, testMethod: Method? = null` | `SandboxContext` | Not thread-safe |
| `createSandboxes()` | `testClass: Class<*>, testMethod: Method? = null` | `List<SandboxContext>` | Not thread-safe |
| `executeInSandbox()` | `context: SandboxContext, testName: String, block: () -> Unit` | `Unit` | Must run on sandbox main thread |
| `getEnvironment()` | `context: SandboxContext` | `RobolectricEnvironment` | Thread-safe |

**SandboxContext Data Class**:
```kotlin
data class SandboxContext(
  val sandbox: AndroidSandbox,
  val configuration: Configuration,
  val sdk: Sdk,
  val appManifest: AndroidManifest,
)
```

---

### ClassLifecycleManager

**Purpose**: Manages class-level sandbox sharing for @BeforeAll/@AfterAll support.

```kotlin
@ExperimentalRunnerApi
class ClassLifecycleManager(sandboxLifecycleManager: SandboxLifecycleManager)
```

**Key Methods**:
| Method | Parameters | Returns | Thread Safety |
|--------|------------|---------|---------------|
| `setupForClass()` | `testClass: Class<*>` | `SandboxContext` | Thread-safe (uses ConcurrentHashMap) |
| `tearDownForClass()` | `testClass: Class<*>` | `Unit` | Thread-safe |
| `getClassContext()` | `testClass: Class<*>` | `SandboxContext?` | Thread-safe |
| `executeInClassContext()` | `testClass: Class<*>, testName: String, configuration: Configuration? = null, block: () -> Unit` | `Unit` | Must call from proper thread |

---

### RobolectricSandboxExecutor

**Purpose**: High-level executor for running tests within sandboxes.

```kotlin
@ExperimentalRunnerApi
class RobolectricSandboxExecutor(lifecycleManager: SandboxLifecycleManager)
```

**Key Methods**:
| Method | Parameters | Returns | Thread Safety |
|--------|------------|---------|---------------|
| `executeSandboxed()` | `testClass: Class<*>, testMethod: Method? = null, testName: String, block: (AndroidSandbox) -> Unit` | `Unit` (throws on failure) | Not thread-safe |
| `executeSandboxedSafe()` | `testClass: Class<*>, testMethod: Method?, testName: String, block: (AndroidSandbox) -> Unit` | `ExecutionResult` | Not thread-safe |
| `executeSandboxedSafe()` (SDK variant) | `testClass: Class<*>, testMethod: Method?, sdk: Sdk, testName: String, block: (AndroidSandbox) -> Unit` | `ExecutionResult` | Not thread-safe |

**ExecutionResult**:
```kotlin
data class ExecutionResult(val isSuccess: Boolean, val error: Throwable? = null) {
  companion object {
    fun success(): ExecutionResult
    fun failure(error: Throwable): ExecutionResult
  }
}
```

---

### TestBootstrapper

**Purpose**: Utilities for bootstrapping test classes through the sandbox classloader.

```kotlin
@ExperimentalRunnerApi
object TestBootstrapper
```

**Key Methods**:
| Method | Parameters | Returns | Thread Safety |
|--------|------------|---------|---------------|
| `bootstrapClass()` | `sandbox: AndroidSandbox, testClass: Class<*>` | `Class<T>` | Thread-safe |
| `createTestInstance()` | `bootstrappedClass: Class<*>` | `Any` | Not thread-safe |
| `invokeTestMethod()` | `testInstance: Any, method: Method, args: Array<Any?>` | `Any?` | Not thread-safe |
| `bootstrapParameterTypes()` | `sandbox: AndroidSandbox, parameterTypes: Array<Class<*>>` | `Array<Class<*>>` | Thread-safe |

---

### LifecycleHelper

**Purpose**: Utilities for invoking lifecycle methods (@Before, @After, etc.).

```kotlin
@ExperimentalRunnerApi
object LifecycleHelper
```

**Key Methods**:
| Method | Parameters | Returns | Thread Safety |
|--------|------------|---------|---------------|
| `invokeLifecycleMethods()` | `testClass: Class<*>, testInstance: Any, annotationClass: Class<out Annotation>` | `Unit` | Not thread-safe |
| `invokeStaticLifecycleMethods()` | `testClass: Class<*>, annotationClass: Class<out Annotation>` | `Unit` | Not thread-safe |

**Ordering Guarantees**:
- `@Before*` methods: superclass first, then subclass
- `@After*` methods: subclass first, then superclass
- Overridden methods are invoked only once

---

### ParameterResolver

**Purpose**: Interface and default implementation for test parameter injection.

```kotlin
@ExperimentalRunnerApi
interface ParameterResolver {
  fun resolveParameter(parameter: Parameter, sandbox: AndroidSandbox): Any?
}

@ExperimentalRunnerApi
object DefaultRobolectricParameterResolver : ParameterResolver

@ExperimentalRunnerApi
object ParameterResolutionHelper {
  fun resolveParameters(parameters: Array<Parameter>, sandbox: AndroidSandbox): Array<Any?>
}
```

**Supported Parameter Types**:
- `android.content.Context`
- `android.app.Application`
- `ActivityController<T>`
- `ServiceController<T>`

---

#### DiscoveryHelpers

**Purpose**: Utilities for framework-agnostic test discovery.

```kotlin
@ExperimentalRunnerApi
object DiscoveryHelpers
```

**Key Methods**:
| Method | Parameters | Returns | Thread Safety |
|--------|------------|---------|---------------|
| `isTestMethod()` | `method: Method` | `Boolean` | Thread-safe |
| `discoverTestMethods()` | `testClass: Class<*>, testAnnotations: List<Class<out Annotation>>, includeInherited: Boolean = true, filter: TestFilter = TestFilter.ACCEPT_ALL` | `List<Method>` | Thread-safe |
| `discoverTestMethodsByName()` | `testClass: Class<*>, annotationClassNames: List<String>, includeInherited: Boolean = true, filter: TestFilter = TestFilter.ACCEPT_ALL` | `List<Method>` | Thread-safe |
| `discoverNestedClasses()` | `testClass: Class<*>, nestedAnnotation: Class<out Annotation>? = null` | `List<Class<*>>` | Thread-safe |
| `createSdkVariants()` | `testClass: Class<*>, testMethod: Method, deps: RobolectricDependencies, baseUniqueId: String, alwaysIncludeSdkInName: Boolean` | `List<SdkTestVariant>` | Thread-safe |
| `discoverAllVariants()` | `testClass: Class<*>, testAnnotations: List<Class<out Annotation>>, deps: RobolectricDependencies, filter: TestFilter = TestFilter.ACCEPT_ALL` | `List<SdkTestVariant>` | Thread-safe |

**Supported Test Annotations (String-Based)**:
- `org.junit.jupiter.api.Test`
- `org.junit.Test`

**Note**: This module is now fully framework-agnostic and has no compile-time dependencies on JUnit Platform or Jupiter. Test framework-specific descriptor building has been moved to the respective engine modules (`runner:junit-jupiter` and `runner:junit-platform`).

---

### ManifestResolver

**Purpose**: Resolves Android manifest for tests.

```kotlin
@ExperimentalRunnerApi
object ManifestResolver {
  fun resolveManifest(config: Config?): AndroidManifest
}
```

---

### RobolectricEnvironment

**Purpose**: Manages the Android test environment state.

```kotlin
@ExperimentalRunnerApi
class RobolectricEnvironment(sandbox: AndroidSandbox, context: SandboxContext)
```

**Key Methods**:
| Method | Parameters | Returns | Thread Safety |
|--------|------------|---------|---------------|
| `setupApplicationState()` | `testName: String` | `Unit` | Must run on sandbox main thread |
| `tearDownApplication()` | - | `Unit` | Must run on sandbox main thread |
| `resetState()` | - | `Unit` | Must run on sandbox main thread |
| `access()` | `block: () -> T` | `T` | Re-entrant lock protected |

---

## Observability Components

### RunnerLogger

**Purpose**: Debug logging for runner operations.

```kotlin
@ExperimentalRunnerApi
object RunnerLogger
```

**Configuration**: `-Drobolectric.runner.debug=true`

**Key Methods**:
| Method | Parameters | Returns |
|--------|------------|---------|
| `logTestStart()` | `className: String, methodName: String, sdkLevel: Int` | `Unit` |
| `logTestEnd()` | `className: String, methodName: String, durationMs: Long, success: Boolean` | `Unit` |
| `logSdkSelection()` | `testClass: String, selectedSdks: List<Int>, configuredSdks: List<Int>` | `Unit` |
| `logSandboxCreation()` | `testClass: String, method: String?, sdk: Int, looperMode: String, sqliteMode: String, graphicsMode: String` | `Unit` |
| `logClassContextCreated()` | `className: String, sdkLevel: Int` | `Unit` |
| `logClassContextReused()` | `className: String` | `Unit` |
| `logClassContextTeardown()` | `className: String` | `Unit` |

---

### RunnerMetrics

**Purpose**: Metrics collection for performance analysis.

```kotlin
@ExperimentalRunnerApi
object RunnerMetrics
```

**Configuration**:
- `-Drobolectric.runner.metrics=true`
- `-Drobolectric.runner.metrics.timing=true`

**Phase Constants**:
- `PHASE_SANDBOX_CREATION`
- `PHASE_ENVIRONMENT_SETUP`
- `PHASE_ENVIRONMENT_TEARDOWN`
- `PHASE_CLASS_SETUP`
- `PHASE_CLASS_TEARDOWN`
- `PHASE_TEST_EXECUTION`

**Key Methods**:
| Method | Parameters | Returns |
|--------|------------|---------|
| `enable()` | - | `Unit` |
| `enableTiming()` | - | `Unit` |
| `reset()` | - | `Unit` |
| `recordSandboxCreation()` | - | `Unit` |
| `recordSandboxTeardown()` | - | `Unit` |
| `recordSandboxCacheHit()` | - | `Unit` |
| `recordSandboxCacheMiss()` | - | `Unit` |
| `recordTestExecution()` | `success: Boolean` | `Unit` |
| `recordTiming()` | `phase: String, durationMs: Long` | `Unit` |
| `timed()` | `phase: String, block: () -> T` | `T` |
| `getSummary()` | - | `String` |

---

## Configuration Components

### SystemPropertiesSupport

**Purpose**: System property parsing for runner configuration.

```kotlin
@ExperimentalRunnerApi
object SystemPropertiesSupport
```

**Key Methods**:
| Method | Parameters | Returns |
|--------|------------|---------|
| `getEnabledSdks()` | - | `List<Int>?` |
| `alwaysIncludeVariantMarkersInTestName()` | - | `Boolean` |
| `formatTestName()` | `methodName: String, sdkLevel: Int, alwaysInclude: Boolean, isLastSdk: Boolean` | `String` |
| `createSdkSegment()` | `sdkLevel: Int` | `String` |

**Supported Properties**:
- `robolectric.enabledSdks` - Comma-separated SDK API levels
- `robolectric.alwaysIncludeVariantMarkersInTestName` - Boolean

---

## Internal Components (Not for External Use)

### SandboxConfigurator

**Purpose**: Internal sandbox configuration logic.

```kotlin
@ExperimentalRunnerApi
class SandboxConfigurator(
  androidConfigurer: AndroidConfigurer,
  shadowProviders: ShadowProviders,
  classHandlerBuilder: ClassHandlerBuilder,
)
```

---

## Integration Patterns

### Minimal Integration

```kotlin
// 1. Initialize dependencies
val deps = RobolectricDependencies.create()
val lifecycleManager = SandboxLifecycleManager(deps)
val executor = RobolectricSandboxExecutor(lifecycleManager)

// 2. Execute test
executor.executeSandboxed(testClass, testMethod) { sandbox ->
  val bootstrappedClass = TestBootstrapper.bootstrapClass<Any>(sandbox, testClass)
  val instance = TestBootstrapper.createTestInstance(bootstrappedClass)
  TestBootstrapper.invokeTestMethod(instance, testMethod, emptyArray())
}
```

### With Class-Level Sharing

```kotlin
val classLifecycleManager = ClassLifecycleManager(lifecycleManager)

// Before class
classLifecycleManager.setupForClass(testClass)

// Each test
classLifecycleManager.executeInClassContext(testClass, testName) {
  // Test code
}

// After class
classLifecycleManager.tearDownForClass(testClass)
```

### With Parameter Injection

```kotlin
val args = ParameterResolutionHelper.resolveParameters(method.parameters, sandbox)
bootstrappedMethod.invoke(testInstance, *args)
```

---

## Threading Model

| Component | Thread Requirement |
|-----------|-------------------|
| `RobolectricDependencies.create()` | Any thread |
| `SandboxLifecycleManager.createSandbox()` | Any thread |
| `SandboxLifecycleManager.executeInSandbox()` | Sandbox main thread |
| `ClassLifecycleManager` methods | Any thread (internally synchronized) |
| `TestBootstrapper.createTestInstance()` | Sandbox main thread |
| `LifecycleHelper` methods | Sandbox main thread |
| `RunnerLogger/RunnerMetrics` | Any thread (internally synchronized) |

---

## Error Handling

- `executeSandboxed()` throws exceptions directly
- `executeSandboxedSafe()` wraps exceptions in `ExecutionResult`
- `InvocationTargetException` is unwrapped to expose actual test failure
- Lifecycle methods handle cleanup even on test failure

---

## Version History

- **4.x**: Initial experimental API release

