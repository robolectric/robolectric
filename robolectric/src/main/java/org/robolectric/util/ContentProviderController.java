package org.robolectric.util;

import android.content.ComponentName;
import android.content.ContentProvider;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.pm.ProviderInfo;
import org.robolectric.RuntimeEnvironment;

public class ContentProviderController<T extends ContentProvider> {
  private T contentProvider;

  private ContentProviderController(T contentProvider) {
    this.contentProvider = contentProvider;
  }

  public static <T extends ContentProvider> ContentProviderController<T> of(T contentProvider) {
    return new ContentProviderController<>(contentProvider);
  }

  public ContentProviderController<T> create() {
    Context baseContext = RuntimeEnvironment.application.getBaseContext();
    ComponentName componentName = ComponentName.createRelative(baseContext, contentProvider.getClass().getName());

    ProviderInfo providerInfo;
    try {
      providerInfo = RuntimeEnvironment.getPackageManager().getProviderInfo(componentName, 0);
    } catch (PackageManager.NameNotFoundException e) {
      Logger.strict("Unable to find provider info for " + componentName, e);
      providerInfo = null;

    }
    contentProvider.attachInfo(baseContext, providerInfo);

    return this;
  }

  public T get() {
    return contentProvider;
  }

  public ContentProviderController<T> shutdown() {
    contentProvider.shutdown();
    return this;
  }
}
