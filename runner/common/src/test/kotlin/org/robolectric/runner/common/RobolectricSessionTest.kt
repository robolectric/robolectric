package org.robolectric.runner.common

import com.google.common.truth.Truth.assertThat
import java.time.Duration
import java.util.Locale
import java.util.concurrent.ConcurrentLinkedQueue
import kotlin.concurrent.thread
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.robolectric.annotation.Config
import org.robolectric.pluginapi.config.ConfigurationStrategy.Configuration
import org.robolectric.plugins.HierarchicalConfigurationStrategy.ConfigurationImpl
import org.robolectric.runner.common.shared.SharedType

@OptIn(ExperimentalRunnerApi::class)
class RobolectricSessionTest {
  @Test
  fun `plan returns the SDK a class is configured with`() {
    val configurations = session.plan(Sdk34::class.java, null)

    assertThat(configurations.map { it.apiLevel }).containsExactly(34)
  }

  @Test
  fun `plan returns one environment for each SDK, lowest first`() {
    val configurations = session.plan(Sdk34::class.java, method<Sdk34>("onTwoSdks"))

    assertThat(configurations.map { it.apiLevel }).containsExactly(33, 34).inOrder()
  }

  @Test
  fun `plan for a class ignores the configuration of its methods`() {
    val configurations = session.plan(OnlyMethodsConfigured::class.java, null)

    assertThat(configurations).hasSize(1)
    assertThat(configurations.single().apiLevel).isNotEqualTo(33)
  }

  @Test
  fun `plan returns nothing when the enabled SDKs exclude those of the test`() {
    RobolectricSession.builder().properties(testProperties(enabledSdks = "33")).build().use {
      filtered ->
      assertThat(filtered.plan(Sdk34::class.java, null)).isEmpty()
      assertThat(filtered.plan(Sdk34::class.java, method<Sdk34>("onTwoSdks")).map { it.apiLevel })
        .containsExactly(33)
    }
  }

  @Test
  fun `configurations of a class and of a method that doesn't override it are equal`() {
    val classSpec = session.plan(Sdk34::class.java, null).single()
    val methodSpec = session.plan(Sdk34::class.java, method<Sdk34>("plain")).single()

    assertThat(methodSpec).isEqualTo(classSpec)
    assertThat(methodSpec.hashCode()).isEqualTo(classSpec.hashCode())
  }

  @Test
  fun `configurations are equal on the SDK they share, whichever SDKs the test selects`() {
    val classSpec = session.plan(Sdk34::class.java, null).single()
    val methodSpecs = session.plan(Sdk34::class.java, method<Sdk34>("onTwoSdks"))

    assertThat(methodSpecs.last()).isEqualTo(classSpec)
    assertThat(methodSpecs.first()).isNotEqualTo(classSpec)
  }

  @Test
  fun `configurations differ when a method changes the configuration`() {
    val classSpec = session.plan(Sdk34::class.java, null).single()
    val methodSpec = session.plan(Sdk34::class.java, method<Sdk34>("landscape")).single()

    assertThat(methodSpec).isNotEqualTo(classSpec)
  }

  @Test
  fun `a planned configuration is set up with its SDK and qualifiers`() {
    val config = Config.Builder().setSdk(34).setQualifiers("w960dp-h600dp-land").build()

    session.open(session.plan(config).single()).use { android ->
      assertThat(android.configuration.apiLevel).isEqualTo(34)
      assertThat(AndroidProbe.read<Int>(android, "sdkInt")).isEqualTo(34)
      assertThat(AndroidProbe.read<String>(android, "qualifiers")).contains("w960dp-h600dp")
    }
  }

  @Test
  fun `a configuration without an SDK is planned for the default one`() {
    val configuration = session.plan(Config.Builder().build()).single()

    session.open(configuration).use { android ->
      assertThat(AndroidProbe.read<Int>(android, "sdkInt")).isEqualTo(configuration.apiLevel)
    }
  }

  @Test
  fun `a configuration is planned for each of its SDKs, lowest first`() {
    val configurations = session.plan(Config.Builder().setSdk(34, 33).build())

    assertThat(configurations.map { it.apiLevel }).containsExactly(33, 34).inOrder()
  }

