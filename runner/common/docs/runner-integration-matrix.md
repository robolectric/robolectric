# Robolectric Runner Integration Matrix

This document maps framework integrations to `runner:common` components and links JUnit-specific ownership to their module paths.

## Framework comparison

| Feature | JUnit Platform | JUnit Jupiter |
|---------|---------------|---------------|
| **Module Path** | `runner/junit-platform` | `runner/junit-jupiter` |
| **Entry Point** | `TestEngine` | `TestEngine` + `Extension` |
| **Discovery** | `DiscoveryHelpers` + `PlatformDescriptorBuilders` | `DiscoveryHelpers` + `JupiterDescriptorBuilders` |
| **Class Lifecycle** | `ClassLifecycleManager` | `ClassLifecycleManager` |
| **Test Execution** | `RobolectricSandboxExecutor` | `RobolectricJupiterEngine` / `InvocationInterceptor` |
| **Parameter Injection** | `ParameterResolver` | `ParameterResolver` |
| **Lifecycle Methods** | `LifecycleHelper` | `LifecycleHelper` (engine) + Jupiter callbacks (extension) |
| **Execution Policy** | engine-local (silent isolated fallback on SDK mismatch — known divergence) | `ExecutionPolicyResolver` (both entry points) |
| **SDK Selection** | `SdkFallbackResolver` | `MethodSdkResolver` (SdkPicker + fallback) |

If the custom Jupiter engine and the standard `junit-jupiter` engine are both on the
classpath, select engines explicitly per test task
(`useJUnitPlatform { includeEngines(...) }`). A plain Jupiter class without
`@ExtendWith(RobolectricExtension)` is otherwise discovered by both engines and runs twice,
once sandboxed and once plain. Details in `runner-design.md` §6.4.

---

## Integration points by framework type

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
  // runner:common. See:
  // - runner/junit-jupiter/.../JupiterDescriptorBuilders.kt
  // - runner/junit-platform/.../PlatformDescriptorBuilders.kt
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

**Note**: JUnit Platform-specific components like `TestDescriptor` builders are implemented in the engine modules (`runner/junit-platform` and `runner/junit-jupiter`), not in `runner:common`.

---

### JUnit Jupiter Extension

Implement Jupiter extension interfaces (skip-and-reinvoke model — Jupiter's test instance is
a placeholder; intercepted invocations are re-invoked on a sandbox-loaded twin):

