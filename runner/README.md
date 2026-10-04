# Robolectric runners

Experimental, opt-in modules that run Robolectric outside
`RobolectricTestRunner`. Core Robolectric modules don't depend on them. Use the
same version for all Robolectric artifacts.

| Module | Artifact | Purpose |
| ------ | -------- | ------- |
| `:runner:common` | `runner-common` | Framework-independent API |

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
