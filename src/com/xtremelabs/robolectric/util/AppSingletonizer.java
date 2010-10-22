package com.xtremelabs.robolectric.util;

import android.app.Application;
import android.content.Context;
import com.xtremelabs.robolectric.ProxyDelegatingHandler;
import com.xtremelabs.robolectric.Robolectric;
import com.xtremelabs.robolectric.fakes.ShadowApplication;

public abstract class AppSingletonizer<T> {
    private Class<T> clazz;

    public AppSingletonizer(Class<T> clazz) {
        this.clazz = clazz;
    }

    synchronized public T getInstance(Context context) {
        Application applicationContext = (Application) context.getApplicationContext();
        ShadowApplication fakeApplication = (ShadowApplication) ProxyDelegatingHandler.getInstance().proxyFor(applicationContext);
        T instance = get(fakeApplication);
        if (instance == null) {
            instance = createInstance(applicationContext);
            set(fakeApplication, instance);
        }
        return instance;
    }

    protected abstract T get(ShadowApplication fakeApplication);

    protected abstract void set(ShadowApplication fakeApplication, T instance);

    protected T createInstance(Application applicationContext) {
        return Robolectric.newInstanceOf(clazz);
    }
}
