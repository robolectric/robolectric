package com.xtremelabs.droidsugar.view;

import android.content.*;
import com.xtremelabs.droidsugar.*;
import junit.framework.*;
import org.junit.*;
import org.junit.runner.*;

@RunWith(DroidSugarAndroidTestRunner.class)
public class FakeIntentTest extends TestCase {

    @Before
    public void setUp() throws Exception {
        DroidSugarAndroidTestRunner.addProxy(Intent.class, FakeIntent.class);
    }

    @org.junit.Test
    public void testExtrasAreStored() throws Exception {
        Intent intent = new Intent();
        intent.putExtra("foo", "bar");

        FakeIntent fakeIntent = (FakeIntent) DroidSugarAndroidTestRunner.proxyFor(intent);
        assertNotNull(fakeIntent);
        assertNotNull(fakeIntent.extras);
        assertEquals("bar", fakeIntent.extras.get("foo"));
    }
}
