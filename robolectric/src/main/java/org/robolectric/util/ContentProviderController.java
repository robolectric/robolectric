package org.robolectric.util;

import android.content.ContentProvider;
import android.content.pm.ProviderInfo;

/**
 * @deprecated Use {@link org.robolectric.android.controller.ContentProviderController} instead.
 * This will be removed in a forthcoming release.
 */
@Deprecated
abstract public class ContentProviderController<T extends ContentProvider> {
  /**
   * @deprecated Use {@link org.robolectric.android.controller.ContentProviderController#of(ContentProvider)} instead.
   * This will be removed in a forthcoming release.
   */
  @Deprecated
  public static <T extends ContentProvider> org.robolectric.android.controller.ContentProviderController<T> of(T contentProvider) {
    return org.robolectric.android.controller.ContentProviderController.of(contentProvider);
  }

  abstract public org.robolectric.android.controller.ContentProviderController<T> create();

  abstract public org.robolectric.android.controller.ContentProviderController<T> create(ProviderInfo providerInfo);

  abstract public T get();

  abstract public org.robolectric.android.controller.ContentProviderController<T> shutdown();
}
