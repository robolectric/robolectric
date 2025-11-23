package org.robolectric.integrationtests.kotlin.exception

import android.app.Activity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.junit.Assert.assertThrows
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.shadows.ShadowLooper

@RunWith(RobolectricTestRunner::class)
@OptIn(DelicateCoroutinesApi::class)
class DummyExceptionTest {

    lateinit var uninitializedProperty: String

    @Test
    fun `GIVEN throwUncaughtExceptions property is true, THEN an exception is thrown`() {
        val controller = Robolectric.buildActivity(Activity::class.java)
        controller.setup().resume()
        val exception = assertThrows(
            UninitializedPropertyAccessException::class.java
        ) {
            controller.use {
                uninitializedProperty.toUByte() // Throws UninitializedPropertyAccessException
            }
        }
        assert(exception != null)
    }

    @Test
    fun `GIVEN throwUncaughtExceptions property is false, THEN the exception is swallowed`() {
        // Set false on purpose
        System.setProperty("robolectric.throwUncaughtExceptions", "false")
        val controller = Robolectric.buildActivity(Activity::class.java)
        controller.setup().resume()
        controller.use {
            GlobalScope.launch {
                uninitializedProperty.toUByte()
            }
        }
    }
}