  @Test
  fun `a planned configuration is not limited to the SDKs that tests are enabled on`() {
    val properties = testProperties().apply { setProperty("robolectric.enabledSdks", "33") }

    RobolectricSession.builder().properties(properties).build().use { limited ->
      assertThat(limited.plan(Sdk34::class.java, null)).isEmpty()
      assertThat(limited.plan(SDK_34).single().apiLevel).isEqualTo(34)
    }
  }

  @Test
  fun `a configuration can't be planned for an SDK that Robolectric doesn't have`() {
    val unknown = Config.Builder().setSdk(1).build()

    val failure = assertThrows<IllegalStateException> { session.plan(unknown) }

    assertThat(failure).hasMessageThat().contains("sdk=1")
  }

  @Test
  fun `open sets up the environment the configuration describes`() {
    session.open(session.plan(Sdk34::class.java, method<Sdk34>("landscape")).single()).use {
      assertThat(it.configuration.apiLevel).isEqualTo(34)
      assertThat(AndroidProbe.read<Int>(it, "sdkInt")).isEqualTo(34)
      assertThat(AndroidProbe.read<String>(it, "qualifiers")).contains("land")
    }
  }

  @Test
  fun `open rejects a configuration that plan didn't return`() {
    assertThrows<IllegalArgumentException> { session.open(ConfigurationImpl()) }
  }

  @Test
  fun `open gives each environment its own application`() {
    val configuration = session.plan(Sdk34::class.java, null).single()

    val first =
      session.open(configuration).use { AndroidProbe.read<Int>(it, "applicationIdentity") }
    val second =
      session.open(configuration).use { AndroidProbe.read<Int>(it, "applicationIdentity") }

    assertThat(second).isNotEqualTo(first)
  }

  @Test
  fun `environments that are open together have a sandbox each`() {
    val configuration = session.plan(Sdk34::class.java, null).single()

    session.open(configuration).use { first ->
      session.open(configuration).use { second ->
        assertThat(second.classLoader).isNotSameInstanceAs(first.classLoader)
        assertThat(AndroidProbe.read<Int>(second, "applicationIdentity"))
          .isNotEqualTo(AndroidProbe.read<Int>(first, "applicationIdentity"))
      }
    }
  }

  @Test
  fun `open reuses the sandbox of an environment that was closed`() {
    val configuration = session.plan(Sdk34::class.java, null).single()

    val first = session.open(configuration).use { it.classLoader }
    val second = session.open(configuration).use { it.classLoader }

    assertThat(second).isSameInstanceAs(first)
  }

  @Test
  fun `open can be used from several threads at once`() {
    val configuration = session.plan(Sdk34::class.java, null).single()
    val sdks = ConcurrentLinkedQueue<Int>()

    val threads =
      List(3) {
        thread { session.open(configuration).use { sdks.add(AndroidProbe.read(it, "sdkInt")) } }
      }
    threads.forEach { it.join() }

    assertThat(sdks).containsExactly(34, 34, 34)
  }

  @Test
  fun `open allows environments in different sandboxes at the same time`() {
    val configurations = session.plan(Sdk34::class.java, method<Sdk34>("onTwoSdks"))

    session.open(configurations[0]).use { sdk33 ->
      session.open(configurations[1]).use { sdk34 ->
        assertThat(AndroidProbe.read<Int>(sdk33, "sdkInt")).isEqualTo(33)
        assertThat(AndroidProbe.read<Int>(sdk34, "sdkInt")).isEqualTo(34)
      }
    }
  }

  @Test
  fun `an inner class is configured like its enclosing class`() {
    val configurations = session.plan(Sdk34.Inner::class.java, method<Sdk34.Inner>("plain"))

    assertThat(configurations.map { it.apiLevel }).containsExactly(34)
    assertThat(configurations.single()).isEqualTo(session.plan(Sdk34::class.java, null).single())
  }

  @Test
  fun `an inner class and its methods can override the enclosing class`() {
    val inner = Sdk34.ConfiguredInner::class.java

    assertThat(session.plan(inner, null).map { it.apiLevel }).containsExactly(33)
    assertThat(session.plan(inner, method<Sdk34.ConfiguredInner>("onSdk34")).map { it.apiLevel })
      .containsExactly(34)
  }

