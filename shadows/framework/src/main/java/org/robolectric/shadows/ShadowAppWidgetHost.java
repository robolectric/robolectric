package org.robolectric.shadows;

import android.appwidget.AppWidgetHost;
import android.appwidget.AppWidgetHostView;
import android.appwidget.AppWidgetProviderInfo;
import android.content.Context;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.shadow.api.Shadow;
import org.robolectric.util.ReflectionHelpers;

@Implements(AppWidgetHost.class)
public class ShadowAppWidgetHost {
  @RealObject private AppWidgetHost realAppWidgetHost;

  private Context context;
  private int hostId;
  private int appWidgetIdToAllocate;
  private boolean listening = false;

  @Implementation
  protected void __constructor__(Context context, int hostId) {
    this.context = context;
    this.hostId = hostId;
  }

  public Context getContext() {
    return context;
  }

  public int getHostId() {
    return hostId;
  }

  public void setAppWidgetIdToAllocate(int idToAllocate) {
    appWidgetIdToAllocate = idToAllocate;
  }

  /** Returns true if this host is listening for updates. */
  public boolean isListening() {
    return listening;
  }

  @Implementation
  protected int allocateAppWidgetId() {
    return appWidgetIdToAllocate;
  }

  @Implementation
  protected AppWidgetHostView createView(
      Context context, int appWidgetId, AppWidgetProviderInfo appWidget) {
    AppWidgetHostView hostView =
        ReflectionHelpers.callInstanceMethod(
            AppWidgetHost.class,
            realAppWidgetHost,
            "onCreateView",
            ReflectionHelpers.ClassParameter.from(Context.class, context),
            ReflectionHelpers.ClassParameter.from(int.class, appWidgetId),
            ReflectionHelpers.ClassParameter.from(AppWidgetProviderInfo.class, appWidget));
    hostView.setAppWidget(appWidgetId, appWidget);
    ShadowAppWidgetHostView shadowAppWidgetHostView = Shadow.extract(hostView);
    shadowAppWidgetHostView.setHost(realAppWidgetHost);
    return hostView;
  }

  @Implementation
  protected void startListening() {
    listening = true;
  }

  @Implementation
  protected void stopListening() {
    listening = false;
  }
}
