package org.robolectric.runner.common

/**
 * Identifies the sandbox that a configuration runs in, as [RobolectricSession.sandboxKey] returns
 * it. Configurations with equal keys can run in the same sandbox, one after the other, even if the
 * configurations themselves differ, such as in their qualifiers: an integration groups tests by it
 * to run them in one sandbox.
 */
@ExperimentalRunnerApi
public class SandboxKey
internal constructor(
  /** What makes sandboxes interchangeable. */
  internal val identity: Any,
  /** The API level of the Android SDK of the sandbox. */
  public val sdk: Int,
  /**
   * Names the key as a string that is the same in every JVM for the same configuration, for example
   * to identify a group of tests across test runs. It starts with [sdk].
   */
  public val id: String,
) {
  override fun equals(other: Any?): Boolean = other is SandboxKey && identity == other.identity

  override fun hashCode(): Int = identity.hashCode()

  override fun toString(): String = "SandboxKey($id)"
}
