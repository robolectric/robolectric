package org.robolectric.shadows;

import static org.assertj.core.api.Assertions.assertThat;

import android.content.Context;
import android.content.pm.PackageManager;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.RobolectricTestRunner;

@RunWith(RobolectricTestRunner.class)
public class ShadowActivityThreadTest {
    @Test
    public void testTriggersUndeclaredThrowableException() throws Exception {
        // createPackageContext internally calls ActivityThread.getPackageInfo which is what we'd like to test here.
        try {
            RuntimeEnvironment.application.createPackageContext("com.unknownpackage.ab", Context.CONTEXT_RESTRICTED);
            Assert.fail("Should've triggered a NameNotFoundException and not UndeclaredThrowableException");
        } catch (PackageManager.NameNotFoundException nnfe) {
            assertThat(nnfe).hasMessageContaining("com.unknownpackage.ab");
        }
    }
}
