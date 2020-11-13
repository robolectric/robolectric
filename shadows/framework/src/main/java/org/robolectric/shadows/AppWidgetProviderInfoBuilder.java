package org.robolectric.shadows;

import static org.robolectric.util.reflector.Reflector.reflector;

import android.appwidget.AppWidgetProviderInfo;
import android.content.pm.ActivityInfo;
import org.robolectric.util.reflector.Accessor;
import org.robolectric.util.reflector.ForType;

/** Class to build {@link AppWidgetProviderInfo} */
public class AppWidgetProviderInfoBuilder {
  private ActivityInfo providerInfo;

  private AppWidgetProviderInfoBuilder() {}

  public AppWidgetProviderInfoBuilder setProviderInfo(ActivityInfo providerInfo) {
    this.providerInfo = providerInfo;
    return this;
  }

  public AppWidgetProviderInfo build() {
    AppWidgetProviderInfo appWidgetProviderInfo = new AppWidgetProviderInfo();
    if (this.providerInfo != null) {
      reflector(AppWidgetProviderInfoReflector.class, appWidgetProviderInfo)
          .setProviderInfo(this.providerInfo);
    }
    return appWidgetProviderInfo;
  }

  /**
   * Create a new {@link AppWidgetProviderInfoBuilder}.
   *
   * @return The created {@link AppWidgetProviderInfoBuilder}.
   */
  public static AppWidgetProviderInfoBuilder newBuilder() {
    return new AppWidgetProviderInfoBuilder();
  }

  /** Accessor interface for {@link AppWidgetProviderInfo}'s internals. */
  @ForType(AppWidgetProviderInfo.class)
  interface AppWidgetProviderInfoReflector {
    @Accessor("providerInfo")
    void setProviderInfo(ActivityInfo providerInfo);
  }
}
