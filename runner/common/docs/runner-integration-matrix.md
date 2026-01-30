# Robolectric Runner Integration Matrix

This document shows how different testing frameworks map to Robolectric runner components.

## Framework Comparison

| Feature | JUnit Platform | JUnit Jupiter | Kotest |
|---------|---------------|---------------|--------|
| **Entry Point** | `TestEngine` | `Extension` | `Extension` |
| **Discovery** | `DiscoveryHelpers` | Jupiter handles | Kotest handles |
| **Class Lifecycle** | `ClassLifecycleManager` | `ClassLifecycleManager` | Per-spec sandbox |
| **Test Execution** | `RobolectricSandboxExecutor` | `InvocationInterceptor` | Listener callbacks |
| **Parameter Injection** | `ParameterResolver` | `ParameterResolver` | Manual via RuntimeEnvironment |
| **Lifecycle Methods** | `LifecycleHelper` | Jupiter handles | Kotest handles |

---

## Integration Points by Framework Type

### JUnit Platform TestEngine

Implement `org.junit.platform.engine.TestEngine`:

```kotlin
class MyRobolectricEngine : TestEngine {
  // Use these components:
  // - RobolectricDependencies.create()
  // - SandboxLifecycleManager
  // - ClassLifecycleManager
  // - RobolectricSandboxExecutor
  // - DiscoveryHelpers (for framework-agnostic discovery)
  // - TestBootstrapper
  // - LifecycleHelper
  // - ParameterResolutionHelper
  
  // Note: JUnit Platform-specific descriptor building (TestDescriptor,
  // UniqueId, etc.) is handled in the engine module itself, not in
  // runner:common. See JupiterDescriptorBuilders or PlatformDescriptorBuilders
  // for examples.
}
```

**Required Components**:
| Component | Purpose |
|-----------|---------|
| `RobolectricDependencies` | Bootstrap Robolectric services |
| `SandboxLifecycleManager` | Create/manage sandboxes |
| `ClassLifecycleManager` | @BeforeClass/@AfterClass support |
| `DiscoveryHelpers` | Framework-agnostic test discovery (method/class discovery, SDK variants) |
| `RobolectricSandboxExecutor` | Execute tests in sandbox |
| `TestBootstrapper` | Load test classes through sandbox |
| `LifecycleHelper` | Invoke @Before/@After methods |

**Note**: JUnit Platform-specific components like `TestDescriptor` builders are now implemented in the engine modules (`runner:junit-jupiter` or `runner:junit-platform`), not in `runner:common`.

---

### JUnit Jupiter Extension

Implement Jupiter extension interfaces:

```kotlin
class MyRobolectricExtension : 
  BeforeAllCallback,
  AfterAllCallback,
  BeforeEachCallback,
  AfterEachCallback,
  TestInstanceFactory,
  InvocationInterceptor {
  // Use these components:
  // - RobolectricDependencies.create()
  // - SandboxLifecycleManager
  // - ClassLifecycleManager
  // - TestBootstrapper
  // - ParameterResolver (optional)
}
```

**Required Components**:
| Component | Purpose |
|-----------|---------|
| `RobolectricDependencies` | Bootstrap Robolectric services |
| `SandboxLifecycleManager` | Create/manage sandboxes |
| `ClassLifecycleManager` | Class-level sandbox sharing |
| `TestBootstrapper` | Create test instances |

**Optional Components**:
| Component | Purpose |
|-----------|---------|
| `ParameterResolver` | Custom parameter injection |
| `RunnerLogger` | Debug logging |
| `RunnerMetrics` | Performance metrics |

---

### Kotest Extension

Implement Kotest listener interfaces:

```kotlin
class MyRobolectricExtension : 
  BeforeSpecListener,
  AfterSpecListener,
  BeforeTestListener,
  AfterTestListener {
  // Use these components:
  // - RobolectricDependencies.create()
  // - SandboxLifecycleManager
}
```