  @Test
  fun `a static nested class is not configured like its enclosing class`() {
    val defaultSdk = session.plan(OnlyMethodsConfigured::class.java, null).single().apiLevel

    val configurations = session.plan(Sdk33.StaticNested::class.java, null)

    assertThat(configurations.map { it.apiLevel }).containsExactly(defaultSdk)
  }

  @Test
  fun `a shared package is not loaded again in the sandbox`() {
    val sharing =
      RobolectricSession.builder()
        .properties(testProperties())
        .sharePackage("org.robolectric.runner.common.shared.")
        .build()
    sharing.use {
      it.open(it.plan(Sdk34::class.java, null).single()).use { environment ->
        assertThat(environment.loadClass(SharedType::class.java))
          .isSameInstanceAs(SharedType::class.java)
        assertThat(environment.loadClass(AndroidProbe::class.java))
          .isNotSameInstanceAs(AndroidProbe::class.java)
      }
    }
  }

  @Test
  fun `the listener is told when environments open and close`() {
    val events = mutableListOf<String>()
    val listener =
      object : RobolectricSessionListener {
        override fun environmentOpened(
          configuration: Configuration,
          sandboxCreated: Boolean,
          duration: Duration,
        ) {
          events.add("opened sdk=${configuration.apiLevel} sandboxCreated=$sandboxCreated")
          assertThat(duration).isGreaterThan(Duration.ZERO)
        }

        override fun environmentClosed(configuration: Configuration, duration: Duration) {
          events.add("closed sdk=${configuration.apiLevel}")
        }
      }
    RobolectricSession.builder().properties(testProperties()).listener(listener).build().use {
      val configuration = it.plan(Sdk34::class.java, null).single()
      it.open(configuration).close()
      it.open(configuration).close()
    }

    assertThat(events)
      .containsExactly(
        "opened sdk=34 sandboxCreated=true",
        "closed sdk=34",
        "opened sdk=34 sandboxCreated=false",
        "closed sdk=34",
      )
      .inOrder()
  }

  @Test
  fun `environments that overlap keep their locale, and leave the one of the JVM as it was`() {
    val localeOfTheJvm = Locale.getDefault()
    RobolectricSession.builder().properties(testProperties()).build().use { ownSession ->
      val french =
        ownSession.open(ownSession.plan(Sdk34::class.java, method<Sdk34>("french")).single())
      // The sandbox of this one is created while the JVM's default locale is French.
      val english = ownSession.open(ownSession.plan(Sdk33::class.java, null).single())
      assertThat(Locale.getDefault().language).isEqualTo("en")

      english.close()
      assertThat(Locale.getDefault().language).isEqualTo("fr")

      french.close()
      assertThat(Locale.getDefault()).isEqualTo(localeOfTheJvm)
      ownSession.open(ownSession.plan(Sdk33::class.java, null).single()).close()
    }

    assertThat(Locale.getDefault()).isEqualTo(localeOfTheJvm)
  }

  @Test
  fun `close closes the environments that are still open`() {
    val ownSession = RobolectricSession.builder().properties(testProperties()).build()
    val environment = ownSession.open(ownSession.plan(Sdk34::class.java, null).single())

    ownSession.close()
    ownSession.close()

    assertThrows<IllegalStateException> { environment.run { 0 } }
    assertThrows<IllegalStateException> { ownSession.plan(Sdk34::class.java, null) }
  }

  @Config(sdk = [34])
  class Sdk34 {
    fun plain() = Unit

    @Config(sdk = [33, 34]) fun onTwoSdks() = Unit

    @Config(qualifiers = "land") fun landscape() = Unit

    @Config(qualifiers = "fr") fun french() = Unit

    inner class Inner {
      fun plain() = Unit
    }

    @Config(sdk = [33])
    inner class ConfiguredInner {
      @Config(sdk = [34]) fun onSdk34() = Unit
    }
  }

  @Config(sdk = [33])
  class Sdk33 {
    class StaticNested
  }

  class OnlyMethodsConfigured {
    @Config(sdk = [33]) fun first() = Unit

    @Config(sdk = [33]) fun second() = Unit
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

    private inline fun <reified T> method(name: String) = T::class.java.getMethod(name)
  }
}
