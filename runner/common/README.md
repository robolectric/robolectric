# Robolectric Runner Common

This module provides **framework-agnostic** shared infrastructure for building Robolectric test runners for any testing framework.

## Key Features

- ✅ **No JUnit dependencies** - Works with any test framework (no compile-time dependencies on JUnit Platform or Jupiter)
- ✅ **High-level integration API** - Simple RobolectricIntegration interface
- ✅ **Flexible sandbox lifecycle** - Multiple sharing strategies
- ✅ **SDK parameterization** - Run tests across multiple Android SDK versions
- ✅ **Observable & debuggable** - Built-in logging and metrics

> **Note**: As of version 4.x, this module is fully framework-agnostic. JUnit Platform-specific code (like `TestDescriptor` builders) has been moved to the respective engine modules (`:runner:junit-jupiter` and `:runner:junit-platform`).

## Quick Start
### Option 1: Using RobolectricIntegration (Recommended)
The simplest way to integrate Robolectric with your test framework:
```kotlin
// Create an integration
val integration = RobolectricIntegrationBuilder
  .forJUnitPlatform()  // or forJUnitJupiter()
  .enableDebugLogging()
  .build()
// In your test framework's lifecycle:
integration.beforeClass(testClass)
integration.beforeTest(testClass, testMethod)
// Execute the test in the sandbox
integration.executeInSandbox(testClass, testMethod) { context ->
  // Test code runs here with full Android environment
  val bootstrappedClass = TestBootstrapper.bootstrapClass<Any>(
    context.sandbox,
    context.testClass
  )
  // ... run the test
}
integration.afterTest(testClass, testMethod, success = true)
integration.afterClass(testClass)
```
### Option 2: Using RunnerConfiguration
For more control over configuration:
```kotlin
val config = RunnerConfiguration.builder()
  .sandboxSharing(SandboxSharingStrategy.PER_CLASS)
  .enableDebugLogging()
  .enableMetrics()
  .forJUnit5()
  .build()
val integration = config.createIntegration()
```
### Option 3: Low-Level API
For full control, use the individual components directly:
```kotlin
// Initialize dependencies
val deps = RobolectricDependencies.create()
val lifecycleManager = SandboxLifecycleManager(deps)
val executor = RobolectricSandboxExecutor(lifecycleManager)
// Execute a test
val result = executor.executeTest(
  testClass = MyTest::class.java,
  testMethod = testMethod,
  beforeEachAnnotations = listOf(BeforeEach::class.java),
  afterEachAnnotations = listOf(AfterEach::class.java),
) { context ->
  // Test code
}
when (result) {
  is ExecutionResult.Success -> println("Passed in ${result.durationMs}ms")
  is ExecutionResult.Failure -> throw result.error
  is ExecutionResult.Skipped -> println("Skipped: ${result.reason}")
}
```
## Architecture
```
┌─────────────────────────────────────────────────────────────────────┐
│                         Test Framework                               │
│  (JUnit Platform, JUnit Jupiter, Custom)                            │
└─────────────────────────────────────────────────────────────────────┘
                                    │
          ┌─────────────────────────┼─────────────────────────┐
          ▼                         ▼                         ▼
┌───────────────────┐    ┌───────────────────┐    ┌───────────────────┐
│RobolectricIntegration│ │RunnerConfiguration│    │ Low-Level APIs    │
│  (High-Level)       │ │  (Configuration)   │    │                   │
└───────────────────┘    └───────────────────┘    └───────────────────┘
          │                         │                         │
          └─────────────────────────┼─────────────────────────┘
                                    ▼
┌─────────────────────────────────────────────────────────────────────┐
│                        runner:common Core                            │
│  ┌──────────────────────┐  ┌──────────────────────┐                 │
│  │ SandboxLifecycleManager│ │ RobolectricSandboxExecutor │          │
│  └──────────────────────┘  └──────────────────────┘                 │
│  ┌──────────────────────┐  ┌──────────────────────┐                 │
│  │ TestBootstrapper     │  │ LifecycleHelper      │                 │
│  └──────────────────────┘  └──────────────────────┘                 │
│  ┌──────────────────────┐  ┌──────────────────────┐                 │
│  │ DiscoveryHelpers     │  │ ParameterResolver    │                 │
│  └──────────────────────┘  └──────────────────────┘                 │
└─────────────────────────────────────────────────────────────────────┘
                                    │
                                    ▼
┌─────────────────────────────────────────────────────────────────────┐
│                     Robolectric Core                                 │
│  (AndroidSandbox, TestEnvironment, ShadowProviders)                 │
└─────────────────────────────────────────────────────────────────────┘
```
## API Reference
### High-Level APIs
#### RobolectricIntegration
The main interface for framework integrations:
```kotlin
interface RobolectricIntegration {
  fun beforeClass(testClass: Class<*>)
  fun beforeTest(testClass: Class<*>, testMethod: Method)
  fun afterTest(testClass: Class<*>, testMethod: Method, success: Boolean)
  fun afterClass(testClass: Class<*>)
  fun getContext(testClass: Class<*>): TestExecutionContext?
  fun <T> executeInSandbox(testClass: Class<*>, testMethod: Method, block: (TestExecutionContext) -> T): T
}
```
#### SandboxSharingStrategy
Controls how sandboxes are shared across tests:
| Strategy | Description | Use Case |
|----------|-------------|----------|
| `PER_TEST` | New sandbox for each test | Maximum isolation |
| `PER_CLASS` | Share within class (default) | Balance of speed/isolation |
| `PER_SDK` | Share across classes with same SDK | Faster multi-class runs |
| `GLOBAL` | Single sandbox for all tests | Fastest, least isolation |
#### LifecycleAnnotations
Predefined lifecycle annotation sets:
| Value | Description |
|-------|-------------|
| `JUNIT4` | @Before, @After, @BeforeClass, @AfterClass |
| `JUNIT5` | @BeforeEach, @AfterEach, @BeforeAll, @AfterAll |
| `JUNIT_COMBINED` | Both JUnit 4 and JUnit 5 annotations |
| `NONE` | No lifecycle annotations |
### Core Components
#### SandboxLifecycleManager
Manages sandbox creation and lifecycle:
```kotlin
val manager = SandboxLifecycleManager(dependencies)
// Create sandbox for a test
val context = manager.createSandbox(testClass, testMethod)
// Execute in sandbox
manager.executeInSandbox(context, testName) {
  // Code runs in sandbox
}
```
#### TestBootstrapper
Handles class loading in the sandbox:
```kotlin
// Bootstrap a class into the sandbox
val bootstrappedClass = TestBootstrapper.bootstrapClass<MyTest>(sandbox, MyTest::class.java)
// Create an instance
val instance = TestBootstrapper.createTestInstance(bootstrappedClass)
// Invoke a test method
TestBootstrapper.invokeTestMethod(instance, testMethod, args)
```
#### DiscoveryHelpers
Utilities for test discovery:
```kotlin
// Discover test methods (framework-agnostic)
val tests = DiscoveryHelpers.discoverTestMethods(
  testClass,
  listOf(Test::class.java),
  filter = TestFilter.byMethodName(Regex(".*Fast.*"))
)
// Create SDK variants
val variants = DiscoveryHelpers.createSdkVariants(testClass, testMethod, deps)
```

