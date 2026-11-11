@file:OptIn(org.robolectric.runner.common.ExperimentalRunnerApi::class)

package org.robolectric.junit.jupiter

import android.os.Build
import com.google.common.truth.Truth.assertThat
import com.google.common.truth.Truth.assertWithMessage
import java.io.File
import java.lang.management.ManagementFactory
import java.nio.file.Paths
import java.util.concurrent.TimeUnit
import kotlin.system.exitProcess
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.extension.ExtendWith
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import org.robolectric.annotation.LooperMode
import org.robolectric.annotation.SQLiteMode
import org.robolectric.config.ConfigurationRegistry
import org.robolectric.junit.platform.ActiveSandbox
import org.robolectric.junit.platform.ActiveSandboxes
import org.robolectric.junit.platform.ProcessSandbox

/** Tests the single-sandbox and modern-mode requirements of the Platform engine. */
class SingleSandboxTest {
  @Test
  fun `multiple selected SDKs fail before running test code`() {
    val results = Fixtures.run(TwoSdks::class)

    assertThat(results.failed.values.single())
      .hasMessageThat()
      .contains("Select one SDK with robolectric.enabledSdks")
    assertThat(results.events).isEmpty()
  }

  @Test
  fun `one enabled SDK runs with modern defaults`() {
    val results =
      Fixtures.run(TwoSdks::class, properties = mapOf("robolectric.enabledSdks" to "34"))

    assertThat(results.failed).isEmpty()
    assertThat(results.events).containsExactly("34 NATIVE NATIVE PAUSED")
  }

  @Test
  fun `the sandbox survives launcher executions`() {
    val first = Fixtures.run(Sdk34::class)
    val second = Fixtures.run(Sdk34::class)

    assertThat(first.failed).isEmpty()
    assertThat(second.failed).isEmpty()
    assertThat(first.events).hasSize(1)
    assertThat(second.events).isEqualTo(first.events)
  }

  @Test
  fun `variant markers name the SDK of the JVM`() {
    val properties = mapOf("robolectric.alwaysIncludeVariantMarkersInTestName" to "true")

    assertThat(Fixtures.run(Sdk34::class, properties = properties).succeeded)
      .containsExactly("runs()[34]")
  }

  @Test
  fun `another SDK cannot create a second sandbox in this JVM`() {
    assertThat(Fixtures.run(Sdk34::class).failed).isEmpty()
    val results = Fixtures.run(Sdk33::class)

    assertThat(results.failed.values.single()).hasMessageThat().contains("one sandbox per JVM")
    assertThat(results.events).isEmpty()
  }

  @Test
  fun `different instrumentation cannot create a second sandbox`() {
    assertThat(Fixtures.run(Sdk34::class).failed).isEmpty()
    val results = Fixtures.run(DifferentInstrumentation::class)

    assertThat(results.failed.values.single()).hasMessageThat().contains("one sandbox per JVM")
    assertThat(results.events).isEmpty()
  }

  @Test
  fun `legacy modes and SDKs without native graphics are rejected`() {
    for ((fixture, reason) in
      listOf(
        LegacyGraphics::class to "GraphicsMode.NATIVE",
        LegacySqlite::class to "SQLiteMode.NATIVE",
        LegacyLooper::class to "LooperMode.PAUSED or INSTRUMENTATION_TEST",
        OldSdk::class to "SDK 26 or newer",
      )) {
      val results = Fixtures.run(fixture)
      assertThat(results.failed.values.single()).hasMessageThat().contains(reason)
      assertThat(results.events).isEmpty()
    }
  }

  @Test
  fun `a legacy class configuration fails even if its tests override it`() {
    val results = Fixtures.run(LegacyClassSqlite::class)

    assertThat(results.failed.values.single()).hasMessageThat().contains("SQLiteMode.NATIVE")
    assertThat(results.events).isEmpty()
  }

  @Test
  fun `disabled tests with rejected configurations are skipped`() {
    val results = Fixtures.run(DisabledLegacyTest::class, DisabledLegacyClass::class)

    assertThat(results.failed).isEmpty()
    assertThat(results.skipped).hasSize(2)
    assertThat(results.events).isEmpty()
  }

  @Test
  fun `a concurrent launcher fails before preparing the sandbox`() {
    assertThat(Fixtures.run(Sdk34::class).failed).isEmpty()
    val configuration =
      ProcessSandbox.createSession().plan(Config.Builder().setSdk(34).build()).single()
    val sandbox = ProcessSandbox.acquire(configuration)
    val other = ActiveSandbox(sandbox, emptyMap(), configuration)
    ActiveSandboxes.register(other)
    try {
      sandbox.open(configuration).use {
        val results = Fixtures.run(Sdk34::class)

        assertThat(results.failed.values.single()).hasMessageThat().contains("Concurrent")
        assertThat(results.events).isEmpty()
      }
    } finally {
      ActiveSandboxes.remove(other)
    }
  }

