package org.robolectric.runner.common

import com.google.common.truth.Truth.assertThat
import java.util.Properties
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import org.robolectric.annotation.LooperMode
import org.robolectric.annotation.ResourcesMode
import org.robolectric.annotation.SQLiteMode
import org.robolectric.config.AndroidConfigurer
import org.robolectric.interceptors.AndroidInterceptors
import org.robolectric.internal.SandboxManager
import org.robolectric.internal.bytecode.InstrumentationConfiguration
import org.robolectric.internal.bytecode.Interceptors
import org.robolectric.internal.bytecode.SandboxConfig
import org.robolectric.plugins.SdkCollection
import org.robolectric.runner.common.internal.Injectors
import org.robolectric.runner.common.internal.SandboxIdentity
import org.robolectric.runner.common.internal.SharedSandboxManager

@OptIn(ExperimentalRunnerApi::class)
class RobolectricSandboxTest {
  @Test
  fun `a lease keeps its sandbox for the environments that are opened in it`() {
    val configuration = session.plan(SDK_34).single()

    session.lease(configuration).use { sandbox ->
      val first = sandbox.open(configuration).use { identityAndLoader(it) }
      val second = sandbox.open(configuration).use { identityAndLoader(it) }

      assertThat(first.second).isSameInstanceAs(sandbox.classLoader)
      assertThat(second.second).isSameInstanceAs(sandbox.classLoader)
      assertThat(second.first).isNotEqualTo(first.first)
    }
  }

  @Test
  fun `a lease runs code on the main thread of its sandbox with its class loader`() {
    session.lease(session.plan(SDK_34).single()).use { sandbox ->
      val (name, loader) =
        sandbox.run {
          Thread.currentThread().name to Thread.currentThread().contextClassLoader
        }
      val nested = sandbox.run { sandbox.run { Thread.currentThread().name } }

      assertThat(name).isEqualTo("SDK 34 Main Thread")
      assertThat(nested).isEqualTo(name)
      assertThat(loader).isSameInstanceAs(sandbox.classLoader)
      assertThat(sandbox.loadClass(Int::class.javaPrimitiveType!!))
        .isSameInstanceAs(Int::class.javaPrimitiveType)
      assertThat(sandbox.loadClass(AndroidProbe::class.java).classLoader)
        .isSameInstanceAs(sandbox.classLoader)
    }
  }

  @Test
  fun `prepare configures the sandbox before an application is set up`() {
    for (mode in listOf(GraphicsMode.Mode.NATIVE, GraphicsMode.Mode.LEGACY)) {
      val configuration = session.plan(SDK_34, mode).single()
      session.lease(configuration).use { sandbox ->
        sandbox.prepare(configuration)

        assertThat(readGraphicsMode(sandbox)).isEqualTo(mode.name)
      }
    }
  }

  @Test
  fun `a lease rejects a configuration of another sandbox`() {
    val sdk34 = session.plan(SDK_34).single()
    val sdk33 = session.plan(Config.Builder().setSdk(33).build()).single()

    session.lease(sdk34).use { sandbox ->
      assertThrows<IllegalArgumentException> { sandbox.open(sdk33) }
      assertThrows<IllegalArgumentException> { sandbox.prepare(sdk33) }
    }
  }

  @Test
  fun `a lease has one environment open at a time`() {
    val configuration = session.plan(SDK_34).single()

    session.lease(configuration).use { sandbox ->
      sandbox.open(configuration).use {
        assertThrows<IllegalStateException> { sandbox.open(configuration) }
        assertThrows<IllegalStateException> { sandbox.prepare(configuration) }
      }
      sandbox.open(configuration).close()
    }
  }

  @Test
  fun `closing a lease closes its environment`() {
    val configuration = session.plan(SDK_34).single()
    val sandbox = session.lease(configuration)
    val environment = sandbox.open(configuration)

    sandbox.close()
    sandbox.close()

    assertThrows<IllegalStateException> { environment.run { 0 } }
    assertThrows<IllegalStateException> { sandbox.run { 0 } }
    assertThrows<IllegalStateException> { sandbox.open(configuration) }
  }

  @Test
  fun `configurations that differ only in what an application gets have the same key`() {
    val plain = session.sandboxKey(session.plan(Sdk34::class.java, null).single())
    val landscape =
      session.sandboxKey(
        session.plan(Sdk34::class.java, Sdk34::class.java.getMethod("land")).single(),
      )
    val sdk33 = session.sandboxKey(session.plan(Config.Builder().setSdk(33).build()).single())

    assertThat(landscape).isEqualTo(plain)
    assertThat(landscape.id).isEqualTo(plain.id)
    assertThat(plain.sdk).isEqualTo(34)
    assertThat(plain.id).startsWith("34-")
    assertThat(sdk33).isNotEqualTo(plain)
    assertThat(sdk33.id).startsWith("33-")
  }

  @Test
  fun `a key's ID is the same in another session`() {
    val configuration = session.plan(SDK_34, GraphicsMode.Mode.NATIVE).single()

    RobolectricSession.builder().properties(testProperties()).build().use { other ->
      assertThat(other.sandboxKey(other.plan(SDK_34, GraphicsMode.Mode.NATIVE).single()).id)
        .isEqualTo(session.sandboxKey(configuration).id)
    }
    assertThat(session.sandboxKey(session.plan(SDK_34, GraphicsMode.Mode.LEGACY).single()).id)
      .isNotEqualTo(session.sandboxKey(configuration).id)
  }

