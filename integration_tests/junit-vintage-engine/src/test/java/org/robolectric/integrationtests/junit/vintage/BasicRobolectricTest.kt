package org.robolectric.integrationtests.junit.vintage

import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.testapp.TestActivity

/** The test class for Robolectric to test behavior with JUnit vintage engine and JUnit jupiter. */
@RunWith(RobolectricTestRunner::class)
class BasicRobolectricTest {
  @Test
  fun launchActivity_testActivity_succeed() {
    Robolectric.buildActivity(TestActivity::class.java).use {
      it.resume()
      it.destroy()
    }
  }
}
