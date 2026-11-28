package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.BAKLAVA;
import static android.os.Build.VERSION_CODES.S;
import static android.os.Build.VERSION_CODES.S_V2;
import static org.robolectric.util.reflector.Reflector.reflector;

import android.appwidget.AppWidgetHost;
import android.appwidget.AppWidgetHostView;
import android.appwidget.AppWidgetProviderInfo;
import android.content.Context;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.util.reflector.Accessor;
import org.robolectric.util.reflector.ForType;

@Implements(AppWidgetHostView.class)
public class ShadowAppWidgetHostView extends ShadowViewGroup {

  @RealObject private AppWidgetHostView appWidgetHostView;
  private AppWidgetHost host;

  /**
   * Records the widget's id and provider info, which {@link AppWidgetHostView#updateAppWidget} then
   * uses as the framework does. Unlike the framework, it does not look up the provider's padding or
   * label, so an {@link AppWidgetProviderInfo} built in a test without a provider can be used.
   */
  @Implementation
  protected void setAppWidget(int appWidgetId, AppWidgetProviderInfo info) {
    AppWidgetHostViewReflector reflector =
        reflector(AppWidgetHostViewReflector.class, appWidgetHostView);
    reflector.setAppWidgetId(appWidgetId);
    reflector.setInfo(info);
  }

  @ForType(AppWidgetHostView.class)
  interface AppWidgetHostViewReflector {
    @Accessor("mAppWidgetId")
    void setAppWidgetId(int appWidgetId);

    @Accessor("mInfo")
    void setInfo(AppWidgetProviderInfo info);
  }

  /**
   * Returns the host view's own context. The framework builds the provider's context from its
   * {@code ApplicationInfo}, which needs the provider's code to be installed; under Robolectric the
   * provider is the app under test, whose code is already loaded.
   */
  @Implementation(maxSdk = S)
  protected Context getRemoteContext() {
    return appWidgetHostView.getContext();
  }

  /** As {@link #getRemoteContext()}. */
  @Implementation(minSdk = S_V2, maxSdk = BAKLAVA)
  protected Context getRemoteContextEnsuringCorrectCachedApkPath() {
    return appWidgetHostView.getContext();
  }

  public AppWidgetHost getHost() {
    return host;
  }

  public void setHost(AppWidgetHost host) {
    this.host = host;
  }
}
