package com.xtremelabs.robolectric.shadows;

import android.appwidget.AppWidgetHost;
import android.appwidget.AppWidgetHostView;
import android.appwidget.AppWidgetProviderInfo;
import com.xtremelabs.robolectric.Robolectric;
import com.xtremelabs.robolectric.WithTestDefaultsRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static com.xtremelabs.robolectric.Robolectric.shadowOf;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;

@RunWith(WithTestDefaultsRunner.class)
public class AppWidgetHostViewTest {
    private AppWidgetHostView appWidgetHostView;
    private ShadowAppWidgetHostView shadowAppWidgetHostView;

    @Before
    public void setUp() throws Exception {
        appWidgetHostView = new AppWidgetHostView(Robolectric.application);
        shadowAppWidgetHostView = shadowOf(appWidgetHostView);
    }

    @Test
    public void shouldKnowItsWidgetId() throws Exception {
        appWidgetHostView.setAppWidget(789, null);
        assertThat(appWidgetHostView.getAppWidgetId(), is(789));
    }

    @Test
    public void shouldKnowItsAppWidgetProviderInfo() throws Exception {
        AppWidgetProviderInfo providerInfo = new AppWidgetProviderInfo();
        appWidgetHostView.setAppWidget(0, providerInfo);
        assertThat(appWidgetHostView.getAppWidgetInfo(), sameInstance(providerInfo));
    }

    @Test
    public void shouldHaveNullHost() throws Exception {
        assertThat(shadowAppWidgetHostView.getHost(), nullValue());
    }

    @Test
    public void shouldBeAbleToHaveHostSet() throws Exception {
        AppWidgetHost host = new AppWidgetHost(null, 0);
        shadowAppWidgetHostView.setHost(host);
        assertThat(shadowAppWidgetHostView.getHost(), sameInstance(host));
    }
}
