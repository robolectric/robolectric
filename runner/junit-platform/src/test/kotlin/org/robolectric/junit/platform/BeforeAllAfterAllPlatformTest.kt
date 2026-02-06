package org.robolectric.junit.platform

import android.app.Application
import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.*
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

/** Tests for @BeforeAll/@AfterAll support in JUnit Platform test engine. */
@Config(sdk = [28])
class BeforeAllAfterAllPlatformTest {

  companion object {
    private var classSetupCalled = false
    private var classTeardownCalled = false
    private lateinit var sharedApp: Application

    @BeforeAll
    @JvmStatic
    fun classSetup() {
      classSetupCalled = true
      sharedApp = RuntimeEnvironment.getApplication()
      assertNotNull(sharedApp, "Application should be available in @BeforeAll")
    }

    @AfterAll
    @JvmStatic
    fun classTeardown() {
      classTeardownCalled = true
    }
  }

  @Test
  fun testClassSetupWasExecuted() {
    assertThat(classSetupCalled).isTrue()
    assertThat(sharedApp).isNotNull()
  }

  @Test
  fun testSharedApplicationConsistency() {
    val currentApp = RuntimeEnvironment.getApplication()
    // Application instance is recreated for each test, so it won't be the same instance
    // as the one created in @BeforeAll.
    assertThat(sharedApp).isNotSameInstanceAs(currentApp)
    assertThat(sharedApp.packageName).isEqualTo(currentApp.packageName)
  }

  @Test
  fun testSharedApplicationPackageName() {
    assertEquals("org.robolectric.default", sharedApp.packageName)
  }
}

/** Tests execution order of lifecycle methods. */
@Config(sdk = [28])
class LifecycleOrderTest {

  companion object {
    val executionOrder = mutableListOf<String>()

    @BeforeAll
    @JvmStatic
    fun beforeAll() {
      executionOrder.add("beforeAll")
    }

    @AfterAll
    @JvmStatic
    fun afterAll() {
      executionOrder.add("afterAll")
      // Verify order: beforeAll -> beforeEach -> test -> afterEach -> ... -> afterAll
      assertTrue(executionOrder.first() == "beforeAll")
      assertTrue(executionOrder.last() == "afterAll")
    }
  }

  @BeforeEach
  fun beforeEach() {
    executionOrder.add("beforeEach")
  }

  @AfterEach
  fun afterEach() {
    executionOrder.add("afterEach")
  }

  @Test
  fun test1() {
    executionOrder.add("test1")
  }

  @Test
  fun test2() {
    executionOrder.add("test2")
  }
}