  @Test
  fun `sandbox config instruments packages like the test runner does`() {
    val plain = session.plan(Sdk34::class.java, null).single()
    val classConfigured = session.plan(Instrumenting::class.java, null).single()
    val methodConfigured =
      session.plan(Instrumenting::class.java, Instrumenting::class.java.getMethod("more")).single()

    assertThat(classConfigured).isNotEqualTo(plain)
    assertThat(session.sandboxKey(classConfigured)).isNotEqualTo(session.sandboxKey(plain))
    assertThat(session.sandboxKey(classConfigured).id).isNotEqualTo(session.sandboxKey(plain).id)
    assertThat(instrumentedPackages(session.sandboxKey(classConfigured)))
      .contains("org.robolectric.runner.common.shared.")
    assertThat(instrumentedPackages(session.sandboxKey(methodConfigured)))
      .containsAtLeast(
        "org.robolectric.runner.common.shared.",
        "org.robolectric.runner.common.more.",
      )
  }

  @Test
  fun `sessions that share sandboxes lease the same sandbox`() {
    sharingSession().use { first ->
      sharingSession().use { second ->
        val firstLease = first.lease(first.plan(SDK_34).single())
        val secondLease = second.lease(second.plan(SDK_34).single())

        assertThat(secondLease.classLoader).isSameInstanceAs(firstLease.classLoader)
        firstLease.close()
        secondLease.close()
      }
    }
  }

  @Test
  fun `the test runner's shared sandbox manager gives it the sandboxes of sessions`() {
    val injector = Injectors.create(testProperties())
    val manager = injector.getInstance(SharedSandboxManager::class.java)
    assertThat(manager).isInstanceOf(SandboxManager::class.java)
    val sdk = injector.getInstance(SdkCollection::class.java).getSdk(34)

    sharingSession().use { sharing ->
      val configuration = sharing.plan(Sdk34::class.java, null).single()
      sharing.lease(configuration).use { sandbox ->
        val runnerSandbox =
          manager.getAndroidSandbox(
            runnerInstrumentation(injector.getInstance(AndroidConfigurer::class.java)),
            sdk,
            configuration.get(ResourcesMode.Mode::class.java),
            configuration.get(LooperMode.Mode::class.java),
            configuration.get(SQLiteMode.Mode::class.java),
            configuration.get(GraphicsMode.Mode::class.java),
          )

        assertThat(runnerSandbox.robolectricClassLoader).isSameInstanceAs(sandbox.classLoader)
      }
    }
  }

  @Test
  fun `sandboxes can't be shared with packages or a classpath of the session's own`() {
    assertThrows<IllegalStateException> {
      RobolectricSession.builder()
        .properties(testProperties())
        .shareSandboxes()
        .sharePackage("org.robolectric.runner.common.shared.")
        .build()
    }
  }

  @Test
  fun `closing a session gives back its leases`() {
    val ownSession = RobolectricSession.builder().properties(testProperties()).build()
    val configuration = ownSession.plan(SDK_34).single()
    val sandbox = ownSession.lease(configuration)
    val environment = sandbox.open(configuration)

    ownSession.close()

    assertThrows<IllegalStateException> { environment.run { 0 } }
    assertThrows<IllegalStateException> { sandbox.run { 0 } }
  }

  @Config(sdk = [34])
  class Sdk34 {
    @Config(qualifiers = "land") fun land() = Unit
  }

  @Config(sdk = [34])
  @SandboxConfig(instrumentedPackages = ["org.robolectric.runner.common.shared."])
  class Instrumenting {
    @SandboxConfig(instrumentedPackages = ["org.robolectric.runner.common.more."]) fun more() = Unit
  }

  companion object {
    private lateinit var session: RobolectricSession
    private val SDK_34 = Config.Builder().setSdk(34).build()

    @JvmStatic
    @BeforeAll
    fun createSession() {
      session = RobolectricSession.builder().properties(testProperties()).build()
    }

    @JvmStatic
    @AfterAll
    fun closeSession() {
      session.close()
    }

    private fun sharingSession(properties: Properties = testProperties()) =
      RobolectricSession.builder().properties(properties).shareSandboxes().build()

    private fun identityAndLoader(environment: RobolectricEnvironment): Pair<Int, ClassLoader> =
      AndroidProbe.read<Int>(environment, "applicationIdentity") to environment.classLoader

    private fun readGraphicsMode(sandbox: RobolectricSandbox): String = sandbox.run {
      val probeClass = sandbox.loadClass(AndroidProbe::class.java)
      probeClass.getMethod("graphicsMode").invoke(probeClass.getDeclaredConstructor().newInstance())
        as String
    }

    private fun instrumentedPackages(key: SandboxKey): Collection<String> =
      InstrumentationConfiguration.Builder((key.identity as SandboxIdentity).instrumentation)
        .instrumentedPackages

    /** Returns the instrumentation that `RobolectricTestRunner` creates for a test of [Sdk34]. */
    private fun runnerInstrumentation(configurer: AndroidConfigurer): InstrumentationConfiguration {
      val builder = InstrumentationConfiguration.newBuilder()
      configurer.configure(builder, Interceptors(AndroidInterceptors.all()))
      configurer.withConfig(builder, Config.Builder().setSdk(34).build())
      return builder.build()
    }
  }
}
