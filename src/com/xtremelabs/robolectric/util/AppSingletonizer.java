package com.xtremelabs.robolectric.util;

import android.app.Application;
import android.content.Context;
import com.xtremelabs.robolectric.ProxyDelegatingHandler;
import com.xtremelabs.robolectric.fakes.FakeApplication;

public abstract class AppSingletonizer<T> {
    private Class<T> clazz;

    public AppSingletonizer(Class<T> clazz) {
        this.clazz = clazz;
    }

    synchronized public T getInstance(Context context) {
        Application applicationContext = (Application) context.getApplicationContext();
        FakeApplication fakeApplication = (FakeApplication) ProxyDelegatingHandler.getInstance().proxyFor(applicationContext);
        T instance = get(fakeApplication);
        if (instance == null) {
            instance = createInstance(applicationContext);
            set(fakeApplication, instance);
        }
        return instance;
    }

    protected abstract T get(FakeApplication fakeApplication);

    protected abstract void set(FakeApplication fakeApplication, T instance);

    protected T createInstance(Application applicationContext) {
        return FakeHelper.newInstanceOf(clazz);
    }
}
