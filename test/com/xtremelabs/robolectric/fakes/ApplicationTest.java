package com.xtremelabs.robolectric.fakes;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.ContextWrapper;
import com.xtremelabs.robolectric.R;
import com.xtremelabs.robolectric.RobolectricAndroidTestRunner;
import com.xtremelabs.robolectric.res.ResourceLoader;
import com.xtremelabs.robolectric.res.StringResourceLoader;
import com.xtremelabs.robolectric.util.FakeHelper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

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

    @Test
    public void shouldBeBindableToAResourceLoader() throws Exception {
        ResourceLoader resourceLoader1 = new ResourceLoader(mock(StringResourceLoader.class), null, null, null, null) {};
        when(resourceLoader1.stringResourceLoader.getValue(R.id.title)).thenReturn("title from resourceLoader1");
        Application app1 = FakeApplication.bind(new Application(), resourceLoader1);

        ResourceLoader resourceLoader2 = new ResourceLoader(mock(StringResourceLoader.class), null, null, null, null) {};
        when(resourceLoader2.stringResourceLoader.getValue(R.id.title)).thenReturn("title from resourceLoader2");
        Application app2 = FakeApplication.bind(new Application(), resourceLoader2);

        assertEquals("title from resourceLoader1", new ContextWrapper(app1).getResources().getString(R.id.title));
        assertEquals("title from resourceLoader2", new ContextWrapper(app2).getResources().getString(R.id.title));
    }
}
