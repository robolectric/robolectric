package com.xtremelabs.robolectric.shadows;

import android.os.Looper;
import com.xtremelabs.robolectric.DogfoodRobolectricTestRunner;
import com.xtremelabs.robolectric.Robolectric;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertSame;

@RunWith(DogfoodRobolectricTestRunner.class)
public class LooperTest {
    @Before
    public void setUp() throws Exception {
        Robolectric.bindShadowClass(Looper.class, ShadowLooper.class);
    }

    @Test
    public void testMainLooperAndMyLooperAreTheSameInstanceOnMainThread() throws Exception {
        assertSame(Looper.myLooper(), Looper.getMainLooper());
    }
}