**Required Components**:
| Component | Purpose |
|-----------|---------|
| `RobolectricDependencies` | Bootstrap Robolectric services |
| `SandboxLifecycleManager` | Create/manage sandboxes |

**Note**: Kotest manages per-spec sandbox lifecycle differently - sandbox is created once per spec and shared across all tests in that spec.

---

### Generic Framework Adapter

For other frameworks, implement this pattern:

```kotlin
class GenericRobolectricAdapter {
  private val deps = RobolectricDependencies.create()
  private val lifecycleManager = SandboxLifecycleManager(deps)
  private val executor = RobolectricSandboxExecutor(lifecycleManager)
  
  fun runTest(testClass: Class<*>, testMethod: Method) {
    executor.executeSandboxed(testClass, testMethod) { sandbox ->
      val bootstrappedClass = TestBootstrapper.bootstrapClass<Any>(sandbox, testClass)
      val instance = TestBootstrapper.createTestInstance(bootstrappedClass)
      TestBootstrapper.invokeTestMethod(instance, testMethod, emptyArray())
    }
  }
}
```

---

## Component Usage Matrix

| Component | JUnit Platform | JUnit Jupiter | Kotest | Generic |
|-----------|:--------------:|:-------------:|:------:|:-------:|
| `RobolectricDependencies` | ✓ | ✓ | ✓ | ✓ |
| `SandboxLifecycleManager` | ✓ | ✓ | ✓ | ✓ |
| `ClassLifecycleManager` | ✓ | ✓ | - | Optional |
| `RobolectricSandboxExecutor` | ✓ | - | - | ✓ |
| `TestBootstrapper` | ✓ | ✓ | - | ✓ |
| `LifecycleHelper` | ✓ | - | - | Optional |
| `DiscoveryHelpers` | ✓ | Optional | - | Optional |
| `ParameterResolver` | ✓ | ✓ | - | Optional |
| `ManifestResolver` | ✓ | ✓ | ✓ | ✓ |
| `RunnerLogger` | ✓ | ✓ | ✓ | Optional |
| `RunnerMetrics` | ✓ | ✓ | ✓ | Optional |

Legend:
- ✓ = Required/Recommended
- - = Not typically used
- Optional = Use if needed

**Note**: `DiscoveryHelpers` now provides only framework-agnostic test discovery. JUnit Platform-specific descriptor builders (`TestDescriptor`, `UniqueId`, etc.) have been moved to the respective engine modules.

---

## Lifecycle Mapping

### Test Class Lifecycle

```
┌─────────────────────────────────────────────────────────────┐
│                    Test Class Execution                      │
├─────────────────────────────────────────────────────────────┤
│                                                              │
│  1. Class Setup                                              │
│     ├─ JUnit Platform: ClassLifecycleManager.setupForClass()│
│     ├─ JUnit Jupiter: beforeAll() callback                   │
│     └─ Kotest: beforeSpec() callback                         │
│                                                              │
│  2. @BeforeAll / @BeforeClass Methods                        │
│     ├─ JUnit Platform: LifecycleHelper.invokeStaticLifecycle │
│     ├─ JUnit Jupiter: interceptBeforeAllMethod()             │
│     └─ Kotest: N/A (use beforeSpec)                          │
│                                                              │
│  3. For Each Test:                                           │
│     ├─ @BeforeEach / @Before                                 │
│     ├─ Test Method Execution                                 │
│     └─ @AfterEach / @After                                   │
│                                                              │
│  4. @AfterAll / @AfterClass Methods                          │
│     ├─ JUnit Platform: LifecycleHelper.invokeStaticLifecycle │
│     ├─ JUnit Jupiter: interceptAfterAllMethod()              │
│     └─ Kotest: N/A (use afterSpec)                           │
│                                                              │
│  5. Class Teardown                                           │
│     ├─ JUnit Platform: ClassLifecycleManager.tearDownForClass│
│     ├─ JUnit Jupiter: afterAll() callback                    │
│     └─ Kotest: afterSpec() callback                          │
│                                                              │
└─────────────────────────────────────────────────────────────┘
```

