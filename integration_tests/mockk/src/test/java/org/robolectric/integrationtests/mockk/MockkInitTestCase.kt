package org.robolectric.integrationtests.mockk

import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

class NumberReturner {
  fun returnNumber() = RETURN_NUMBER

  companion object {
    private const val RETURN_NUMBER = 0
  }
}

@RunWith(RobolectricTestRunner::class)
class MockkInitTestCase {

  @MockK lateinit var returner: NumberReturner

  @Before fun setUp() = MockKAnnotations.init(this)

  @Test
  fun mockk1() {
    every { returner.returnNumber() } returns 1
    assert(returner.returnNumber() == 1)
  }
}
