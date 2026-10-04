package org.robolectric.junit.jupiter

import android.content.Context
import android.os.Build
import com.google.common.truth.Truth.assertThat
import java.io.Serializable
import java.nio.file.Files
import java.nio.file.Path
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.MethodOrderer
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestMethodOrder
import org.junit.jupiter.api.extension.BeforeEachCallback
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.api.extension.RegisterExtension
import org.junit.jupiter.api.extension.TestInstancePostProcessor
import org.junit.jupiter.api.io.TempDir
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

/** Tests that what JUnit and its extensions inject into a test instance reaches the test. */
@ExtendWith(RobolectricExtension::class, InjectedFieldsTest.CounterInjector::class)
@Config(sdk = [34])
class InjectedFieldsTest {
  @TempDir lateinit var directory: Path

  lateinit var counter: Counter

  private var directoryAtSetUp: Path? = null

  @BeforeEach
  fun setUp() {
    directoryAtSetUp = directory
    counter.count++
  }

  @Test
  fun `an injected object that is recreated in the Android environment is recreated once`() {
    assertThat(counter.javaClass.classLoader).isSameInstanceAs(javaClass.classLoader)
    assertThat(counter.count).isEqualTo(1)
  }

  @Test
  fun `a field that JUnit injects is set in the Android environment`() {
    assertThat(Files.isDirectory(directory)).isTrue()
    assertThat(directoryAtSetUp).isEqualTo(directory)
    assertThat(Build.VERSION.SDK_INT).isEqualTo(34)
  }

  @Nested
  inner class Inner {
    @Test
    fun `a field of the enclosing instance is set for a nested test`() {
      assertThat(Files.isDirectory(directory)).isTrue()
    }
  }

  /** An object of a class that the sandbox loads again, and recreates by serializing it. */
  class Counter : Serializable {
    var count = 0

    private companion object {
      const val serialVersionUID = 1L
    }
  }

  class CounterInjector : TestInstancePostProcessor {
    override fun postProcessTestInstance(testInstance: Any, context: ExtensionContext) {
      (testInstance as? InjectedFieldsTest)?.counter = Counter()
    }
  }

  /** Tests that an instance that lives for all tests of a class gets what each test injects. */
  @Nested
  @TestInstance(TestInstance.Lifecycle.PER_CLASS)
  @TestMethodOrder(MethodOrderer.OrderAnnotation::class)
  inner class OneInstancePerClass {
    @TempDir lateinit var ownDirectory: Path

    private var directoryOfFirstTest: Path? = null

    @Test
    @Order(1)
    fun `the first test gets its directory`() {
      assertThat(Files.isDirectory(ownDirectory)).isTrue()
      directoryOfFirstTest = ownDirectory
    }

    @Test
    @Order(2)
    fun `the second test gets another directory`() {
      assertThat(Files.isDirectory(ownDirectory)).isTrue()
      assertThat(ownDirectory).isNotEqualTo(directoryOfFirstTest)
    }
  }
}

/** Tests construction and extension injection inside the Android environment. */
class ConstructedByJUnitTest {
  @Test
  fun `an extension registered from an instance field still runs`() {
    val results = Fixtures.run(WithInstanceExtension::class)

    assertThat(results.failed).isEmpty()
    assertThat(results.events).containsExactly("extension beforeEach", "test on 34").inOrder()
  }

  @Test
  fun `a constructor with instance extensions can take an Android parameter`() {
    val results = Fixtures.run(WithInstanceExtensionAndContext::class)

    assertThat(results.failed).isEmpty()
    assertThat(results.succeeded).containsExactly("test()")
  }

  @Test
  fun `a static initializer can use Android`() {
    val results = Fixtures.run(WithStaticInitializer::class)

    assertThat(results.failed).isEmpty()
    assertThat(results.succeeded).containsExactly("test()")
  }

  @Test
  fun `an injected app object stays in the Android environment`() {
    val results = Fixtures.run(WithUnusableInjection::class)

    assertThat(results.failed).isEmpty()
    assertThat(results.succeeded).containsExactly("test()")
  }

  class RecordingExtension : BeforeEachCallback {
    override fun beforeEach(context: ExtensionContext) = Fixtures.record("extension beforeEach")
  }

  @Fixture
  @ExtendWith(RobolectricExtension::class)
  @Config(sdk = [34])
  class WithInstanceExtension {
    @JvmField @RegisterExtension val extension = RecordingExtension()

    @TempDir lateinit var directory: Path

    private val own = NotSerializable()

    @Test
    fun test() {
      assertThat(Files.isDirectory(directory)).isTrue()
      assertThat(own.javaClass.classLoader).isSameInstanceAs(javaClass.classLoader)
      Fixtures.record("test on ${Build.VERSION.SDK_INT}")
    }
  }

  @Fixture
  @ExtendWith(RobolectricExtension::class)
  @Config(sdk = [34])
  class WithInstanceExtensionAndContext(private val context: Context) {
    @JvmField @RegisterExtension val extension = RecordingExtension()

    @Test
    fun test() {
      assertThat(context).isSameInstanceAs(RuntimeEnvironment.getApplication())
    }
  }

  @Fixture
  @ExtendWith(RobolectricExtension::class)
  @Config(sdk = [34])
  class WithStaticInitializer {
    @Test
    fun test() {
      assertThat(sdkInt).isEqualTo(34)
    }

    companion object {
      private val sdkInt = Build.VERSION.SDK_INT.also { check(it > 0) { "There is no Android" } }
    }
  }

  /** An app object that cannot be serialized. */
  class NotSerializable

  class Injector : TestInstancePostProcessor {
    override fun postProcessTestInstance(testInstance: Any, context: ExtensionContext) {
      (testInstance as WithUnusableInjection).injected = NotSerializable()
    }
  }

  @Fixture
  @ExtendWith(RobolectricExtension::class, Injector::class)
  @Config(sdk = [34])
  class WithUnusableInjection {
    var injected: NotSerializable? = null

    @Test
    fun test() {
      assertThat(injected).isNotNull()
      assertThat(injected!!.javaClass.classLoader).isSameInstanceAs(javaClass.classLoader)
    }
  }
}
