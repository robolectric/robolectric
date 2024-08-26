package org.robolectric.integrationtests.dynamicfeatures

import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class MainActivityTest {

    @Test
    fun `MainActivity should be created successfully`() {
        val activity = Robolectric.buildActivity(MainActivity::class.java).create().get()

        assert(activity != null)
    }
}
