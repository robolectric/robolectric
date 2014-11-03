package org.robolectric.shadows;

import android.content.Context;
import android.content.pm.PackageManager;
import android.util.AttributeSet;
import android.widget.TextView;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;


import static org.assertj.core.api.Assertions.assertThat;


@RunWith(RobolectricTestRunner.class)
public class ShadowActivityThreadTest {


    @Test
    public void testTriggersUndeclaredThrowableException() throws Exception {
        // createPackageContext internally calls ActivityThread.getPackageInfo which is what we'd like to test here.
        try {
            Robolectric.application.createPackageContext("com.unknownpackage.ab", Context.CONTEXT_RESTRICTED);
            Assert.fail("Should've triggered a NameNotFoundException and not UndeclaredThrowableException");
        } catch (PackageManager.NameNotFoundException nnfe) {
            assertThat(nnfe).hasMessageContaining("com.unknownpackage.ab");
        }
    }




}
