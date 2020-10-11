package org.robolectric.shadows;

import android.appwidget.AppWidgetProviderInfo;
import android.content.pm.ActivityInfo;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.util.ReflectionHelpers;

@Implements(value = AppWidgetProviderInfo.class)
public class ShadowAppWidgetProviderInfo {
  @RealObject private AppWidgetProviderInfo realObject;

  public void setProviderInfo(ActivityInfo providerInfo) {
    ReflectionHelpers.setField(realObject, "providerInfo", providerInfo);
  }
}
