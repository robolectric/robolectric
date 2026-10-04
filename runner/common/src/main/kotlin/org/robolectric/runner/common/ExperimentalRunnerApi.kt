package org.robolectric.runner.common

/**
 * Marks the API that test framework integrations build on. It may change or be removed without
 * notice until it has been validated by more than one integration.
 */
@RequiresOptIn(
  message = "This Robolectric runner API is experimental and may change in future releases.",
  level = RequiresOptIn.Level.ERROR,
)
@Retention(AnnotationRetention.BINARY)
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY)
@MustBeDocumented
public annotation class ExperimentalRunnerApi