```kotlin
class MyRobolectricExtension :
  BeforeAllCallback,
  AfterAllCallback,
  InvocationInterceptor,
  ParameterResolver {
  // Use these components:
  // - RobolectricDependencies.create()
  // - SandboxLifecycleManager
  // - ClassLifecycleManager
  // - RobolectricSandboxExecutor (isolated per-method environments)
  // - ExecutionPolicyResolver + MethodSdkResolver (canonical execution policy)
  // - TestBootstrapper / TestMethodInvoker
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

**Current behavior note**:
- The JUnit Jupiter TestEngine path also uses `ClassLifecycleManager` for class-level lifecycle orchestration.
- The extension path (`RobolectricExtension`) remains callback/interceptor-driven and uses class context storage in `ExtensionContext.Store`.

---

### Generic framework adapter

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

## Component usage matrix

| Component | JUnit Platform | JUnit Jupiter | Generic |
|-----------|:--------------:|:-------------:|:-------:|
| `RobolectricDependencies` | ✓ | ✓ | ✓ |
| `SandboxLifecycleManager` | ✓ | ✓ | ✓ |
| `ClassLifecycleManager` | ✓ | ✓ | Optional |
| `RobolectricSandboxExecutor` | ✓ | ✓ (extension: isolated per-method environments) | ✓ |
| `TestBootstrapper` | ✓ | ✓ | ✓ |
| `LifecycleHelper` | ✓ | - | Optional |
| `DiscoveryHelpers` | ✓ | Optional | Optional |
| `ParameterResolver` | ✓ | ✓ | Optional |
| `ManifestResolver` | ✓ | ✓ | ✓ |
| `RunnerLogger` | ✓ | ✓ | Optional |
| `RunnerMetrics` | ✓ | ✓ | Optional |

Legend:
- ✓ = Required/Recommended
- - = Not typically used
- Optional = Use if needed

**Note**: `DiscoveryHelpers` now provides only framework-agnostic test discovery. JUnit Platform-specific descriptor builders (`TestDescriptor`, `UniqueId`, etc.) have been moved to the respective engine modules.

---

## Lifecycle mapping

### Test class lifecycle

```
┌─────────────────────────────────────────────────────────────┐
│                    Test Class Execution                      │
├─────────────────────────────────────────────────────────────┤
│                                                              │
│  1. Class Setup                                              │
│     ├─ JUnit Platform: ClassLifecycleManager.setupForClass()│
│     ├─ JUnit Jupiter (engine): ClassLifecycleManager.setupForClass() │
│     └─ JUnit Jupiter (extension): beforeAll() callback       │
│                                                              │
│  2. @BeforeAll / @BeforeClass Methods                        │
│     ├─ JUnit Platform: LifecycleHelper.invokeStaticLifecycle │
│     ├─ JUnit Jupiter (engine): LifecycleHelper.invokeStaticLifecycle │
│     └─ JUnit Jupiter (extension): interceptBeforeAllMethod() │
│                                                              │
│  3. For Each Test:                                           │
│     ├─ @BeforeEach / @Before                                 │
│     ├─ Test Method Execution                                 │
│     └─ @AfterEach / @After                                   │
│                                                              │
│  4. @AfterAll / @AfterClass Methods                          │
│     ├─ JUnit Platform: LifecycleHelper.invokeStaticLifecycle │
│     ├─ JUnit Jupiter (engine): LifecycleHelper.invokeStaticLifecycle │
│     └─ JUnit Jupiter (extension): interceptAfterAllMethod()  │
│                                                              │
│  5. Class Teardown                                           │
│     ├─ JUnit Platform: ClassLifecycleManager.tearDownForClass│
│     ├─ JUnit Jupiter (engine): ClassLifecycleManager.tearDownForClass │
│     └─ JUnit Jupiter (extension): afterAll() callback        │
│                                                              │
└─────────────────────────────────────────────────────────────┘
```

### Single test lifecycle

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

## Configuration mapping

| System Property | JUnit Platform | JUnit Jupiter |
|----------------|:--------------:|:-------------:|
| `robolectric.enabledSdks` | ✓ | ✓ |
| `robolectric.alwaysIncludeVariantMarkersInTestName` | ✓ | ✓ |
| `robolectric.runner.debug` | ✓ | ✓ |
| `robolectric.runner.metrics` | ✓ | ✓ |
| `robolectric.runner.metrics.timing` | ✓ | ✓ |

---

## Error handling

| Scenario | JUnit Platform | JUnit Jupiter |
|----------|----------------|---------------|
| Test failure | `ExecutionResult.failure()` | Exception propagation |
| Setup failure | Skip children, report error | Exception propagation |
| Teardown failure | Log + report | Exception propagation |
| Sandbox/SDK discovery failure | Fail-fast with explicit diagnostic error | Fail-fast with explicit diagnostic error |

---

## Practical notes

### For all frameworks

1. Create `RobolectricDependencies` once per test run and reuse it; it is expensive.
2. Share sandboxes where the tests allow it, via `ClassLifecycleManager` (or a
   `SandboxSharingStrategy` if you go through the façade).
3. Wire `RunnerLogger` and `RunnerMetrics` when debugging; both are off by default.
4. Clean up sandbox state on failure, not just on success.

### JUnit Platform

1. Use `DiscoveryHelpers` for framework-agnostic test discovery (methods, classes, SDK variants)
2. Implement JUnit Platform-specific descriptor building in your engine module (for examples, see `runner/junit-jupiter/.../JupiterDescriptorBuilders.kt` and `runner/junit-platform/.../PlatformDescriptorBuilders.kt`)
3. Support both `@org.junit.Test` and `@org.junit.jupiter.api.Test`
4. Handle SDK parameterization in discovery phase

### JUnit Jupiter

1. Store state in `ExtensionContext.Store` for proper cleanup
2. Do **not** use `TestInstanceFactory` to return sandbox-loaded instances — Jupiter rejects
   instances from a foreign classloader. Treat Jupiter's instance as a placeholder instead.
3. Use `InvocationInterceptor` to skip original invocations and re-invoke them on a
   sandbox-loaded twin (`TestMethodInvoker`)

## Migration guide

### From the JUnit 4 runner to the Platform engine

1. Remove `@RunWith(RobolectricTestRunner.class)`; the engine discovers plain
   `@org.junit.Test` methods once it is on the classpath and selected in the build.
2. Keep `@Config`; the engine reads it the same way the JUnit 4 runner does.
3. Select the engine per test task (`includeEngines("robolectric-junit-platform-engine")`)
   so no other engine claims the same classes.

### From the JUnit 4 runner to the Jupiter extension

1. Replace `@RunWith(RobolectricTestRunner.class)` with
   `@ExtendWith(RobolectricExtension.class)`. `@Config` keeps working.
2. Replace `@Before`/`@After` with `@BeforeEach`/`@AfterEach` and
   `@BeforeClass`/`@AfterClass` with `@BeforeAll`/`@AfterAll`.
3. JUnit 4 rules have no Jupiter equivalent. An `ActivityScenarioRule` becomes a plain
   `ActivityScenario.launch(...).use { }` block; custom `TestRule`s have to be rewritten as
   Jupiter extensions or inlined into the test.
4. Keep Android calls out of constructors and field initializers; the placeholder instance
   is created outside the sandbox (`runner-design.md` §8.2). Use `lazy` or `@BeforeEach`.
