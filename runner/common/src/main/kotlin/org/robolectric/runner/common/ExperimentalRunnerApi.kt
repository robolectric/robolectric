package org.robolectric.runner.common

/**
 * Marks declarations in the Robolectric runner common API as experimental.
 *
 * This annotation indicates that the API is subject to change in future releases. It is recommended
 * to use this API with caution and be prepared for potential breaking changes.
 *
 * Usage:
 * ```kotlin
 * @ExperimentalRunnerApi
 * class MyCustomRunner { ... }
 * ```
 */
@RequiresOptIn(
  message = "This Robolectric runner API is experimental and may change in future releases.",
  level = RequiresOptIn.Level.WARNING,
)
@Retention(AnnotationRetention.BINARY)
@Target(
  AnnotationTarget.CLASS,
  AnnotationTarget.FUNCTION,
  AnnotationTarget.PROPERTY,
  AnnotationTarget.TYPEALIAS,
)
annotation class ExperimentalRunnerApi
