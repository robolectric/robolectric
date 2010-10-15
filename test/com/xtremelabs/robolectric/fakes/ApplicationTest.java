package com.xtremelabs.robolectric.fakes;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import com.xtremelabs.robolectric.RobolectricAndroidTestRunner;
import com.xtremelabs.robolectric.util.FakeHelper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.assertThat;

@RunWith(RobolectricAndroidTestRunner.class)
public class ApplicationTest {
    @Before
    public void setUp() throws Exception {
        RobolectricAndroidTestRunner.addGenericProxies();

        FakeHelper.application = new Application();
    }

    @Test
    public void shouldBeAContext() throws Exception {
        assertThat(new Activity().getApplication(), sameInstance(FakeHelper.application));
        assertThat(new Activity().getApplication().getApplicationContext(), sameInstance((Context)FakeHelper.application));
    }
}