**Note**: This is now fully framework-agnostic. If you need JUnit Platform-specific test descriptor building (e.g., `TestDescriptor`, `UniqueId`), see the `JupiterDescriptorBuilders` in `:runner:junit-jupiter` or `PlatformDescriptorBuilders` in `:runner:junit-platform` modules.
### Configuration
#### System Properties
| Property                                            | Description                | Default          |
|-----------------------------------------------------|----------------------------|------------------|
| `robolectric.runner.debug`                          | Enable debug logging       | `false`          |
| `robolectric.runner.metrics`                        | Enable metrics collection  | `false`          |
| `robolectric.runner.metrics.timing`                 | Enable timing metrics      | `false`          |
| `robolectric.enabledSdks`                           | Comma-separated SDK levels | (all configured) |
| `robolectric.alwaysIncludeVariantMarkersInTestName` | Include SDK in test names  | `false`          |
## Creating a Custom Integration
### Step 1: Implement Test Discovery
```kotlin
class MyTestEngine : TestEngine {
  override fun discover(request: EngineDiscoveryRequest, uniqueId: UniqueId): TestDescriptor {
    val root = EngineDescriptor(uniqueId, "My Engine")
    
    // Use DiscoveryHelpers for framework-agnostic test discovery
    request.getSelectorsByType(ClassSelector::class.java).forEach { selector ->
      val testClass = selector.getJavaClass()
      val methods = DiscoveryHelpers.discoverTestMethods(
        testClass,
        listOf(MyTest::class.java)
      )
      
      // Build your own descriptors (JUnit Platform-specific)
      // Note: runner:common no longer provides TestDescriptor builders
      // See JupiterDescriptorBuilders or PlatformDescriptorBuilders for examples
      methods.forEach { method ->
        val variants = DiscoveryHelpers.createSdkVariants(testClass, method, deps)
        variants.forEach { variant ->
          root.addChild(MyTestDescriptor(variant))
        }
      }
    }
    return root
  }
}
```
### Step 2: Implement Test Execution
```kotlin
override fun execute(request: ExecutionRequest) {
  val integration = RobolectricIntegrationBuilder
    .forJUnitPlatform()
    .build()
  request.rootTestDescriptor.descendants.forEach { descriptor ->
    if (descriptor is MyTestDescriptor) {
      val listener = request.engineExecutionListener
      listener.executionStarted(descriptor)
      try {
        integration.beforeClass(descriptor.testClass)
        integration.beforeTest(descriptor.testClass, descriptor.method)
        integration.executeInSandbox(descriptor.testClass, descriptor.method) { context ->
          // Run the test
          val bootstrappedClass = TestBootstrapper.bootstrapClass<Any>(
            context.sandbox,
            context.testClass
          )
          val instance = TestBootstrapper.createTestInstance(bootstrappedClass)
          TestBootstrapper.invokeTestMethod(instance, descriptor.method, emptyArray())
        }
        integration.afterTest(descriptor.testClass, descriptor.method, success = true)
        listener.executionFinished(descriptor, TestExecutionResult.successful())
      } catch (e: Throwable) {
        integration.afterTest(descriptor.testClass, descriptor.method, success = false)
        listener.executionFinished(descriptor, TestExecutionResult.failed(e))
      } finally {
        integration.afterClass(descriptor.testClass)
      }
    }
  }
}
```
### Step 3: Handle Parameters (Optional)
```kotlin
// Create a custom parameter resolver
class MyParameterResolver : ParameterResolver {
  override fun resolveParameter(parameter: Parameter, sandbox: AndroidSandbox): Any? {
    return when {
      parameter.type == Application::class.java -> 
        sandbox.bootstrappedClass(Application::class.java)
          .let { ApplicationProvider.getApplicationContext() }
      parameter.type == Context::class.java ->
        ApplicationProvider.getApplicationContext()
      else -> null
    }
  }
}
// Use it in your integration
val integration = RobolectricIntegrationBuilder
  .forJUnitPlatform()
  .parameterResolver(MyParameterResolver())
  .build()
```
## Troubleshooting
### Common Issues
#### "No sandbox found for SDK X"
The requested SDK is not available. Check:
1. The SDK is included in `robolectric.enabledSdks`
2. The SDK JAR is available in the classpath
3. The @Config annotation doesn't specify an unsupported SDK
#### ClassNotFoundException in sandbox
The class wasn't bootstrapped properly:
```kotlin
// Wrong - using original class
val clazz = MyClass::class.java
// Right - bootstrap into sandbox
val clazz = TestBootstrapper.bootstrapClass<MyClass>(sandbox, MyClass::class.java)
```
#### Tests run slowly
Consider using a less isolating sharing strategy:
```kotlin
val config = RunnerConfiguration.builder()
  .sandboxSharing(SandboxSharingStrategy.PER_SDK)  // or GLOBAL
  .build()
```
### Debug Logging
Enable debug logging to see detailed execution flow:
```bash
./gradlew test -Drobolectric.runner.debug=true
```
### Metrics
Enable metrics to see execution statistics:
```bash
./gradlew test -Drobolectric.runner.metrics=true
```
## See Also
- [JUnit Platform Integration](../junit-platform/README.md)
- [JUnit Jupiter Extension](../junit-jupiter/README.md)
- [Robolectric Configuration](../../robolectric/README.md)
