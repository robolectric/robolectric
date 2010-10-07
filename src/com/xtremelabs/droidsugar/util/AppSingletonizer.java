package com.xtremelabs.droidsugar.util;

import android.app.Application;
import android.content.Context;

import java.util.WeakHashMap;

public class AppSingletonizer<T> {
    private WeakHashMap<Application, T> instances = new WeakHashMap<Application,T>();
    private Class<T> clazz;

    public AppSingletonizer(Class<T> clazz) {
        this.clazz = clazz;
    }

    synchronized public T getInstance(Context context) {
        @SuppressWarnings({"RedundantCast"})
        T instance = instances.get((Application) context.getApplicationContext());
        if (instance == null) {
            instance = createInstance();
            instances.put((Application) context, instance);
        }
        return instance;
    }

    protected T createInstance() {
        return FakeHelper.newInstanceOf(clazz);
    }
}
