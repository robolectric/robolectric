package org.robolectric.integrationtests.mockito.kotlin

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify

/** Tests for Mockito + Kotlin in Kotlin in a Robolectric environment. */
@RunWith(AndroidJUnit4::class)
class MockitoKotlinFunctionInKotlinTest {
  @Test
  fun testFunction1() {
    val function = mock(Function1::class.java) as (String) -> Unit
    function.invoke("test")
    verify(function).invoke("test")
  }
}
