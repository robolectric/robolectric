package com.xtremelabs.droidsugar.fakes;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import com.xtremelabs.droidsugar.DroidSugarAndroidTestRunner;
import com.xtremelabs.droidsugar.util.FakeHelper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.assertThat;

@RunWith(DroidSugarAndroidTestRunner.class)
public class ApplicationTest {
    @Before
    public void setUp() throws Exception {
        DroidSugarAndroidTestRunner.addGenericProxies();

        FakeHelper.application = new Application();
    }

    @Test
    public void shouldBeAContext() throws Exception {
        assertThat(new Activity().getApplication(), sameInstance(FakeHelper.application));
        assertThat(new Activity().getApplication().getApplicationContext(), sameInstance((Context)FakeHelper.application));
    }
}
