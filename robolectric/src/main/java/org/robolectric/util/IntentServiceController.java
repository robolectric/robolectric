package org.robolectric.util;

import android.app.IntentService;
import android.content.Intent;
import org.robolectric.ShadowsAdapter;
import org.robolectric.android.controller.ComponentController;

/**
 * @deprecated Use {@link org.robolectric.android.controller.IntentServiceController} instead.
 * This will be removed in a forthcoming release.
 */
@Deprecated
abstract public class IntentServiceController<T extends IntentService> extends ComponentController<IntentServiceController<T>, T> {
  /**
   * @deprecated Use {@link org.robolectric.android.controller.IntentServiceController#of(ShadowsAdapter, IntentService, Intent)} instead.
   * This will be removed in a forthcoming release.
   */
  @Deprecated
  public static <T extends IntentService> org.robolectric.android.controller.IntentServiceController<T> of(final ShadowsAdapter shadowsAdapter,
                                                                                                           final T service,
                                                                                                           final Intent intent) {
    return org.robolectric.android.controller.IntentServiceController.of(shadowsAdapter, service, intent);
  }

  protected IntentServiceController(ShadowsAdapter shadowsAdapter, T activity, Intent intent) {
    super(shadowsAdapter, activity, intent);
  }

  abstract public org.robolectric.android.controller.IntentServiceController<T> bind();

  abstract public org.robolectric.android.controller.IntentServiceController<T> create();

  abstract public org.robolectric.android.controller.IntentServiceController<T> destroy();

  abstract public org.robolectric.android.controller.IntentServiceController<T> rebind();

  abstract public org.robolectric.android.controller.IntentServiceController<T> startCommand(final int flags, final int startId);

  abstract public org.robolectric.android.controller.IntentServiceController<T> unbind();

  abstract public org.robolectric.android.controller.IntentServiceController<T> handleIntent();
}
