package com.xtremelabs.robolectric.shadows;

import android.os.Bundle;
import com.xtremelabs.robolectric.WithTestDefaultsRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;


@RunWith(WithTestDefaultsRunner.class)
public class BundleTest {

    private Bundle bundle;

    @Before public void setUp() throws Exception {
        bundle = new Bundle();
    }

    @Test
    public void testContainsKey() throws Exception {
        assertFalse(bundle.containsKey("foo"));
        bundle.putString("foo", "bar");
        assertTrue(bundle.containsKey("foo"));
    }
}
