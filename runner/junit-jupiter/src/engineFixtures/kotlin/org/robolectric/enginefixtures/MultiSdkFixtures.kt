package org.robolectric.enginefixtures

import android.os.Build
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.robolectric.annotation.Config

/** Fixtures for `MultiSdkEngineTest`; see `EngineGuardFixtures` for the package contract. */
class MultiSdkFixture {

  companion object {
    const val SEEN_PROPERTY = "org.robolectric.enginefixtures.multiSdkSeen"
  }

  @Test
  @Config(sdk = [33, 34])
  fun runsOnBoth() {
    val seen = System.getProperty(SEEN_PROPERTY, "")
    System.setProperty(SEEN_PROPERTY, "$seen,${Build.VERSION.SDK_INT}")
  }
}

/**
 * Multi-SDK method on a class with a shared class environment: an implicit conflict — both variants
 * must fail fast rather than silently run on the wrong SDK.
 */
@Config(sdk = [33])
class MultiSdkWithLifecycleFixture {

  companion object {
    @JvmStatic @BeforeAll fun setUpClass() = Unit
  }

  @Test @Config(sdk = [33, 34]) fun conflictingVariants() = Unit
}
