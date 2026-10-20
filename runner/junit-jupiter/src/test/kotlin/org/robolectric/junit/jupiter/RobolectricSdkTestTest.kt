package org.robolectric.junit.jupiter

import android.os.Build
import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

/** Tests [RobolectricSdkTest]. */
class RobolectricSdkTestTest {
  @Test
  fun `runs once on each SDK, named as the test runner names them`() {
    val results = Fixtures.run(OnTwoSdks::class)

    assertThat(results.failed).isEmpty()
    assertThat(results.succeeded).containsExactly("runsOnBoth[33]", "runsOnBoth").inOrder()
    assertThat(results.events).containsExactly("33", "34").inOrder()
  }

  @Test
  fun `marks every SDK when variant markers are always included`() {
    val results =
      Fixtures.run(
        OnTwoSdks::class,
        properties = mapOf("robolectric.alwaysIncludeVariantMarkersInTestName" to "true"),
      )

    assertThat(results.succeeded).containsExactly("runsOnBoth[33]", "runsOnBoth[34]").inOrder()
  }

  @Test
  fun `runs only on the enabled SDKs`() {
    val results =
      Fixtures.run(OnTwoSdks::class, properties = mapOf("robolectric.enabledSdks" to "34"))

    assertThat(results.succeeded).containsExactly("runsOnBoth")
    assertThat(results.events).containsExactly("34")
  }

  @Test
  fun `shares the environment of its class on that SDK, and is on its own on another`() {
    val results = Fixtures.run(InASharedEnvironment::class)

    assertThat(results.failed).isEmpty()
    assertThat(results.events).containsExactly("33 shared=false", "34 shared=true").inOrder()
  }

  @Test
  fun `gets an environment of its own if it is configured differently than its class`() {
    val results = Fixtures.run(ConfiguredDifferentlyThanItsSharedEnvironment::class)

    assertThat(results.failed).isEmpty()
    assertThat(results.events).containsExactly("34 land shared=false")
  }

  @Test
  fun `on a class, runs the class once for each SDK of its tests`() {
    val results = Fixtures.run(ClassOnTwoSdks::class)

    assertThat(results.failed).isEmpty()
    assertThat(results.containers).containsAtLeast("SDK 33", "SDK 34")
    assertThat(results.succeeded).containsExactly("onBoth()", "onBoth()", "onlyOnSdk34()")
    assertThat(results.skipped["onlyOnSdk34()"])
      .contains("is not configured for the Android SDK 33")
    assertThat(results.events).containsExactly("onBoth 33", "onBoth 34", "onlyOnSdk34 34")
  }

  @Test
  fun `on a class that shares environments, each run shares the one of its SDK`() {
    val results = Fixtures.run(SharingClassOnTwoSdks::class)

    assertThat(results.failed).isEmpty()
    assertThat(results.containers).containsAtLeast("SDK 33", "SDK 34")
    assertThat(results.events)
      .containsExactly(
        "setUpClass 33",
        "setUpClass 34",
        "test 33 shared=true",
        "test 34 shared=true",
      )
      .inOrder()
  }

  @Test
  fun `on a class, runs for the enabled SDKs only`() {
    val results =
      Fixtures.run(ClassOnTwoSdks::class, properties = mapOf("robolectric.enabledSdks" to "33"))

    assertThat(results.failed).isEmpty()
    assertThat(results.containers).doesNotContain("SDK 34")
    assertThat(results.events).containsExactly("onBoth 33")
  }

  @Fixture
  @ExtendWith(RobolectricExtension::class)
  @RobolectricSdkTest
  @Config(sdk = [33, 34])
  class ClassOnTwoSdks {
    @Test
    fun onBoth() {
      Fixtures.record("onBoth ${Build.VERSION.SDK_INT}")
    }

    @Test
    @Config(sdk = [34])
    fun onlyOnSdk34() {
      Fixtures.record("onlyOnSdk34 ${Build.VERSION.SDK_INT}")
    }
  }

  @Fixture
  @ExtendWith(RobolectricExtension::class)
  @RobolectricSdkTest
  @Config(sdk = [33, 34])
  class SharingClassOnTwoSdks {
    @Test
    fun test() {
      Fixtures.record("test ${Build.VERSION.SDK_INT} shared=$setUpClassRan")
    }

    companion object {
      private var setUpClassRan = false

      @JvmStatic
      @BeforeAll
      fun setUpClass() {
        setUpClassRan = true
        Fixtures.record("setUpClass ${Build.VERSION.SDK_INT}")
      }
    }
  }

  @Fixture
  @ExtendWith(RobolectricExtension::class)
  @Config(sdk = [34])
  class ConfiguredDifferentlyThanItsSharedEnvironment {
    @RobolectricSdkTest
    @Config(qualifiers = "land")
    fun inLandscape() {
      val orientation = if (RuntimeEnvironment.getQualifiers().contains("land")) "land" else "port"
      Fixtures.record("${Build.VERSION.SDK_INT} $orientation shared=$setUpClassRan")
    }

    companion object {
      private var setUpClassRan = false

      @JvmStatic
      @BeforeAll
      fun setUpClass() {
        setUpClassRan = true
      }
    }
  }

  @Fixture
  @ExtendWith(RobolectricExtension::class)
  class OnTwoSdks {
    @RobolectricSdkTest
    @Config(sdk = [33, 34])
    fun runsOnBoth() {
      Fixtures.record(Build.VERSION.SDK_INT.toString())
    }
  }

  @Fixture
  @ExtendWith(RobolectricExtension::class)
  @Config(sdk = [34])
  class InASharedEnvironment {
    @RobolectricSdkTest
    @Config(sdk = [33, 34])
    fun runsOnBoth() {
      Fixtures.record("${Build.VERSION.SDK_INT} shared=$setUpClassRan")
    }

    companion object {
      private var setUpClassRan = false

      @JvmStatic
      @BeforeAll
      fun setUpClass() {
        setUpClassRan = true
      }
    }
  }
}
