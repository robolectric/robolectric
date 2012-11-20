package com.xtremelabs.robolectric.shadows;

import android.appwidget.AppWidgetHost;
import android.content.Context;
import com.xtremelabs.robolectric.Robolectric;
import com.xtremelabs.robolectric.WithTestDefaultsRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static com.xtremelabs.robolectric.Robolectric.shadowOf;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.junit.Assert.assertThat;

@RunWith(WithTestDefaultsRunner.class)
public class AppWidgetHostTest {
    private AppWidgetHost appWidgetHost;
    private ShadowAppWidgetHost shadowAppWidgetHost;
    private Context context;

    @Before
    public void setup() throws Exception {
        context = Robolectric.application;
        appWidgetHost = new AppWidgetHost(context, 404);
        shadowAppWidgetHost = shadowOf(appWidgetHost);
    }

    @Test
    public void shouldKnowItsContext() throws Exception {
        assertThat(shadowAppWidgetHost.getContext(), sameInstance(context));
    }
}
