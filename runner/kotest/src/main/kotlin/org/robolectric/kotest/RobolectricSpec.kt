package org.robolectric.kotest

import io.kotest.core.extensions.ApplyExtension
import org.robolectric.kotest.internal.LauncherTakeover

/**
 * Applies [RobolectricExtension] to the spec classes that implement it, as
 * `@ApplyExtension(RobolectricExtension::class)` does:
 * ```
 * class MySpec : FunSpec({ ... }), RobolectricSpec
 * ```
 *
 * Other than with the annotation, the static initializers of the spec class, such as that of a
 * companion object, can then use Android with Kotest's own launcher too: the JVM initializes this
 * interface with the spec class, which gives the extension its turn before they run.
 *
 * This interface is experimental: its behavior may change in future releases.
 */
@ApplyExtension(RobolectricExtension::class)
public interface RobolectricSpec {
  // The JVM only initializes an interface with a class if it has a function that isn't abstract.
  @Suppress("UnusedPrivateMember") private fun isInitializedWithSpecClasses() = Unit

  private companion object {
    init {
      LauncherTakeover.specClassInitializes()
    }
  }
}
