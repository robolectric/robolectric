package org.robolectric.shadows;

import android.appwidget.AppWidgetHost;
import android.appwidget.AppWidgetHostView;
import android.appwidget.AppWidgetProviderInfo;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.TestRunners;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.robolectric.Robolectric.shadowOf;

@RunWith(TestRunners.WithDefaults.class)
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
    assertThat(appWidgetHostView.getAppWidgetId()).isEqualTo(789);
  }

  @Test
  public void shouldKnowItsAppWidgetProviderInfo() throws Exception {
    AppWidgetProviderInfo providerInfo = new AppWidgetProviderInfo();
    appWidgetHostView.setAppWidget(0, providerInfo);
    assertThat(appWidgetHostView.getAppWidgetInfo()).isSameAs(providerInfo);
  }

  @Test
  public void shouldHaveNullHost() throws Exception {
    assertThat(shadowAppWidgetHostView.getHost()).isNull();
  }

  @Test
  public void shouldBeAbleToHaveHostSet() throws Exception {
    AppWidgetHost host = new AppWidgetHost(Robolectric.application, 0);
    shadowAppWidgetHostView.setHost(host);
    assertThat(shadowAppWidgetHostView.getHost()).isSameAs(host);
  }
}
