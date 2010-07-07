package com.xtremelabs.droidsugar.view;

import android.content.Intent;
import com.xtremelabs.droidsugar.DroidSugarAndroidTestRunner;
import junit.framework.TestCase;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(DroidSugarAndroidTestRunner.class)
public class FakeIntentTest extends TestCase {

    @Before
    public void setUp() throws Exception {
        DroidSugarAndroidTestRunner.addProxy(Intent.class, FakeIntent.class);
    }

    @Test
    public void testExtrasAreStored() throws Exception {
        Intent intent = new Intent();
        intent.putExtra("foo", "bar");

        FakeIntent fakeIntent = (FakeIntent) DroidSugarAndroidTestRunner.proxyFor(intent);
        assertNotNull(fakeIntent);
        assertNotNull(fakeIntent.extras);
        assertEquals("bar", fakeIntent.extras.get("foo"));
    }

    @Test
    public void testGetActionReturnsWhatWasSet() throws Exception {
        Intent intent = new Intent();
        intent.setAction("foo");
        assertEquals("foo", intent.getAction());
    }
}
