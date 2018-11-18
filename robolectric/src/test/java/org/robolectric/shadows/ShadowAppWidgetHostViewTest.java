package org.robolectric.shadows;

import static com.google.common.truth.Truth.assertThat;
import static org.robolectric.Shadows.shadowOf;

import android.appwidget.AppWidgetHost;
import android.appwidget.AppWidgetHostView;
import android.appwidget.AppWidgetProviderInfo;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class ShadowAppWidgetHostViewTest {
  private AppWidgetHostView appWidgetHostView;
  private ShadowAppWidgetHostView shadowAppWidgetHostView;

  @Before
  public void setUp() throws Exception {
    appWidgetHostView = new AppWidgetHostView(ApplicationProvider.getApplicationContext());
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
    AppWidgetHost host = new AppWidgetHost(ApplicationProvider.getApplicationContext(), 0);
    shadowAppWidgetHostView.setHost(host);
    assertThat(shadowAppWidgetHostView.getHost()).isSameAs(host);
  }
}
