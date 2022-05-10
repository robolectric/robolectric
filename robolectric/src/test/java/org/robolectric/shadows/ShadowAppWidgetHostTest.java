package org.robolectric.shadows;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.assertNotNull;
import static org.robolectric.Shadows.shadowOf;

import android.appwidget.AppWidgetHost;
import android.appwidget.AppWidgetHostView;
import android.appwidget.AppWidgetProviderInfo;
import android.content.Context;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class ShadowAppWidgetHostTest {
  private AppWidgetHost appWidgetHost;
  private ShadowAppWidgetHost shadowAppWidgetHost;
  private Context context;

  @Before
  public void setup() {
    context = ApplicationProvider.getApplicationContext();
    appWidgetHost = new AppWidgetHost(context, 404);
    shadowAppWidgetHost = shadowOf(appWidgetHost);
  }

  @Test
  public void shouldKnowItsContext() {
    assertThat(shadowAppWidgetHost.getContext()).isSameInstanceAs(context);
  }

  @Test
  public void shouldKnowItsHostId() {
    assertThat(shadowAppWidgetHost.getHostId()).isEqualTo(404);
  }

  @Test
  public void createView_shouldReturnAppWidgetHostView() {
    AppWidgetHostView hostView = appWidgetHost.createView(context, 0, null);
    assertNotNull(hostView);
  }

  @Test
  public void createView_shouldSetViewsContext() {
    AppWidgetHostView hostView = appWidgetHost.createView(context, 0, null);
    assertThat(hostView.getContext()).isSameInstanceAs(context);
  }

  @Test
  public void createView_shouldSetViewsAppWidgetId() {
    AppWidgetHostView hostView = appWidgetHost.createView(context, 765, null);
    assertThat(hostView.getAppWidgetId()).isEqualTo(765);
  }

  @Test
  public void createView_shouldSetViewsAppWidgetInfo() {
    AppWidgetProviderInfo info = new AppWidgetProviderInfo();
    AppWidgetHostView hostView = appWidgetHost.createView(context, 0, info);
    assertThat(hostView.getAppWidgetInfo()).isSameInstanceAs(info);
  }

  @Test
  public void createView_shouldSetHostViewsHost() {
    AppWidgetHostView hostView = appWidgetHost.createView(context, 0, null);
    assertThat(shadowOf(hostView).getHost()).isSameInstanceAs(appWidgetHost);
  }

  @Test
  public void shouldKnowIfItIsListening() {
    assertThat(shadowAppWidgetHost.isListening()).isFalse();
    appWidgetHost.startListening();
    assertThat(shadowAppWidgetHost.isListening()).isTrue();
    appWidgetHost.stopListening();
    assertThat(shadowAppWidgetHost.isListening()).isFalse();
  }
}
