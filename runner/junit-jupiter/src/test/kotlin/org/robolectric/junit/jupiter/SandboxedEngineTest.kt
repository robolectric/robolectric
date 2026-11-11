package org.robolectric.junit.jupiter

import android.app.Application
import android.content.SharedPreferences
import android.os.Build
import com.google.common.truth.Truth.assertThat
import java.util.concurrent.Callable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.coroutines.withContext
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.AfterEachCallback
import org.junit.jupiter.api.extension.BeforeEachCallback
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.junit.platform.engine.discovery.DiscoverySelectors
import org.junit.rules.ExternalResource
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.junit.jupiter.MockitoExtension
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import org.robolectric.config.ConfigurationRegistry

/** Regressions for extensions and class-level code that must execute inside the sandbox. */
class SandboxedEngineTest {
  @Test
  fun `excluding the engine fails rather than silently dropping Robolectric tests`() {
    val results = Fixtures.run(SharingFixture::class, engines = listOf("junit-jupiter"))
    assertThat(results.failed).isNotEmpty()
    assertThat(results.failed.values.first()).hasMessageThat().contains("robolectric")
  }

  @Test
  fun `a unique id reruns only the selected method`() {
    val first = Fixtures.run(SelectionFixture::class)
    assertThat(first.failed).isEmpty()
    val id = first.identifiers.getValue("first()")
    val selected = Fixtures.run(selectors = listOf(DiscoverySelectors.selectUniqueId(id)))
    assertThat(selected.failed).isEmpty()
    assertThat(selected.succeeded).containsExactly("first()")
    assertThat(selected.events).containsExactly("first 34")
  }

  @Test
  fun `a unique id reruns only the selected parameterized invocation`() {
    val first = Fixtures.run(ParameterizedFixture::class)
    assertThat(first.failed).isEmpty()
    assertThat(first.succeeded).containsExactly("\"first\"", "\"second\"")
    val id = first.identifiers.getValue("\"first\"")
    val selected = Fixtures.run(selectors = listOf(DiscoverySelectors.selectUniqueId(id)))
    assertThat(selected.failed).isEmpty()
    assertThat(selected.succeeded).containsExactly("\"first\"")
    assertThat(selected.events).containsExactly("first")
  }

  @Test
  fun `Mockito extensions can inject Android and JDK mocks`() {
    val results = Fixtures.run(MockitoFixture::class)
    assertThat(results.failed).isEmpty()
    assertThat(results.succeeded).hasSize(1)
  }

  @Test
  fun `third party extensions set the dispatcher used by the test`() {
    val results = Fixtures.run(MainDispatcherFixture::class)
    assertThat(results.failed).isEmpty()
    assertThat(results.events).containsExactly("main dispatcher used")
  }

  @Test
  fun `companion initializers and class callbacks run inside a configured sandbox`() {
    val results = Fixtures.run(ClassCallbacksFixture::class)
    results.failed.values.firstOrNull()?.let { throw it }
    assertThat(results.failed).isEmpty()
    assertThat(results.events)
      .containsExactly("beforeAll", "application created", "test", "afterAll")
      .inOrder()
  }

  @Test
  fun `JUnit 4 and Jupiter share a class loader without changing class lifecycle timing`() {
    val results = Fixtures.run(LegacyFixture::class, SharingFixture::class)
    results.failed.values.firstOrNull()?.let { throw it }
    assertThat(results.failed).isEmpty()
    assertThat(results.succeeded).hasSize(2)
    val loaders = results.events.filter { it.startsWith("loader ") }
    assertThat(loaders).hasSize(2)
    assertThat(loaders.distinct()).hasSize(1)
    assertThat(results.events)
      .containsAtLeast("classRule before", "beforeClass", "afterClass", "classRule after")
      .inOrder()
  }

  @RunWith(RobolectricTestRunner::class)
  @Config(sdk = [34])
  @GraphicsMode(GraphicsMode.Mode.NATIVE)
  class LegacyFixture {
    @org.junit.Test
    fun usesAndroid() {
      assertThat(RuntimeEnvironment.getApplication()).isNotNull()
      Fixtures.record("loader ${System.identityHashCode(Build::class.java.classLoader)}")
    }