  @Test
  fun `another SDK and instrumentation looper work in separate JVMs`() {
    for (mode in listOf("sdk33", "instrumentation")) {
      val java = Paths.get(System.getProperty("java.home"), "bin", "java").toString()
      val flags =
        ManagementFactory.getRuntimeMXBean().inputArguments.filter {
          it.startsWith("--add-") || it.startsWith("--enable-")
        }
      val command =
        listOf(java) +
          flags +
          listOf("-cp", System.getProperty("java.class.path"), ProcessProbe::class.java.name, mode)
      val log = File("build/single-sandbox-$mode.log")
      val process = ProcessBuilder(command).redirectErrorStream(true).redirectOutput(log).start()
      try {
        check(process.waitFor(2, TimeUnit.MINUTES)) {
          "The $mode JVM did not exit: ${log.readText()}"
        }
        assertWithMessage(log.readText()).that(process.exitValue()).isEqualTo(0)
      } finally {
        process.destroyForcibly()
      }
    }
  }

  @Fixture
  @ExtendWith(RobolectricExtension::class)
  @Config(sdk = [33, 34])
  class TwoSdks {
    @Test
    fun modes() {
      recordModes()
    }
  }

  @Fixture
  @ExtendWith(RobolectricExtension::class)
  @Config(sdk = [34])
  open class Sdk34 {
    @Test
    fun runs() {
      Fixtures.record("loader ${System.identityHashCode(Build::class.java.classLoader)}")
    }
  }

  @Fixture @Config(sdk = [33]) class Sdk33 : Sdk34()

  @Fixture
  @Config(sdk = [34], instrumentedPackages = ["example.extra"])
  class DifferentInstrumentation : Sdk34()

  @Fixture @GraphicsMode(GraphicsMode.Mode.LEGACY) class LegacyGraphics : Sdk34()

  @Fixture @SQLiteMode(SQLiteMode.Mode.LEGACY) class LegacySqlite : Sdk34()

  @Fixture @LooperMode(LooperMode.Mode.LEGACY) class LegacyLooper : Sdk34()

  @Fixture @Config(sdk = [24]) class OldSdk : Sdk34()

  @Fixture
  @ExtendWith(RobolectricExtension::class)
  @Config(sdk = [34])
  @SQLiteMode(SQLiteMode.Mode.LEGACY)
  @TestInstance(TestInstance.Lifecycle.PER_CLASS)
  class LegacyClassSqlite {
    @Test
    @SQLiteMode(SQLiteMode.Mode.NATIVE)
    fun runs() {
      Fixtures.record("ran")
    }
  }

  @Fixture
  @ExtendWith(RobolectricExtension::class)
  @Config(sdk = [34])
  class DisabledLegacyTest {
    @Test
    @Disabled
    @GraphicsMode(GraphicsMode.Mode.LEGACY)
    fun disabled() {
      Fixtures.record("ran")
    }
  }

  @Fixture @Disabled @LooperMode(LooperMode.Mode.LEGACY) class DisabledLegacyClass : Sdk34()

  @Fixture
  @ExtendWith(RobolectricExtension::class)
  @Config(sdk = [34])
  @LooperMode(LooperMode.Mode.INSTRUMENTATION_TEST)
  class InstrumentationLooper {
    @Test
    fun modes() {
      recordModes()
    }
  }

  object ProcessProbe {
    @JvmStatic
    fun main(args: Array<String>) {
      val fixture = if (args.single() == "sdk33") Sdk33::class else InstrumentationLooper::class
      val results = Fixtures.run(fixture)
      val expected =
        if (fixture == Sdk33::class) results.events.size == 1
        else results.events == listOf("34 NATIVE NATIVE INSTRUMENTATION_TEST")
      val passed = results.failed.isEmpty() && results.succeeded.size == 1 && expected
      if (!passed) System.err.println("${results.failed} ${results.succeeded} ${results.events}")
      // Exits explicitly because Android leaves non-daemon threads running.
      exitProcess(if (passed) 0 else 1)
    }
  }

  companion object {
    private fun recordModes() {
      Fixtures.record(
        "${Build.VERSION.SDK_INT} " +
          "${ConfigurationRegistry.get(GraphicsMode.Mode::class.java)} " +
          "${ConfigurationRegistry.get(SQLiteMode.Mode::class.java)} " +
          "${ConfigurationRegistry.get(LooperMode.Mode::class.java)}",
      )
    }
  }
}
