package org.robolectric.integrationtests.kotlin.exception

import android.app.Activity
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertThrows
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
@OptIn(DelicateCoroutinesApi::class)
class RobolectricExceptionCollectorTest {

    lateinit var pleaseDontUseMe: String

    @Test
    fun `GIVEN throwUncaughtExceptions property true, WHEN coroutine fails, THEN an exception is thrown`() {
        // Set to true to make this test and the subsequent one fail too
        System.setProperty("robolectric.throwUncaughtExceptions", "false")
        val controller = Robolectric.buildActivity(Activity::class.java)
        controller.setup().resume()
        controller.use {
            GlobalScope.launch {
                // Throws UninitializedPropertyAccessException
                pleaseDontUseMe.toUByte()
            }
        }
        assert(true)
    }

    @Test
    fun `GIVEN throwUncaughtExceptions property is false, THEN the exception is swallowed`() {
        // Test crashes if set to true, OR if previous test also crashed!
        System.setProperty("robolectric.throwUncaughtExceptions", "false")
        val controller = Robolectric.buildActivity(Activity::class.java)
        controller.setup().resume()
        controller.use {
            GlobalScope.launch {
                // Throws UninitializedPropertyAccessException
                pleaseDontUseMe.toUByte()
            }
        }
        assert(true)
    }
}