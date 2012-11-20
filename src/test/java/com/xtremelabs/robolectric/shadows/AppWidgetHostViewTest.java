package com.xtremelabs.robolectric.shadows;

import android.appwidget.AppWidgetHostView;
import com.xtremelabs.robolectric.Robolectric;
import com.xtremelabs.robolectric.WithTestDefaultsRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static com.xtremelabs.robolectric.Robolectric.shadowOf;
import static org.hamcrest.CoreMatchers.is;
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
}