### Single Test Lifecycle

```
┌─────────────────────────────────────────────────────────────┐
│                    Single Test Execution                     │
├─────────────────────────────────────────────────────────────┤
│                                                              │
│  1. Get/Create Sandbox                                       │
│     └─ SandboxLifecycleManager.createSandbox() or reuse      │
│                                                              │
│  2. Bootstrap Test Class                                     │
│     └─ TestBootstrapper.bootstrapClass()                     │
│                                                              │
│  3. Create Test Instance                                     │
│     └─ TestBootstrapper.createTestInstance()                 │
│                                                              │
│  4. Resolve Parameters (if any)                              │
│     └─ ParameterResolutionHelper.resolveParameters()         │
│                                                              │
│  5. Execute Test                                             │
│     ├─ Run on sandbox main thread                            │
│     └─ SandboxLifecycleManager.executeInSandbox()            │
│                                                              │
│  6. Cleanup                                                  │
│     └─ Environment reset (handled by lifecycle manager)      │
│                                                              │
└─────────────────────────────────────────────────────────────┘
```

---

## Configuration Mapping

| System Property | JUnit Platform | JUnit Jupiter | Kotest |
|----------------|:--------------:|:-------------:|:------:|
| `robolectric.enabledSdks` | ✓ | ✓ | ✓ |
| `robolectric.alwaysIncludeVariantMarkersInTestName` | ✓ | ✓ | ✓ |
| `robolectric.runner.debug` | ✓ | ✓ | ✓ |
| `robolectric.runner.metrics` | ✓ | ✓ | ✓ |
| `robolectric.runner.metrics.timing` | ✓ | ✓ | ✓ |

---

## Error Handling

| Scenario | JUnit Platform | JUnit Jupiter | Kotest |
|----------|----------------|---------------|--------|
| Test failure | `ExecutionResult.failure()` | Exception propagation | TestResult |
| Setup failure | Skip children, report error | Exception propagation | Spec failure |
| Teardown failure | Log + report | Exception propagation | Log + continue |
| Sandbox creation failure | Skip test, report error | Exception propagation | Spec failure |

---

## Best Practices

### For All Frameworks

1. **Cache Dependencies**: Create `RobolectricDependencies` once per test run
2. **Share Sandboxes**: Use `ClassLifecycleManager` or per-spec sandbox for efficiency
3. **Enable Observability**: Wire `RunnerLogger` and `RunnerMetrics` for debugging
4. **Handle Failures Gracefully**: Always clean up sandbox state on failure

### JUnit Platform Specific

1. Use `DiscoveryHelpers` for framework-agnostic test discovery (methods, classes, SDK variants)
2. Implement JUnit Platform-specific descriptor building in your engine module (see `JupiterDescriptorBuilders` or `PlatformDescriptorBuilders` for examples)
3. Support both `@org.junit.Test` and `@org.junit.jupiter.api.Test`
4. Handle SDK parameterization in discovery phase

### JUnit Jupiter Specific

1. Store state in `ExtensionContext.Store` for proper cleanup
2. Use `TestInstanceFactory` for test instance creation
3. Use `InvocationInterceptor` for sandbox execution

### Kotest Specific

1. Create sandbox once per spec, not per test
2. Handle classloader switching for coroutine support
3. Use spec-level lifecycle callbacks

---

## Migration Guide

### From JUnit 4 Runner to JUnit Platform

1. Replace `@RunWith(RobolectricTestRunner.class)` with test engine
2. Replace `@Config` annotations (still supported)
3. Use `DiscoveryHelpers.isTestMethod()` for `@Test` detection

### From JUnit 4 Runner to JUnit Jupiter

1. Replace `@RunWith(RobolectricTestRunner.class)` with `@ExtendWith(RobolectricExtension.class)`
2. Replace `@Before`/`@After` with `@BeforeEach`/`@AfterEach`
3. Replace `@BeforeClass`/`@AfterClass` with `@BeforeAll`/`@AfterAll`

