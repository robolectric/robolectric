package org.robolectric.android.controller;

import android.content.ComponentName;
import android.content.ContentProvider;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.pm.ProviderInfo;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.shadows.ShadowContentResolver;
import org.robolectric.util.Logger;

public class ContentProviderController<T extends ContentProvider>  {
  private T contentProvider;

  private ContentProviderController(T contentProvider) {
    this.contentProvider = contentProvider;
  }

  public static <T extends ContentProvider> ContentProviderController<T> of(T contentProvider) {
    return new ContentProviderController<>(contentProvider);
  }

  /**
   * Create and register {@link ContentProvider} using {@link ProviderInfo} found from manifest.
   */
  public ContentProviderController<T> create() {
    Context baseContext = RuntimeEnvironment.application.getBaseContext();

    ComponentName componentName = createRelative(baseContext.getPackageName(), contentProvider.getClass().getName());

    ProviderInfo providerInfo = null;
    try {
      providerInfo =
          baseContext
              .getPackageManager()
              .getProviderInfo(componentName, PackageManager.MATCH_DISABLED_COMPONENTS);
    } catch (PackageManager.NameNotFoundException e) {
      Logger.strict("Unable to find provider info for " + componentName, e);
    }

    return create(providerInfo);
  }

  /**
   * Create and register {@link ContentProvider} using {@link ProviderInfo} found from manifest.
   *
   * @param authority the authority to use
   * @return this {@link ContentProviderController}
   */
  public ContentProviderController<T> create(String authority) {
    ProviderInfo providerInfo = new ProviderInfo();
    providerInfo.authority = authority;
    return create(providerInfo);
  }

  /**
   * Create and register {@link ContentProvider} using the given {@link ProviderInfo}.
   *
   * @param providerInfo the {@link ProviderInfo} to use
   * @return this {@link ContentProviderController}
   */
  public ContentProviderController<T> create(ProviderInfo providerInfo) {
    Context baseContext = RuntimeEnvironment.application.getBaseContext();
    // make sure the component is enabled
    ComponentName componentName =
        createRelative(baseContext.getPackageName(), contentProvider.getClass().getName());
    baseContext
        .getPackageManager()
        .setComponentEnabledSetting(
            componentName, PackageManager.COMPONENT_ENABLED_STATE_ENABLED, 0);
    contentProvider.attachInfo(baseContext, providerInfo);

    if (providerInfo != null) {
      ShadowContentResolver.registerProviderInternal(providerInfo.authority, contentProvider);
    }

    return this;
  }

  public T get() {
    return contentProvider;
  }

  public ContentProviderController<T> shutdown() {
    contentProvider.shutdown();
    return this;
  }

  private static ComponentName createRelative(String pkg, String cls) {
    final String fullName;
    if (cls.charAt(0) == '.') {
      // Relative to the package. Prepend the package name.
      fullName = pkg + cls;
    } else {
      // Fully qualified package name.
      fullName = cls;
    }
    return new ComponentName(pkg, fullName);
  }
}
