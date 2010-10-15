package com.xtremelabs.robolectric.fakes;

import android.os.Looper;
import com.xtremelabs.robolectric.RobolectricAndroidTestRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertSame;

@RunWith(RobolectricAndroidTestRunner.class)
public class LooperTest {
    @Before
    public void setUp() throws Exception {
        RobolectricAndroidTestRunner.addProxy(Looper.class, FakeLooper.class);
    }

    @Test
    public void testMainLooperAndMyLooperAreTheSameInstanceOnMainThread() throws Exception {
        assertSame(Looper.myLooper(), Looper.getMainLooper());
    }
}
