package org.robolectric.integrationtests.dynamicfeatures

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowLog

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class BaseSplitActivityTest {

    @Test
    fun `attachBaseContext should call SplitCompat installActivity`() {
        // Initialize the activity
        val context: Context = ApplicationProvider.getApplicationContext()
        val activity = Robolectric.buildActivity(TestSplitActivity::class.java)
            .create()
            .get()

        ShadowLog.getLogs().forEach { logItem ->
            if (logItem.msg.contains("SplitCompat.installActivity")) {
                assert(logItem.msg.contains(activity.javaClass.simpleName))
            }
        }
    }

    class TestSplitActivity : BaseSplitActivity()
}
