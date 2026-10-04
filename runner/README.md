# Robolectric runners

Experimental modules for running Robolectric outside `RobolectricTestRunner`.
They require Java 17 and Kotlin 2.2+. Use the same version for all Robolectric
artifacts.

| Module | Artifact | Purpose |
| ------ | -------- | ------- |
| `:runner:junit-jupiter` | `runner-junit-jupiter` | JUnit Platform engine and Jupiter extension |
| `:runner:common` | `runner-common` | Framework-independent API |

## JUnit Jupiter

Requires JUnit 6, SDK 26+, and `PAUSED` or `INSTRUMENTATION_TEST` looper mode.

```kotlin
dependencies {
  testImplementation("org.robolectric:runner-junit-jupiter:<robolectric-version>")
}

tasks.withType<Test>().configureEach { useJUnitPlatform() }
```

Android modules also need
`android.testOptions.unitTests.isIncludeAndroidResources = true`. Don't exclude
the `robolectric` engine.

```kotlin
@ExtendWith(RobolectricExtension::class)
@Config(sdk = [34])
class MyTest {
  @Test
  fun usesAndroid() {
    assertThat(ApplicationProvider.getApplicationContext<Context>().packageName)
      .isNotEmpty()
  }
}
```

- One sandbox per JVM (select one SDK with `robolectric.enabledSdks`; fork for
  other SDKs, modes, or instrumentation).
- Tests run sequentially on the Android main thread. Each test gets a fresh
  application, except `@TestInstance(PER_CLASS)` shares one across the class.
- JUnit 4 tests on `junit-vintage-engine` share the sandbox when SDK and modes
  match (`robolectric.graphicsMode=NATIVE` is the default here; disable sharing
  with `robolectric.runner.shareSandboxes=false`).

## Framework-independent API

```kotlin
RobolectricSession.create().use { session ->
  val config = session.plan(testClass, testMethod).first()
  session.open(config).use { android ->
    android.run { android.loadClass(testClass) }
  }
}
```

Use `lease`/`prepare` before `open` for class-level setup.
