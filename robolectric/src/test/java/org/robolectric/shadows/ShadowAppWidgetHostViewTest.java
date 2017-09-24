package org.robolectric.shadows;

import static org.assertj.core.api.Assertions.assertThat;
import static org.robolectric.Shadows.shadowOf;

import android.appwidget.AppWidgetHost;
import android.appwidget.AppWidgetHostView;
import android.appwidget.AppWidgetProviderInfo;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.RobolectricTestRunner;

@RunWith(RobolectricTestRunner.class)
public class ShadowAppWidgetHostViewTest {
  private AppWidgetHostView appWidgetHostView;
  private ShadowAppWidgetHostView shadowAppWidgetHostView;

  @Before
  public void setUp() throws Exception {
    appWidgetHostView = new AppWidgetHostView(RuntimeEnvironment.application);
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
    AppWidgetHost host = new AppWidgetHost(RuntimeEnvironment.application, 0);
    shadowAppWidgetHostView.setHost(host);
    assertThat(shadowAppWidgetHostView.getHost()).isSameAs(host);
  }
}
