package org.robolectric.junit.jupiter

import android.app.Application
import android.content.Context
import android.os.Build
import android.os.Looper
import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.MethodOrderer
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.RepeatedTest
import org.junit.jupiter.api.RepetitionInfo
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInfo
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestMethodOrder
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import org.junit.jupiter.params.provider.ValueSource
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

/**
 * Tests that JUnit's lifecycle and arguments reach the test instance in the Android environment.
 */
@ExtendWith(RobolectricExtension::class)
@Config(sdk = [34])
class RobolectricExtensionLifecycleTest {
  private var testNameAtSetUp: String? = null

  @BeforeEach
  fun setUp(testInfo: TestInfo) {
    testNameAtSetUp = testInfo.testMethod.get().name
  }

  @Test
  fun `lifecycle methods get the arguments JUnit resolves`() {
    assertThat(testNameAtSetUp).isEqualTo("lifecycle methods get the arguments JUnit resolves")
  }

  @Test
  fun `tests run on Android's main thread`() {
    assertThat(Looper.getMainLooper().thread).isSameInstanceAs(Thread.currentThread())
  }

  @Test
  fun `a test gets arguments from JUnit and from the Android environment`(
    testInfo: TestInfo,
    context: Context,
    application: Application,
  ) {
    assertThat(testInfo.displayName).contains("a test gets arguments")
    assertThat(context).isSameInstanceAs(RuntimeEnvironment.getApplication())
    assertThat(application).isSameInstanceAs(context)
  }

  @ParameterizedTest
  @ValueSource(ints = [1, 2])
  fun `a parameterized test gets its arguments`(value: Int) {
    assertThat(value).isAnyOf(1, 2)
    assertThat(Build.VERSION.SDK_INT).isEqualTo(34)
  }

  @ParameterizedTest
  @EnumSource(Shape::class)
  fun `a parameterized test gets enum arguments of the Android environment`(shape: Shape) {
    assertThat(shape).isAnyOf(Shape.ROUND, Shape.SQUARE)
  }

  @RepeatedTest(2)
  fun `a repeated test gets its repetition`(repetition: RepetitionInfo) {
    assertThat(repetition.totalRepetitions).isEqualTo(2)
  }

  enum class Shape {
    ROUND,
    SQUARE,
  }

  @Nested
  inner class NestedTests {
    private var innerSetUpRan = false

    @BeforeEach
    fun setUpInner() {
      innerSetUpRan = true
    }

    @Test
    fun `the lifecycle of the enclosing class runs on the enclosing instance`() {
      assertThat(testNameAtSetUp).startsWith("the lifecycle of the enclosing class")
      assertThat(innerSetUpRan).isTrue()
    }

    @Test
    fun `the configuration of the enclosing class applies`() {
      assertThat(Build.VERSION.SDK_INT).isEqualTo(34)
    }
  }

  /** Tests that one test instance, and its Android environment, serve all tests of a class. */
  @Nested
  @TestInstance(TestInstance.Lifecycle.PER_CLASS)
  @TestMethodOrder(MethodOrderer.OrderAnnotation::class)
  inner class OneInstancePerClass {
    private var application: Application? = null
    private var testsRun = 0

    @BeforeAll
    fun setUpOnce() {
      application = RuntimeEnvironment.getApplication()
    }

    @AfterAll
    fun tearDownOnce() {
      assertThat(testsRun).isEqualTo(2)
    }

    @Test
    @Order(1)
    fun `a BeforeAll method of the instance ran in the shared environment`() {
      testsRun++
      assertThat(application).isSameInstanceAs(RuntimeEnvironment.getApplication())
    }

    @Test
    @Order(2)
    fun `the instance keeps its state between tests`() {
      testsRun++
      assertThat(testsRun).isEqualTo(2)
      assertThat(application).isSameInstanceAs(RuntimeEnvironment.getApplication())
    }
  }
}
