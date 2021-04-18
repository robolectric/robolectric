package org.robolectric.shadows;

import android.appwidget.AppWidgetHost;
import android.appwidget.AppWidgetHostView;
import android.appwidget.AppWidgetProviderInfo;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RemoteViews;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;

@Implements(AppWidgetHostView.class)
public class ShadowAppWidgetHostView extends ShadowViewGroup {

  @RealObject private AppWidgetHostView appWidgetHostView;
  private int appWidgetId;
  private AppWidgetProviderInfo appWidgetInfo;
  private AppWidgetHost host;
  private View view;

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

  @Implementation
  protected void updateAppWidget(RemoteViews remoteViews) {
    if (view != null) {
      realViewGroup.removeView(view);
    }
    Context context = appWidgetHostView.getContext();
    view = LayoutInflater.from(context).inflate(remoteViews.getLayoutId(), null);
    remoteViews.reapply(context, view);
    realViewGroup.addView(view);
  }

  public AppWidgetHost getHost() {
    return host;
  }

  public void setHost(AppWidgetHost host) {
    this.host = host;
  }
}
