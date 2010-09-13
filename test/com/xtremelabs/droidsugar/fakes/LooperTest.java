package com.xtremelabs.droidsugar.fakes;

import android.os.Looper;
import com.xtremelabs.droidsugar.DroidSugarAndroidTestRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertSame;

@RunWith(DroidSugarAndroidTestRunner.class)
public class LooperTest {
    @Before
    public void setUp() throws Exception {
        DroidSugarAndroidTestRunner.addProxy(Looper.class, FakeLooper.class);
    }

    @Test
    public void testMainLooperAndMyLooperAreTheSameInstanceOnMainThread() throws Exception {
        assertSame(Looper.myLooper(), Looper.getMainLooper());
    }
}
