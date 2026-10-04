# Robolectric runners

Experimental modules for running Robolectric outside `RobolectricTestRunner`.
They require Java 17 and Kotlin 2.2+. Use the same version for all Robolectric
artifacts.

| Module | Artifact | Purpose |
| ------ | -------- | ------- |
| `:runner:common` | `runner-common` | Framework-independent API |

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
