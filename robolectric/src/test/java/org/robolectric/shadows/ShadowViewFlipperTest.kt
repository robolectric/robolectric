package org.robolectric.shadows

import android.content.Context
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue

import android.widget.ViewFlipper
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ShadowViewFlipperTest {
  protected lateinit var flipper: ViewFlipper

  @Before
  fun setUp() {
    flipper = ViewFlipper(ApplicationProvider.getApplicationContext<Context>())
  }

  @Test
  fun testStartFlipping() {
    flipper.startFlipping()
    assertTrue("flipping", flipper.isFlipping)
  }

  @Test
  fun testStopFlipping() {
    flipper.stopFlipping()
    assertFalse("flipping", flipper.isFlipping)
  }
}
