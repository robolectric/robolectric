# Robolectric runners

Experimental, opt-in modules that run Robolectric outside
`RobolectricTestRunner`. Core Robolectric modules don't depend on them. Use the
same version for all Robolectric artifacts.

| Module | Artifact | Purpose |
| ------ | -------- | ------- |
| `:runner:junit-jupiter` | `runner-junit-jupiter` | JUnit Platform engine and Jupiter extension |
| `:runner:kotest` | `runner-kotest` | Kotest extension |
| `:runner:kotest-gradle-plugin` | plugin `org.robolectric.kotest` | Android-aware spec discovery for Kotest's Gradle plugin |
| `:runner:common` | `runner-common` | Framework-independent API |

## JUnit Jupiter

Requires Java 17, JUnit 6, and Kotlin 2.2 or newer.

```kotlin
dependencies {
  testImplementation("org.robolectric:runner-junit-jupiter:<robolectric-version>")
}

tasks.withType<Test>().configureEach { useJUnitPlatform() }
```

Android modules also need
`android.testOptions.unitTests.isIncludeAndroidResources = true`. Don't limit
the engines to `junit-jupiter`: the `robolectric` engine runs the tests that use
`RobolectricExtension`.

```kotlin
@ExtendWith(RobolectricExtension::class)
@Config(sdk = [34])
class MyTest {
  @Test
  fun usesAndroid() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    assertThat(context.packageName).isNotEmpty()
  }
}
```

Jupiter itself runs inside the sandbox, so test instances, extensions and
arguments use sandbox classes.

Each JVM uses one sandbox for all its tests:

- Tests in a JVM must use the same SDK, instrumentation and looper mode. Select
  one SDK from a wider `@Config` with `robolectric.enabledSdks`, and run other
  SDKs in separate test tasks or forks.
- Tests need SDK 26 or newer, native graphics (the default here), native
  SQLite, and the `PAUSED` or `INSTRUMENTATION_TEST` looper mode.

Lifecycle:

- Class-level code, such as `@BeforeAll`, runs in the sandbox before an
  application exists.
- Each test gets a new application, set up before the test instance is created
  and torn down after `@AfterEach`.
- With `@TestInstance(PER_CLASS)`, the tests of a class share one application,
  so they need compatible configurations.
- Tests run sequentially on the Android main thread. Concurrent launchers in
  one JVM aren't supported.

JUnit 4 tests can keep running on `junit-vintage-engine`, with their usual
runner and lifecycle. They share the sandbox only when their SDK and modes
match, so set `robolectric.graphicsMode=NATIVE` for them too. Set
`robolectric.runner.shareSandboxes=false` to stop sharing.

## Kotest

```kotlin
dependencies {
  testImplementation("org.robolectric:runner-kotest:<robolectric-version>")
  testImplementation("io.kotest:kotest-runner-junit6:<kotest-version>")
}

tasks.withType<Test>().configureEach { useJUnitPlatform() }
```

```kotlin
@ApplyExtension(RobolectricExtension::class)
@Config(sdk = [33, 34])
class MySpec : FunSpec({
  test("uses Android") {
    ApplicationProvider.getApplicationContext<Context>()
      .packageName.shouldNotBeEmpty()
  }
})
```

Use `org.robolectric.kotest.RobolectricExtension`, or implement
`RobolectricSpec`. Specs need a no-argument constructor and run once for each of
their SDKs, with SDK markers in test names. Android state lives for the spec
instance, or for each root test with `IsolationMode.InstancePerRoot`. Coroutine
tests can use `Dispatchers.Main` and `coroutineTestScope = true`.

With Kotest's Gradle plugin, also apply `org.robolectric.kotest` so spec
discovery can use Android. Other launchers can add the `runner-kotest` jar as a
`-javaagent`.

## Framework-independent API

```kotlin
RobolectricSession.create().use { session ->
  val configuration = session.plan(testClass, testMethod).first()
  session.open(configuration).use { android ->
    android.run {
      val test = android.loadClass(testClass)
      // Run sandbox-loaded code here.
    }
  }
}
```

`plan` resolves configurations without setting up Android. `open` sets up an
application, and closing it tears the application down. To run class-level code
first, `lease` a sandbox, `prepare` it, and open each test's environment from
it. The API targets Java 11 and Kotlin 2.2 metadata.
