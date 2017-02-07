package org.robolectric.util;

import android.app.Service;
import android.content.Intent;
import org.robolectric.ShadowsAdapter;
import org.robolectric.android.controller.ComponentController;

import static org.robolectric.util.ReflectionHelpers.ClassParameter.from;

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

  abstract public org.robolectric.android.controller.ServiceController<T> bind();

  abstract public org.robolectric.android.controller.ServiceController<T> create();

  abstract public org.robolectric.android.controller.ServiceController<T> destroy();

  abstract public org.robolectric.android.controller.ServiceController<T> rebind();

  abstract public org.robolectric.android.controller.ServiceController<T> startCommand(int flags, int startId);

  abstract public org.robolectric.android.controller.ServiceController<T> unbind();
}
