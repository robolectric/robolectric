package com.xtremelabs.robolectric.shadows;

import android.appwidget.AppWidgetHost;
import android.appwidget.AppWidgetHostView;
import android.appwidget.AppWidgetProviderInfo;
import android.content.Context;
import com.xtremelabs.robolectric.Robolectric;
import com.xtremelabs.robolectric.WithTestDefaultsRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static com.xtremelabs.robolectric.Robolectric.shadowOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.junit.Assert.assertNotNull;
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

    @Test
    public void shouldKnowItsHostId() throws Exception {
        assertThat(shadowAppWidgetHost.getHostId(), is(404));
    }

    @Test
    public void createView_shouldReturnAppWidgetHostView() throws Exception {
        AppWidgetHostView hostView = appWidgetHost.createView(context, 0, null);
        assertNotNull(hostView);
    }

    @Test
    public void createView_shouldSetViewsContext() throws Exception {
        AppWidgetHostView hostView = appWidgetHost.createView(context, 0, null);
        assertThat(hostView.getContext(), sameInstance(context));
    }

    @Test
    public void createView_shouldSetViewsAppWidgetId() throws Exception {
        AppWidgetHostView hostView = appWidgetHost.createView(null, 765, null);
        assertThat(hostView.getAppWidgetId(), is(765));
    }

    @Test
    public void createView_shouldSetViewsAppWidgetInfo() throws Exception {
        AppWidgetProviderInfo info = new AppWidgetProviderInfo();
        AppWidgetHostView hostView = appWidgetHost.createView(null, 0, info);
        assertThat(hostView.getAppWidgetInfo(), sameInstance(info));
    }

    @Test
    public void createView_shouldSetHostViewsHost() throws Exception {
        AppWidgetHostView hostView = appWidgetHost.createView(null, 0, null);
        assertThat(shadowOf(hostView).getHost(), sameInstance(appWidgetHost));
    }
}
