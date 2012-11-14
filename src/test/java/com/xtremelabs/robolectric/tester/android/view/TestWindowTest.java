package com.xtremelabs.robolectric.tester.android.view;

import com.xtremelabs.robolectric.TestRunners;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(TestRunners.WithDefaults.class)
public class TestWindowTest {

    @Test
    public void windowManager__shouldNotBeNull() throws Exception {
        TestWindow window = new TestWindow(null);
        Assert.assertNotNull(window.getWindowManager());
    }
}
