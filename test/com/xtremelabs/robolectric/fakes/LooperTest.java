package com.xtremelabs.robolectric.fakes;

import android.os.Looper;
import com.xtremelabs.robolectric.DogfoodRobolectricTestRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertSame;

@RunWith(DogfoodRobolectricTestRunner.class)
public class LooperTest {
    @Before
    public void setUp() throws Exception {
        DogfoodRobolectricTestRunner.addProxy(Looper.class, ShadowLooper.class);
    }

    @Test
    public void testMainLooperAndMyLooperAreTheSameInstanceOnMainThread() throws Exception {
        assertSame(Looper.myLooper(), Looper.getMainLooper());
    }
}
