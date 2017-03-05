package org.robolectric.android.controller;

import android.app.IntentService;
import android.content.Intent;
import org.robolectric.ShadowsAdapter;

import static org.robolectric.util.ReflectionHelpers.ClassParameter.from;

public class IntentServiceController<T extends IntentService> extends org.robolectric.util.IntentServiceController<T> {
  public static <T extends IntentService> IntentServiceController<T> of(final ShadowsAdapter shadowsAdapter,
                                                                        final T service,
                                                                        final Intent intent) {
    final IntentServiceController<T> controller = new IntentServiceController<>(shadowsAdapter, service, intent);
      return controller;
    }

    private IntentServiceController(final ShadowsAdapter shadowsAdapter, final T service, final Intent intent) {
        super(shadowsAdapter, service, intent);
    }

    public IntentServiceController<T> bind() {
      invokeWhilePaused("onBind", from(Intent.class, getIntent()));
      return this;
    }

    public IntentServiceController<T> create() {
      invokeWhilePaused("onCreate");
      return this;
    }

    public IntentServiceController<T> destroy() {
      invokeWhilePaused("onDestroy");
      return this;
    }

    public IntentServiceController<T> rebind() {
      invokeWhilePaused("onRebind", from(Intent.class, getIntent()));
      return this;
    }

    public IntentServiceController<T> startCommand(final int flags, final int startId) {
      final IntentServiceController<T> intentServiceController = handleIntent();
      get().stopSelf(startId);
      return intentServiceController;
    }

    public IntentServiceController<T> unbind() {
      invokeWhilePaused("onUnbind", from(Intent.class, getIntent()));
      return this;
    }

    public IntentServiceController<T> handleIntent() {
      invokeWhilePaused("onHandleIntent", from(Intent.class, getIntent()));
      return this;
    }
}