    companion object {
      @JvmField
      @org.junit.ClassRule
      val rule =
        object : ExternalResource() {
          override fun before() {
            org.junit.Assume.assumeTrue(java.lang.Boolean.getBoolean(Fixtures.PROPERTY))
            assertThat(ConfigurationRegistry.get(Config::class.java).sdk).isEqualTo(intArrayOf(34))
            Fixtures.record("classRule before")
          }

          override fun after() {
            Fixtures.record("classRule after")
          }
        }

      @JvmStatic
      @org.junit.BeforeClass
      fun beforeClass() {
        Fixtures.record("beforeClass")
      }

      @JvmStatic
      @org.junit.AfterClass
      fun afterClass() {
        Fixtures.record("afterClass")
      }
    }
  }

  @Fixture
  @ExtendWith(RobolectricExtension::class)
  @Config(sdk = [34])
  class SharingFixture {
    @Test
    fun usesSameSandbox() {
      Fixtures.record("loader ${System.identityHashCode(Build::class.java.classLoader)}")
    }
  }

  @Fixture
  @ExtendWith(RobolectricExtension::class)
  @Config(sdk = [34])
  class SelectionFixture {
    @Test
    fun first() {
      Fixtures.record("first ${Build.VERSION.SDK_INT}")
    }

    @Test
    fun second() {
      Fixtures.record("second ${Build.VERSION.SDK_INT}")
    }
  }

  @Fixture
  @ExtendWith(RobolectricExtension::class)
  @Config(sdk = [34])
  class ParameterizedFixture {
    @ParameterizedTest(name = "{0}")
    @ValueSource(strings = ["first", "second"])
    fun parameter(value: String) {
      Fixtures.record(value)
    }
  }

  @Fixture
  @ExtendWith(RobolectricExtension::class, MockitoExtension::class)
  @Config(sdk = [34])
  class MockitoFixture {
    @Mock lateinit var preferences: SharedPreferences
    @Mock lateinit var callable: Callable<String>

    @Test
    fun usesMocks() {
      `when`(preferences.getString("name", "world")).thenReturn("Robolectric")
      `when`(callable.call()).thenReturn("Robolectric")
      assertThat(preferences.getString("name", "world")).isEqualTo("Robolectric")
      assertThat(callable.call()).isEqualTo("Robolectric")
    }
  }

  @OptIn(ExperimentalCoroutinesApi::class)
  class MainDispatcherExtension : BeforeEachCallback, AfterEachCallback {
    override fun beforeEach(context: ExtensionContext) {
      Dispatchers.setMain(StandardTestDispatcher())
    }

    override fun afterEach(context: ExtensionContext) {
      Dispatchers.resetMain()
    }
  }

  @Fixture
  @ExtendWith(RobolectricExtension::class, MainDispatcherExtension::class)
  @Config(sdk = [34])
  class MainDispatcherFixture {
    @Test
    fun usesExtensionDispatcher() = runTest {
      withContext(Dispatchers.Main) { Fixtures.record("main dispatcher used") }
    }
  }

  @Fixture
  @ExtendWith(RobolectricExtension::class)
  @Config(sdk = [34], qualifiers = "land", application = LifecycleApplication::class)
  class ClassCallbacksFixture {
    @Test
    fun applicationIsReadyForTheTest() {
      assertThat(RuntimeEnvironment.getApplication()).isNotNull()
      assertThat(companionSdk).isEqualTo(34)
      Fixtures.record("test")
    }

    companion object {
      private val companionSdk = Build.VERSION.SDK_INT

      @JvmStatic
      @BeforeAll
      fun beforeAll() {
        assertThat(companionSdk).isEqualTo(34)
        assertThat(Thread.currentThread().name).isEqualTo("SDK 34 Main Thread")
        assertThat(ConfigurationRegistry.get(Config::class.java).qualifiers).isEqualTo("land")
        Fixtures.record("beforeAll")
      }

      @JvmStatic
      @AfterAll
      fun afterAll() {
        assertThat(Thread.currentThread().name).isEqualTo("SDK 34 Main Thread")
        Fixtures.record("afterAll")
      }
    }
  }

  class LifecycleApplication : Application() {
    override fun onCreate() {
      super.onCreate()
      Fixtures.record("application created")
    }
  }
}
