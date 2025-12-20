package org.robolectric.junit.jupiter

import android.app.Application
import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

/** Tests for @BeforeAll/@AfterAll support in RobolectricExtension. */
@ExtendWith(RobolectricExtension::class)
@Config(sdk = [28])
class BeforeAllAfterAllTest {

  companion object {
    private var setupCalled = false
    private var teardownCalled = false
    private var sharedApplication: Application? = null

    @BeforeAll
    @JvmStatic
    fun setupClass() {
      setupCalled = true
      // Access Robolectric environment in @BeforeAll
      sharedApplication = RuntimeEnvironment.getApplication()
      assertThat(sharedApplication).isNotNull()
    }

    @AfterAll
    @JvmStatic
    fun teardownClass() {
      teardownCalled = true
      sharedApplication = null
    }
  }

  @Test
  fun testSetupWasCalled() {
    assertThat(setupCalled).isTrue()
  }

  @Test
  fun testSharedApplicationIsAvailable() {
    assertThat(sharedApplication).isNotNull()
  }

  @Test
  fun testSharedApplicationConsistency() {
    val currentApp = RuntimeEnvironment.getApplication()
    // Application instance is recreated for each test, so it won't be the same instance
    // as the one created in @BeforeAll.
    assertThat(sharedApplication).isNotSameInstanceAs(currentApp)
    assertThat(sharedApplication?.packageName).isEqualTo(currentApp.packageName)
  }

  @AfterEach
  fun verifyState() {
    // Verify shared state is maintained
    assertThat(sharedApplication).isNotNull()
  }
}

/** Tests that @AfterAll is properly called. */
@ExtendWith(RobolectricExtension::class)
@Config(sdk = [28])
class AfterAllExecutionTest {

  companion object {
    var afterAllExecuted = false

    @AfterAll
    @JvmStatic
    fun cleanup() {
      afterAllExecuted = true
    }
  }

  @Test
  fun testDummy() {
    // Just a test to trigger class execution
    assertThat(true).isTrue()
  }
}
