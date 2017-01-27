package org.robolectric.util;

import android.app.Service;
import android.content.Intent;
import org.robolectric.ShadowsAdapter;
import org.robolectric.android.controller.ComponentController;

/**
 * @deprecated Use {@link org.robolectric.android.controller.ServiceController} instead.
 * This will be removed in a forthcoming release.
 */
@Deprecated
abstract public class ServiceController<T extends Service> extends ComponentController<org.robolectric.android.controller.ServiceController<T>, T> {
  /**
   * @deprecated Use {@link org.robolectric.android.controller.ServiceController#of(ShadowsAdapter, Service, Intent)} instead.
   * This will be removed in a forthcoming release.
   */
  @Deprecated
  public static <T extends Service> org.robolectric.android.controller.ServiceController<T> of(ShadowsAdapter shadowsAdapter, T service, Intent intent) {
    return org.robolectric.android.controller.ServiceController.of(shadowsAdapter, service, intent);
  }

  protected ServiceController(ShadowsAdapter shadowsAdapter, T service, Intent intent) {
    super(shadowsAdapter, service, intent);
  }
}
