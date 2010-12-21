package com.xtremelabs.robolectric.internal;

import android.app.Application;
import android.content.Context;
import com.xtremelabs.robolectric.shadows.ShadowApplication;

import static com.xtremelabs.robolectric.Robolectric.newInstanceOf;
import static com.xtremelabs.robolectric.Robolectric.shadowOf;

public abstract class AppSingletonizer<T> {
    private Class<T> clazz;

    public AppSingletonizer(Class<T> clazz) {
        this.clazz = clazz;
    }

    synchronized public T getInstance(Context context) {
        Application applicationContext = (Application) context.getApplicationContext();
        ShadowApplication shadowApplication = (ShadowApplication) shadowOf(applicationContext);
        T instance = get(shadowApplication);
        if (instance == null) {
            instance = createInstance(applicationContext);
            set(shadowApplication, instance);
        }
        return instance;
    }

    protected abstract T get(ShadowApplication shadowApplication);

    protected abstract void set(ShadowApplication shadowApplication, T instance);

    protected T createInstance(Application applicationContext) {
        return newInstanceOf(clazz);
    }
}
