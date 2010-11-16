package com.xtremelabs.robolectric;

import android.app.Application;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(WithTestDefaultsRunner.class)
public class RobolectricTestRunnerTest {
    @Test
    public void shouldInitializeApplication() throws Exception {
        assertNotNull(Robolectric.application);
        assertEquals(Application.class, Robolectric.application.getClass());
    }
}
