package org.robolectric.shadows;

import android.appwidget.AppWidgetHost;
import android.appwidget.AppWidgetHostView;
import android.appwidget.AppWidgetProviderInfo;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

@Implements(AppWidgetHostView.class)
public class ShadowAppWidgetHostView extends ShadowViewGroup {

  private int appWidgetId;
  private AppWidgetProviderInfo appWidgetInfo;
  private AppWidgetHost host;

  @Implementation
  protected void setAppWidget(int appWidgetId, AppWidgetProviderInfo info) {
    this.appWidgetId = appWidgetId;
    this.appWidgetInfo = info;
  }

  @Implementation
  protected int getAppWidgetId() {
    return appWidgetId;
  }

  @Implementation
  protected AppWidgetProviderInfo getAppWidgetInfo() {
    return appWidgetInfo;
  }

  public AppWidgetHost getHost() {
    return host;
  }

  public void setHost(AppWidgetHost host) {
    this.host = host;
  }
}
