package org.robolectric.util;

import android.app.Application;
import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.ShadowsAdapter;
import org.robolectric.util.ReflectionHelpers.ClassParameter;

public class IntentServiceController<T extends IntentService> extends ComponentController<IntentServiceController<T>, T> {
    private final  String shadowActivityThreadClassName;

    public static <T extends IntentService> IntentServiceController<T> of(final ShadowsAdapter shadowsAdapter,
                                                                          final T service,
                                                                          final Intent intent) {
        final IntentServiceController<T> controller = new IntentServiceController<>(shadowsAdapter, service, intent);
        controller.attach();
        return controller;
    }

    private IntentServiceController(final ShadowsAdapter shadowsAdapter, final T service, final Intent intent) {
        super(shadowsAdapter, service, intent);
        shadowActivityThreadClassName = shadowsAdapter.getShadowActivityThreadClassName();
    }

    public IntentServiceController<T> attach() {
        if (attached) {
            return this;
        }

        final Context baseContext = RuntimeEnvironment.application.getBaseContext();

        final ClassLoader cl = baseContext.getClassLoader();
        final Class<?> activityThreadClass;
        try {
            activityThreadClass = cl.loadClass(shadowActivityThreadClassName);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }

        ReflectionHelpers.callInstanceMethod(Service.class, component, "attach",
                ReflectionHelpers.ClassParameter.from(Context.class, baseContext),
                ReflectionHelpers.ClassParameter.from(activityThreadClass, null),
                ReflectionHelpers.ClassParameter.from(String.class, component.getClass().getSimpleName()),
                ReflectionHelpers.ClassParameter.from(IBinder.class, null),
                ReflectionHelpers.ClassParameter.from(Application.class, RuntimeEnvironment.application),
                ReflectionHelpers.ClassParameter.from(Object.class, null));

        attached = true;
        return this;
    }

    public IntentServiceController<T> bind() {
        invokeWhilePaused("onBind", getIntent());
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
        invokeWhilePaused("onRebind", getIntent());
        return this;
    }

    public IntentServiceController<T> startCommand(final int flags, final int startId) {
        final IntentServiceController<T> intentServiceController = handleIntent();
        get().stopSelf(startId);
        return intentServiceController;
    }

    public IntentServiceController<T> unbind() {
        invokeWhilePaused("onUnbind", getIntent());
        return this;
    }

    public IntentServiceController<T> handleIntent() {
        invokeWhilePaused("onHandleIntent", getIntent());
        return this;
    }
}