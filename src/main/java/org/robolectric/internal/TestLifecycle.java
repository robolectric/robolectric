package org.robolectric.internal;

import org.robolectric.RobolectricContext;

import java.lang.reflect.Method;

public interface TestLifecycle<T> {
    void init(RobolectricContext robolectricContext);

    void beforeTest(Method method);

    void prepareTest(Object test);

    T createApplication(Method method);

    void afterTest(Method method);
}