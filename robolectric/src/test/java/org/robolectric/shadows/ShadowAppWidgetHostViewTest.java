package org.robolectric.shadows;

import static com.google.common.truth.Truth.assertThat;
import static org.robolectric.Shadows.shadowOf;

import android.appwidget.AppWidgetHost;
import android.appwidget.AppWidgetHostView;
import android.appwidget.AppWidgetProviderInfo;
import android.widget.RemoteViews;
import android.widget.TextView;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.R;

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
  public void shouldKnowItsWidgetId() {
    appWidgetHostView.setAppWidget(789, null);
    assertThat(appWidgetHostView.getAppWidgetId()).isEqualTo(789);
  }

  @Test
  public void shouldKnowItsAppWidgetProviderInfo() {
    AppWidgetProviderInfo providerInfo = new AppWidgetProviderInfo();
    appWidgetHostView.setAppWidget(0, providerInfo);
    assertThat(appWidgetHostView.getAppWidgetInfo()).isSameInstanceAs(providerInfo);
  }

  @Test
  public void shouldHaveNullHost() {
    assertThat(shadowAppWidgetHostView.getHost()).isNull();
  }

  @Test
  public void shouldBeAbleToHaveHostSet() {
    AppWidgetHost host = new AppWidgetHost(ApplicationProvider.getApplicationContext(), 0);
    shadowAppWidgetHostView.setHost(host);
    assertThat(shadowAppWidgetHostView.getHost()).isSameInstanceAs(host);
  }

  @Test
  public void shouldBeAbleToAddRemoteViews() {
    RemoteViews remoteViews =
        new RemoteViews(
            ApplicationProvider.getApplicationContext().getPackageName(), R.layout.main);
    remoteViews.setTextViewText(R.id.subtitle, "Hola");
    appWidgetHostView.updateAppWidget(remoteViews);
    assertThat(((TextView) appWidgetHostView.findViewById(R.id.subtitle)).getText().toString())
        .isEqualTo("Hola");
  }
}
