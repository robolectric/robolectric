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
  public void setup() throws Exception {
    context = ApplicationProvider.getApplicationContext();
    appWidgetHost = new AppWidgetHost(context, 404);
    shadowAppWidgetHost = shadowOf(appWidgetHost);
  }

  @Test
  public void shouldKnowItsContext() throws Exception {
    assertThat(shadowAppWidgetHost.getContext()).isSameAs(context);
  }

  @Test
  public void shouldKnowItsHostId() throws Exception {
    assertThat(shadowAppWidgetHost.getHostId()).isEqualTo(404);
  }

  @Test
  public void createView_shouldReturnAppWidgetHostView() throws Exception {
    AppWidgetHostView hostView = appWidgetHost.createView(context, 0, null);
    assertNotNull(hostView);
  }

  @Test
  public void createView_shouldSetViewsContext() throws Exception {
    AppWidgetHostView hostView = appWidgetHost.createView(context, 0, null);
    assertThat(hostView.getContext()).isSameAs(context);
  }

  @Test
  public void createView_shouldSetViewsAppWidgetId() throws Exception {
    AppWidgetHostView hostView = appWidgetHost.createView(context, 765, null);
    assertThat(hostView.getAppWidgetId()).isEqualTo(765);
  }

  @Test
  public void createView_shouldSetViewsAppWidgetInfo() throws Exception {
    AppWidgetProviderInfo info = new AppWidgetProviderInfo();
    AppWidgetHostView hostView = appWidgetHost.createView(context, 0, info);
    assertThat(hostView.getAppWidgetInfo()).isSameAs(info);
  }

  @Test
  public void createView_shouldSetHostViewsHost() throws Exception {
    AppWidgetHostView hostView = appWidgetHost.createView(context, 0, null);
    assertThat(shadowOf(hostView).getHost()).isSameAs(appWidgetHost);
  }
}
