# AGENTS.md

Quick guide for coding agents working on Robolectric. The full contributor rules
are in [.github/CONTRIBUTING.md](.github/CONTRIBUTING.md).

## Overview

Robolectric runs Android tests on the JVM. It loads instrumented Android
framework jars (`android-all`, SDK 24 to 37) into a sandbox class loader and
routes some calls to Java *shadow* classes. Some native code, such as SQLite and
optionally graphics, runs for real through the `nativeruntime` module.

## Modules

| Module | Contents |
|---|---|
| `shadows/framework` | Shadows (`org.robolectric.shadows.Shadow*`), fakes, `RuntimeEnvironment`, `ActivityController` and other controllers |
| `shadowapi` | `Shadow`, `ReflectionHelpers` and other shadow-facing API |
| `annotations` | `@Implements`, `@Implementation`, `@RealObject`, `@Resetter`, `@Config` |
| `processor`, `processor-ksp` | Annotation processors that generate shadow metadata (`ShadowProvider`) |
| `sandbox` | Sandbox class loader, bytecode instrumentation, `ShadowMap` |
| `robolectric` | Test runner, `Robolectric`, SDK plugins, and most shadow tests |
| `resources` | Resource and manifest loading |
| `nativeruntime` | Native runtime and graphics support |
| `pluginapi`, `plugins` | Plugin interfaces and the Maven dependency resolver |
| `junit` | JUnit integration |
| `utils`, `utils/reflector` | Utilities, the injector (`PluginFinder`) and the `@ForType` reflector |
| `simulator`, `simulator-gradle-plugin` | Desktop simulator that runs an Android app in a sandbox, and its Gradle plugin |
| `integration_tests/*` | Cross-cutting tests, such as `androidx`, `ctesque` (CTS-style), `kotlin` and `mockito` |
| `build-logic` | Gradle convention plugins |

## Build and test

These instructions apply to the public GitHub repository
([robolectric/robolectric](https://github.com/robolectric/robolectric)), which
builds with Gradle.

- Use `./gradlew` with network access; offline builds fail unless every
  dependency is cached.
- CI uses the JDK version in `.github/.java-version`.
- Set `SKIP_ERRORPRONE=true` (and optionally `SKIP_JAVADOC=true`) for faster
  builds, as CI does.
- Run the smallest set of tests that covers the change:

  ```shell
  SKIP_ERRORPRONE=true ./gradlew :robolectric:test --tests '*ShadowActivityTest'
  SKIP_ERRORPRONE=true ./gradlew :utils:test --tests '*PluginFinderTest'
  SKIP_ERRORPRONE=true ./gradlew :integration_tests:androidx:testDebugUnitTest --tests '*MultiDisplayTest'
  SKIP_ERRORPRONE=true ./gradlew :integration_tests:ctesque:testDebugUnitTest --tests 'android.display.DisplayTest'
  ```

- Most tests here run once per supported SDK (`sdk=ALL_SDKS` in their
  `robolectric.properties`). Narrow them with `-Drobolectric.enabledSdks=33,36`;
  Gradle forwards `-Drobolectric.*` properties to the test JVM. In test code,
  use `@Config(sdk = ...)` or `@Config(minSdk = ..., maxSdk = ...)`.
- Shadow tests live in
  `robolectric/src/test/java/org/robolectric/shadows/ShadowFooTest.java`. Get a
  shadow with the generated `Shadows.shadowOf(...)`.
- Instrumented tests (`ctesque`, `androidx_test`) run on Gradle managed
  emulators for API 30 to 37. This needs the Android SDK and KVM. Use
  `connectedDebugAndroidTest` for an attached device.

  ```shell
  ./gradlew -Pandroid.sdk.channel=3 :integration_tests:ctesque:nexusOneApi36DebugAndroidTest
  ```

- Results are XML files in `<module>/build/test-results/test/`, or
  `testDebugUnitTest/` for Android modules.
- `./gradlew assemble testClasses` compiles everything.
- Declare dependencies and their versions in `gradle/libs.versions.toml`.
  Modules cannot declare their own repositories.
- Run `./gradlew spotlessApply` before committing. CI runs `spotlessCheck`,
  `detekt` and `lint`, and actionlint on workflow changes.
- To try a change in another project, run
  `SKIP_JAVADOC=true ./gradlew publishToMavenLocal`, add `mavenLocal()` to that
  project's repositories, and depend on the `thisVersion` from
  `gradle.properties` (for example `org.robolectric:robolectric:4.19-SNAPSHOT`).

## Code style

- Spotless formats Java with google-java-format and Kotlin with ktfmt (Google
  style).
- Wrap Markdown at 80 columns, except for tables, code blocks and long links.
- Tests use JUnit 4 and Truth. Use `assertThrows`, not `@Test(expected = ...)`.
- Every behavior change needs a test.
- Java modules compile with source level 11, so do not use newer language
  features such as records or text blocks.
- Add comments only where the code needs clarification.
- Keep changes focused. Do not reformat or refactor unrelated code.

## Shadows

- Run as much real Android code as possible. Before adding or changing a shadow
  method, check the Android source (for example on
  [cs.android.com](https://cs.android.com)) to see whether the real code can
  already run on the host; if so, prefer it over a shadow.
- A shadow is annotated `@Implements(Foo.class)`. Its `@Implementation` methods
  match the framework method signatures and are `protected`. Use `@RealObject`
  for the real instance and `@Resetter` to clear static state between tests.
- Limit methods to an SDK range with
  `@Implementation(minSdk = ..., maxSdk = ...)`, using `Build.VERSION_CODES`
  constants. Framework signatures often change between SDKs, so check the method
  in several `android-all` jars before choosing parameter types.
- Reach hidden APIs through `@ForType` reflectors or `ReflectionHelpers`.
- Shadow a hidden class with
  `@Implements(className = "...", isInAndroidSdk = false)`. For parameter or
  return types missing from older SDKs, or not visible to the shadow (such as
  package-private or private types), use `Object` annotated with
  `@ClassName("...")`.
- Document every `@Implementation` that differs from the framework, and every
  other public method. Never write "Shadow for X".
- Shadow registration is generated by the annotation processor; do not edit
  generated code.
- Prefer fixing an existing shadow or controller over adding public API.

## Simulator

- `simulator` runs an Android app in a Robolectric sandbox inside a Swing
  window, with native graphics and SQLite. `SimulatorMain` takes the app's
  resources APK and launches its activity.
- `simulator-gradle-plugin` (plugin ID `org.robolectric.simulator`) adds a
  `simulate<Variant>` task, such as `simulateDebug`, to app modules. It needs
  `android.testOptions.unitTests.isIncludeAndroidResources = true`.
- The end-to-end test builds `integration_tests/simulator/app`, launches it and
  checks the rendered pixels:

  ```shell
  SKIP_ERRORPRONE=true ./gradlew :integration_tests:simulator:test:test
  ```

## Compatibility

- Changes must work on every supported SDK. When in doubt, run the affected
  tests on an old (24 or 26), a middle (31 to 34) and the newest SDK.
- When a shadow references a framework type that older SDKs lack (in a field, a
  method signature or the shadowed class), add a test that uses the shadow to
  `integration_tests/sdkcompat`, to catch a `NoClassDefFoundError` on older
  SDKs.
- Mark public API `@Deprecated`, with migration notes in its `@deprecated`
  Javadoc, in at least a patch release before removing it in the next minor
  release.

## Commits and pull requests

- PRs target `master` and normally contain one commit. Split a large change into
  several commits only when each one is meaningful on its own and keeps the
  build and tests passing.
- Each commit needs a title of at most 120 characters (not "Internal") and a
  body explaining what changed and why. Body lines are at most 120 characters,
  except for links. CI checks this in
  `.github/workflows/validate_commit_message.yml`.

## Gotchas

- CI skips Error Prone, so `master` can contain Error Prone failures in
  unrelated code. Use `SKIP_ERRORPRONE=true` instead of fixing them in passing.
- Tests use pre-instrumented `android-all` jars by default, which skip runtime
  instrumentation. When changing bytecode instrumentation in `sandbox`, run
  tests with `-Drobolectric.usePreinstrumentedJars=false`.
- Tests in `integration_tests/ctesque/src/sharedTest` also run on emulators as
  instrumented tests, so they must match real-device behavior.
- The Gradle build and configuration caches are on. If a result looks stale,
  rerun that task with `--rerun-tasks`.
